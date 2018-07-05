package org.graylog.plugins.alertmanager;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * Implement the PluginMetaData interface here.
 */
public class AlertManagerMetaData implements PluginMetaData {
    private static final String PLUGIN_PROPERTIES = "org.graylog.plugins.graylog-plugin-alertmanager/graylog-plugin.properties";

    @Override
    public String getUniqueId() {
        return "org.graylog.plugins.alertmanager.AlertManagerPlugin";
    }

    @Override
    public String getName() {
        return "AlertManagerNotification";
    }

    @Override
    public String getAuthor() {
        return "Siteminds B.V. <bastiaan.schaap@siteminds.nl>";
    }

    @Override
    public URI getURL() {
        return URI.create("https://github.com/Siteminds/graylog-plugin-alertmanager");
    }

    @Override
    public Version getVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "version", Version.from(0, 1, 0, "unknown"));
    }

    @Override
    public String getDescription() {
        // TODO Insert correct plugin description
        return "This plugin allows for sending alert notifications to AlertManager";
    }

    @Override
    public Version getRequiredVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "graylog.version", Version.from(2, 4, 5, "unknown"));
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
