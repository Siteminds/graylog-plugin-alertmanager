import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import { Button } from 'react-bootstrap';
import { BootstrapModalForm, Input } from 'components/bootstrap';
import { IfPermitted } from 'components/common';
import ObjectUtils from 'util/ObjectUtils';

const AlertManagerPluginConfiguration = createReactClass({
  displayName: 'AlertManagerPluginConfiguration',

  propTypes: {
    config: PropTypes.object,
    updateConfig: PropTypes.func.isRequired,
  },

  getDefaultProps() {
    return {
      config: {
        alertmanager_api_url: 'https://alertmanager.mycompany.com/api',
        client_url: 'https://logging.mycompany.com'
      },
    };
  },

  getInitialState() {
    return {
      config: ObjectUtils.clone(this.props.config),
    };
  },

  componentWillReceiveProps(newProps) {
    this.setState({ config: ObjectUtils.clone(newProps.config) });
  },

  _updateConfigField(field, value) {
    const update = ObjectUtils.clone(this.state.config);
    update[field] = value;
    this.setState({ config: update });
  },

  _onCheckboxClick(field, ref) {
    return () => {
      this._updateConfigField(field, this.refs[ref].getChecked());
    };
  },

  _onSelect(field) {
    return (selection) => {
      this._updateConfigField(field, selection);
    };
  },

  _onUpdate(field) {
    return e => {
      this._updateConfigField(field, e.target.value);
    };
  },

  _openModal() {
    this.refs.awsConfigModal.open();
  },

  _closeModal() {
    this.refs.awsConfigModal.close();
  },

  _resetConfig() {
    // Reset to initial state when the modal is closed without saving.
    this.setState(this.getInitialState());
  },

  _saveConfig() {
    this.props.updateConfig(this.state.config).then(() => {
      this._closeModal();
    });
  },

  render() {
    return (
      <div>
        <h3>AlertManager Plugin Configuration</h3>

        <p>
          Settings for the AlertManager Plugin.
        </p>

        <dl className="deflist">
          <dt>AlertManager API URL:</dt>
          <dd>
            {this.state.config.alertmanager_api_url ? this.state.config.alertmanager_api_url : '[not set]'}
          </dd>

          <dt>Graylog public url:</dt>
          <dd>
            {this.state.config.client_url ? this.state.config.client_url : '[not set]'}
          </dd>
        </dl>

        <IfPermitted permissions="clusterconfigentry:edit">
          <Button bsStyle="info" bsSize="xs" onClick={this._openModal}>
            Configure
          </Button>
        </IfPermitted>

        <BootstrapModalForm
          ref="alertmanagerConfigModal"
          title="Update AlertManager Plugin Configuration"
          onSubmitForm={this._saveConfig}
          onModalClose={this._resetConfig}
          submitButtonText="Save">
          <fieldset>
            <Input
              id="alertmanager-api-url"
              type="text"
              label="AlertManager API URL"
              help={
                <span>
                  This URL needs to point to the AlertManager API endpoint.
                </span>
              }
              name="alertmanager_api_url"
              value={this.state.config.alertmanager_api_url}
              onChange={this._onUpdate('alertmanager_api_url')}
            />

            <Input
              id="client-url"
              type="text"
              label="Public URL of Graylog"
              help={
                <span>
                  This URL is used to construct a backlink to the Graylog alert.
                </span>
              }
              name="client_url"
              value={this.state.config.client_url}
              onChange={this._onUpdate('client_url')}
            />
          </fieldset>
        </BootstrapModalForm>
      </div>
    );
  },
});

export default AlertManagerPluginConfiguration;
