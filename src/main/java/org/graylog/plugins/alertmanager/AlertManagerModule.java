package org.graylog.plugins.alertmanager;

import org.graylog.plugins.alertmanager.alerts.AlertManagerAlertNotification;
import org.graylog2.plugin.PluginConfigBean;
import org.graylog2.plugin.PluginModule;

import java.util.Collections;
import java.util.Set;

/**
 * Register the AlertManager plugin with the system.
 */
public class AlertManagerModule extends PluginModule {
    /**
     * Returns all configuration beans required by this plugin.
     */
    @Override
    public Set<? extends PluginConfigBean> getConfigBeans() {
        return Collections.emptySet();
    }

    @Override
    protected void configure() {
        addAlarmCallback(AlertManagerAlertNotification.class);
    }
}
