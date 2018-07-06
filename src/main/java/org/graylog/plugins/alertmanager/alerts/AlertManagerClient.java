package org.graylog.plugins.alertmanager.alerts;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.streams.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;

import java.text.SimpleDateFormat;
import java.util.*;


public class AlertManagerClient {

    private static final Logger LOG = LoggerFactory.getLogger(AlertManagerClient.class);

    private final URI httpProxyUri;
    private final String alertManagerAPIURL;
    private final String clientUrl;
    private final ObjectMapper objectMapper;

    public AlertManagerClient(final URI httpProxyUri,
                              final String alertManagerAPIURL,
                              final String clientUrl,
                              final ObjectMapper objectMapper) {

        if (!alertManagerAPIURL.endsWith("/")) {
            this.alertManagerAPIURL = alertManagerAPIURL + "/v1/alerts";
        } else {
            this.alertManagerAPIURL = alertManagerAPIURL + "v1/alerts";
        }
        this.httpProxyUri = httpProxyUri;
        this.clientUrl = clientUrl;
        this.objectMapper = objectMapper;
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));
    }

    public AlertManagerClient(final URI httpProxyUri,
                              final String alertManagerAPIURL,
                              final String clientUrl) {
        this(httpProxyUri, alertManagerAPIURL, clientUrl, new ObjectMapper());
    }

    public void trigger(final Stream stream, final AlertCondition.CheckResult checkResult) throws AlarmCallbackException {
        final URL url;
        try {
            url = new URL(alertManagerAPIURL);

        } catch (MalformedURLException e) {
            throw new AlarmCallbackException("Malformed URL for AlertManager API.", e);
        }

        final HttpURLConnection conn;
        try {
            if (httpProxyUri != null) {
                final InetSocketAddress proxyAddress = new InetSocketAddress(httpProxyUri.getHost(), httpProxyUri.getPort());
                final Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
                conn = (HttpURLConnection) url.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setRequestMethod("POST");

        } catch (IOException e) {
            throw new AlarmCallbackException("Error while opening connection to AlertManager API.", e);
        }

        conn.setDoOutput(true);

        try (final OutputStream requestStream = conn.getOutputStream()) {
            final List<AlertManagerEvent> event = buildAlertManagerEvent(stream, checkResult);
            LOG.debug("going to send AlertManager event:");
            LOG.debug(objectMapper.writeValueAsString(event));
            requestStream.write(objectMapper.writeValueAsBytes(event));
            requestStream.flush();

            final InputStream responseStream;
            if (conn.getResponseCode() == 200) {
                responseStream = conn.getInputStream();
            } else {
                responseStream = conn.getErrorStream();
            }

            final AlertManagerResponse response = objectMapper.readValue(responseStream, AlertManagerResponse.class);
            if ("success".equals(response.status)) {
                LOG.debug("Successfully sent alert to AlertManager");
            } else {
                LOG.warn("Error while creating alert at AlertManager: {} ({}: {})",
                        response.status, response.errorType, response.error);
                throw new AlarmCallbackException("Error while creating alert at AlertManager: " +
                        response.errorType + " " + response.error);
            }
        } catch (IOException e) {
            throw new AlarmCallbackException("Could not POST alert to AlertManager API.", e);
        }
    }

    private String buildStreamLink(String baseUrl, Stream stream) {
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        return baseUrl + "streams/" + stream.getId() + "/messages?q=*&rangetype=relative&relative=3600";
    }

    private List<AlertManagerEvent> buildAlertManagerEvent(final Stream stream, final AlertCondition.CheckResult checkResult) {
        final String alertDescription = checkResult.getTriggeredCondition().getDescription();
        final String description = "[ " + stream.getTitle() + " ] " + checkResult.getResultDescription();

        final AlertManagerEvent event = new AlertManagerEvent(
                ImmutableMap.<String, String>of(
                        "stream_id", stream.getId(),
                        "stream_title", stream.getTitle(),
                        "backlog", Integer.toString(checkResult.getTriggeredCondition().getBacklog()),
                        "search_hits", Integer.toString(getAlarmBacklog(checkResult).size()),
                        "alert_description", alertDescription
                ),
                ImmutableMap.<String, Object>of(
                        "summary", "Graylog alert for " + stream.getTitle(),
                        "description", description
                ),
                new Date(),
                buildStreamLink(clientUrl, stream)
        );
        List<AlertManagerEvent> list = new ArrayList<>();
        list.add(event);
        return list;
    }

    protected List<Message> getAlarmBacklog(AlertCondition.CheckResult result) {
        final AlertCondition alertCondition = result.getTriggeredCondition();
        final List<MessageSummary> matchingMessages = result.getMatchingMessages();

        final int effectiveBacklogSize = Math.min(alertCondition.getBacklog(), matchingMessages.size());

        if (effectiveBacklogSize == 0) {
            return Collections.emptyList();
        }

        final List<MessageSummary> backlogSummaries = matchingMessages.subList(0, effectiveBacklogSize);

        final List<Message> backlog = Lists.newArrayListWithCapacity(effectiveBacklogSize);

        for (MessageSummary messageSummary : backlogSummaries) {
            backlog.add(messageSummary.getRawMessage());
        }

        return backlog;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class AlertManagerEvent {

        @JsonProperty
        public Map<String, String> labels;
        @JsonProperty
        public Map<String, Object> annotations;
        @JsonProperty
        public Date startsAt;
        @JsonProperty
        public String generatorURL;


        public AlertManagerEvent(Map<String, String> labels,
                                 Map<String, Object> annotations,
                                 Date startsAt,
                                 String generatorURL) {
            this.labels = labels;
            this.annotations = annotations;
            this.startsAt = startsAt;
            this.generatorURL = generatorURL;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AlertManagerResponse {
        @JsonProperty
        public String status;
        @JsonProperty
        public String errorType;
        @JsonProperty
        public String error;
        @JsonProperty
        public List<String> data;
    }

}
