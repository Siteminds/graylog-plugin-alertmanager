package org.graylog.plugins.alertmanager.alerts;

import com.google.common.annotations.VisibleForTesting;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.BooleanField;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.streams.Stream;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import java.net.URI;
import java.net.URISyntaxException;

public class AlertManagerAlertNotification implements AlarmCallback {

    private Configuration config;
    private final URI httpProxyUri;

    private static final String CK_ALERTMANAGER_API_URL = "alertmanager_api_url";
    private static final String CK_CLIENT_URL = "client_url";


    @Inject
    public AlertManagerAlertNotification(@Named("http_proxy_uri") @Nullable URI httpProxyUri) {
        this.httpProxyUri = httpProxyUri;
    }

    @Override
    public void initialize(Configuration config) throws AlarmCallbackConfigurationException {
        this.config = config;
    }

    @Override
    public void call(final Stream stream, final AlertCondition.CheckResult result) throws AlarmCallbackException {
        call(new AlertManagerClient(
                httpProxyUri,
                config.getString(CK_ALERTMANAGER_API_URL),
                config.getString(CK_CLIENT_URL)), stream, result);
    }

    @VisibleForTesting
    public void call(final AlertManagerClient client, final Stream stream, final AlertCondition.CheckResult result) throws AlarmCallbackException {
        client.trigger(stream, result);
    }

    @Override
    public ConfigurationRequest getRequestedConfiguration() {
        final ConfigurationRequest configurationRequest = new ConfigurationRequest();

        configurationRequest.addField(new TextField(
                CK_ALERTMANAGER_API_URL, "AlertManager API URL", "",
                "The URL of the AlertManager API instance the notifications should be send to.",
                ConfigurationField.Optional.NOT_OPTIONAL));

        configurationRequest.addField(new TextField(
                CK_CLIENT_URL, "Client URL", "",
                "The URL of the Graylog system that is triggering the AlertManager event.",
                ConfigurationField.Optional.OPTIONAL));

        return configurationRequest;
    }

    @Override
    public String getName() {
        return "AlertManager Alert Notification";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return config.getSource();
    }

    @Override
    public void checkConfiguration() throws ConfigurationException {
        if (config.stringIsSet(CK_ALERTMANAGER_API_URL)) {
            try {
                final URI clientUri = new URI(config.getString(CK_ALERTMANAGER_API_URL));

                if (!"http".equals(clientUri.getScheme()) && !"https".equals(clientUri.getScheme())) {
                    throw new ConfigurationException(CK_ALERTMANAGER_API_URL + " must be a valid HTTP or HTTPS URL.");
                }
            } catch (URISyntaxException e) {
                throw new ConfigurationException("Couldn't parse " + CK_ALERTMANAGER_API_URL + " correctly.", e);
            }
        }
    }
}
