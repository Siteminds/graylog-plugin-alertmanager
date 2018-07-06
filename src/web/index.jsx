import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';
import AlertManagerPluginConfiguration from 'components/AlertManagerPluginConfiguration';
import packageJson from '../../package.json';

PluginStore.register(new PluginManifest(packageJson, {
  systemConfigurations: [
    {
      component: AlertManagerPluginConfiguration,
      configType: 'org.graylog.aws.config.AlertManagerPluginConfiguration',
    },
  ],
}));
