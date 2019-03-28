# AlertManager Plugin for Graylog

[![Build Status](https://travis-ci.org/Siteminds/graylog-plugin-alertmanager.svg?branch=master)](https://travis-ci.org/Siteminds/graylog-plugin-alertmanager)

This plugin enables Graylog to send alert notifications to 
Prometheus [AlertManager](https://prometheus.io/docs/alerting/alertmanager/)

**Required Graylog version:** 2.4.5 and later

Installation
------------

[Download the plugin](https://github.com/Siteminds/graylog-plugin-alertmanager/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

Development
-----------

You can improve your development experience for the web interface part of your plugin
dramatically by making use of hot reloading. To do this, do the following:

* `git clone https://github.com/Graylog2/graylog2-server.git`
* `cd graylog2-server/graylog2-web-interface`
* `ln -s $YOURPLUGIN plugin/`
* `npm install && npm start`

Usage
-----

After installation of the plugin, the following settings need to be added
to the `graylog.conf` file:
* `alertmanager_api_url`
  This needs to point to the API endpoint of your AlertManager instance. 
  E.g.: `https://alertmanager.mycompany.com:8443/api/`
* `client_url`
  This needs to point back to the public url of the graylog server.
  E.g.: `https://logging.mycompany.com`


Getting started
---------------

This project is using Maven 3 and requires Java 7 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.

Plugin Release
--------------

We are using the maven release plugin:

```
$ mvn release:prepare
[...]
$ mvn release:perform
```

This sets the version numbers, creates a tag and pushes to GitHub. Travis CI will build the release artifacts and upload to GitHub automatically.
