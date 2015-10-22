/*
 * Copyright 2012-2015 eBay Software Foundation and selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.selendroid.standalone.server.grid;

import com.google.gson.Gson;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.remote.CapabilityType;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.selendroid.standalone.server.model.SelendroidStandaloneDriver.APP_BASE_PACKAGE;
import static io.selendroid.standalone.server.model.SelendroidStandaloneDriver.APP_ID;

public class SelfRegisteringRemote {
  private static final Logger log = Logger.getLogger(SelfRegisteringRemote.class.getName());
  public static final String ANDROIDDRIVER_APP = "io.selendroid.androiddriver";
  private SelendroidConfiguration config;
  private SelendroidStandaloneDriver driver;
  private boolean isFirstTime = true;
  private final URL hub;

  public SelfRegisteringRemote(SelendroidConfiguration config, SelendroidStandaloneDriver driver) {
    this.config = config;
    this.driver = driver;
    this.hub = getGridHubUrl(config.getRegistrationUrl());
  }

  public void performRegistrationIfNotRegistered() {
    try {
      if (isFirstTime) {
        performRegistration();
        isFirstTime = false;
      } else if (!isRegistered()) {
        performRegistration();
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "An error occurred while registering selendroid into grid hub.", e);
    }
  }

  public void performRegistration() throws Exception {
    String nodeConfigString = getNodeConfig().toString();
    log.info("Registering Selendroid node with following config:\n" + nodeConfigString + "\n" +
            "at Selenium Grid hub: " + hub.toExternalForm());

    BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST",
            hub.toExternalForm());
    request.setEntity(new StringEntity(nodeConfigString));

    HttpResponse response = getHttpClient().execute(getHttpHost(), request);

    if (response.getStatusLine().getStatusCode() != 200) {
      throw new SelendroidException("Error sending the registration request. Response from server: " +
              response.getStatusLine().toString());
    }
  }

  // for testing purpose
  protected HttpClient getHttpClient() {
    return HttpClientBuilder.create().build();
  }

  /**
   * Get the node configuration and capabilities for Grid registration
   *
   * @return The configuration
   */
  private JSONObject getNodeConfig() {
    JSONObject res = new JSONObject();
    try {
      res.put("class", "org.openqa.grid.common.RegistrationRequest");
      res.put("configuration", getConfiguration());
      JSONArray caps = new JSONArray();
      JSONArray devices = driver.getSupportedDevices();
      for (int i = 0; i < devices.length(); i++) {
        JSONObject device = (JSONObject) devices.get(i);
        for (int x = 0; x < driver.getSupportedApps().length(); x++) {
          caps.put(getDeviceConfig(device, driver.getSupportedApps().getJSONObject(x)));
        }
      }
      res.put("capabilities", caps);
    } catch (JSONException e) {
      throw new SelendroidException(e.getMessage(), e);
    }
    return res;
  }

  private JSONObject getDeviceConfig(final JSONObject device, final JSONObject supportedApp) throws JSONException {
    JSONObject capa = new JSONObject(device, JSONObject.getNames(device));
    // For each device, register as "android" for WebView tests and also selendroid if an aut is specified
    if (ANDROIDDRIVER_APP.equals(supportedApp.get(APP_BASE_PACKAGE))) {
      //it's possible the user does not want to register the device as capable to recieve webview tests
      if (!config.isNoWebViewApp()) {
        capa.put(CapabilityType.BROWSER_NAME, "android");
      }
    } else {
      capa.put(CapabilityType.BROWSER_NAME, "selendroid");
      capa.put(SelendroidCapabilities.AUT, supportedApp.get(APP_ID));
    }
    capa.put(CapabilityType.PLATFORM, "ANDROID");
    capa.put(SelendroidCapabilities.PLATFORM_NAME, "android");
    capa.put(CapabilityType.VERSION, device.getString(SelendroidCapabilities.PLATFORM_VERSION));
    capa.put("maxInstances", config.getMaxInstances());
    return capa;
  }

  /**
   * Extracts the configuration.
   *
   * @return The configuration
   * @throws JSONException On JSON errors.
   */
  private JSONObject getConfiguration() throws JSONException {
    JSONObject configuration = new JSONObject();

    configuration.put("port", config.getPort());
    configuration.put("register", true);

    if (config.getProxy() != null) {
      configuration.put("proxy", config.getProxy());
    } else {
      configuration.put("proxy", "org.openqa.grid.selenium.proxy.DefaultRemoteProxy");
    }
    configuration.put("role", "node");
    configuration.put("registerCycle", config.getRegisterCycle());
    if (config.getMaxSession() == 0) {
      configuration.put("maxSession", driver.getSupportedDevices().length());
    } else {
      configuration.put("maxSession", config.getMaxSession());
    }
    configuration.put("browserTimeout", config.getSessionTimeoutMillis() / 1000);
    configuration.put("cleanupCycle", config.getCleanupCycle());
    configuration.put("timeout", config.getTimeout());
    configuration.put("nodePolling", config.getNodePolling());
    configuration.put("unregisterIfStillDownAfter", config.getUnregisterIfStillDownAfter());
    configuration.put("downPollingLimit", config.getDownPollingLimit());
    configuration.put("nodeStatusCheckTimeout", config.getNodeStatusCheckTimeout());

    // adding hub details
    configuration.put("hubHost", hub.getHost());
    configuration.put("hubPort", hub.getPort());

    // adding driver details
    configuration.put("seleniumProtocol", "WebDriver");
    configuration.put("host", config.getServerHost());
    configuration.put("remoteHost", "http://" + config.getServerHost() + ":" + config.getPort());
    return configuration;
  }

  protected boolean isRegistered() {
    try {
      URL hubProxyApiUrl = new URL(hub.getProtocol(), hub.getHost(), hub.getPort(), "/grid/api/proxy");
      HttpResponse response = getHttpClient().execute(getHttpHost(), new BasicHttpRequest("GET",
              hubProxyApiUrl.toExternalForm() + "?id=http://" + config.getServerHost() + ":" + config.getPort()));

      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("Hub is down or not responding. Response was: " + response.getStatusLine().toString());
      }

      GridResponse gridResponse = new Gson().fromJson(EntityUtils.toString(response.getEntity()), GridResponse.class);
      return gridResponse.success;
    } catch (IOException e) {
      throw new SelendroidException("Hub is down or not responding.", e);
    }
  }

  private HttpHost getHttpHost() {
    return new HttpHost(hub.getHost(), hub.getPort(), hub.getProtocol());
  }

  private URL getGridHubUrl(String connectionString) {
      try {
        return new URL(connectionString);
      } catch (MalformedURLException e) {
        throw new SelendroidException("Grid hub connection string cannot be parsed into URL", e);
      }
  }

  private class GridResponse {
    private boolean success;
  }

}
