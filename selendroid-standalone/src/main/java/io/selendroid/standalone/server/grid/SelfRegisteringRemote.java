/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
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

import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;
import io.selendroid.standalone.server.util.HttpClientUtil;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.remote.CapabilityType;

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

  public SelfRegisteringRemote(SelendroidConfiguration config, SelendroidStandaloneDriver driver) {
    this.config = config;
    this.driver = driver;
  }

  public void performRegistration() throws Exception {
    String tmp = config.getRegistrationUrl();

    HttpClient client = HttpClientUtil.getHttpClient();

    URL registration = new URL(tmp);
    if (log.isLoggable(Level.INFO)) {
      log.info("Registering selendroid node to Selenium Grid hub :" + registration);
    }
    BasicHttpEntityEnclosingRequest r =
        new BasicHttpEntityEnclosingRequest("POST", registration.toExternalForm());
    JSONObject nodeConfig = getNodeConfig();
    String nodeConfigString = nodeConfig.toString();
    if (log.isLoggable(Level.INFO)) {
      log.info("Registering selendroid node with following config:\n" + nodeConfigString);
    }
    r.setEntity(new StringEntity(nodeConfigString));

    HttpHost host = new HttpHost(registration.getHost(), registration.getPort());
    HttpResponse response = client.execute(host, r);
    if (response.getStatusLine().getStatusCode() != 200) {
      throw new SelendroidException("Error sending the registration request.");
    }
  }

  /**
   * Get the node configuration and capabilities for Grid registration
   *
   * @return The configuration
   * @throws JSONException On JSON errors.
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
          JSONObject capa = new JSONObject();
          capa.put(SelendroidCapabilities.SCREEN_SIZE,
                  device.getString(SelendroidCapabilities.SCREEN_SIZE));
          String version = device.getString(SelendroidCapabilities.PLATFORM_VERSION);
          capa.put(SelendroidCapabilities.PLATFORM_VERSION, version);
          capa.put(SelendroidCapabilities.EMULATOR, device.getString(SelendroidCapabilities.EMULATOR));
          // For each device, register as "android" for WebView tests and also selendroid if an aut is specified
          if (ANDROIDDRIVER_APP.equals(driver.getSupportedApps().getJSONObject(x).get(APP_BASE_PACKAGE))) {
            //it's possible the user does not want to register the device as capable to recieve webview tests
            if (!config.isNoWebViewApp()) {
              capa.put(CapabilityType.BROWSER_NAME, "android");
            }
          } else {
            capa.put(CapabilityType.BROWSER_NAME, "selendroid");
            capa.put(SelendroidCapabilities.AUT, driver.getSupportedApps().getJSONObject(x).get(APP_ID));
          }
          capa.put(CapabilityType.PLATFORM, "ANDROID");
          capa.put(CapabilityType.VERSION, version);
          capa.put("maxInstances", config.getMaxInstances());
          caps.put(capa);
        }
      }
      res.put("capabilities", caps);
    } catch (JSONException e) {
      throw new SelendroidException(e.getMessage(), e);
    }
    return res;
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
    configuration.put("registerCycle", 5000);
    configuration.put("maxSession", config.getMaxSession());

    // adding hub details
    URL registrationUrl;
    try {
      registrationUrl = new URL(config.getRegistrationUrl());
    } catch (MalformedURLException e) {
      log.log(Level.SEVERE, "Grid hub url cannot be parsed", e);
      throw new SelendroidException("Grid hub url cannot be parsed: " + e.getMessage());
    }
    configuration.put("hubHost", registrationUrl.getHost());
    configuration.put("hubPort", registrationUrl.getPort());

    // adding driver details
    configuration.put("seleniumProtocol", "WebDriver");
    configuration.put("host", config.getServerHost());
    configuration.put("remoteHost", "http://" + config.getServerHost() + ":" + config.getPort());
    return configuration;
  }
}
