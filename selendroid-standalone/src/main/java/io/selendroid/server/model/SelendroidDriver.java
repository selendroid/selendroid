/*
 * Copyright 2013 selendroid committers.
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
package io.selendroid.server.model;

import io.selendroid.SelendroidCapabilities;
import io.selendroid.SelendroidConfiguration;
import io.selendroid.android.AndroidApp;
import io.selendroid.android.AndroidDevice;
import io.selendroid.android.AndroidEmulator;
import io.selendroid.android.impl.DefaultAndroidEmulator;
import io.selendroid.builder.SelendroidServerBuilder;
import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.exceptions.DeviceStoreException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.server.Versionable;
import io.selendroid.server.model.impl.DefaultHardwareDeviceFinder;
import io.selendroid.server.util.HttpClientUtil;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.SessionNotCreatedException;

import com.beust.jcommander.internal.Lists;

public class SelendroidDriver implements Versionable {
  public static final String WD_RESP_KEY_VALUE = "value";
  public static final String WD_RESP_KEY_STATUS = "status";
  public static final String WD_RESP_KEY_SESSION_ID = "sessionId";
  private static int selendroidServerPort = 38080;
  private static final Logger log = Logger.getLogger(SelendroidDriver.class.getName());
  private Map<String, AndroidApp> appsStore = new HashMap<String, AndroidApp>();
  private Map<String, AndroidApp> selendroidServers = new HashMap<String, AndroidApp>();
  private Map<String, ActiveSession> sessions = new HashMap<String, ActiveSession>();
  private DeviceStore deviceStore = null;
  private SelendroidServerBuilder selendroidApkBuilder = null;
  private SelendroidConfiguration serverConfiguration = null;
  private DeviceFinder androidDeviceFinder = null;

  public SelendroidDriver(SelendroidConfiguration serverConfiguration) throws AndroidSdkException,
      AndroidDeviceException {
    this.serverConfiguration = serverConfiguration;
    selendroidApkBuilder = new SelendroidServerBuilder();
    androidDeviceFinder = new DefaultHardwareDeviceFinder();
    initApplicationsUnderTest(serverConfiguration);
    initAndroidDevices();
  }

  /**
   * For testing only
   */
  /* package */SelendroidDriver(SelendroidServerBuilder builder, DeviceFinder deviceFinder) {
    this.selendroidApkBuilder = builder;
    androidDeviceFinder = deviceFinder;
  }

  /* package */void initApplicationsUnderTest(SelendroidConfiguration serverConfiguration)
      throws AndroidSdkException {
    if (serverConfiguration == null || serverConfiguration.getSupportedApps() == null
        || serverConfiguration.getSupportedApps().isEmpty()) {
      throw new SelendroidException("Configuration error - no apps has been configured.");
    }
    this.serverConfiguration = serverConfiguration;
    for (String appPath : serverConfiguration.getSupportedApps()) {
      File file = new File(appPath);
      if (file.exists()) {

        AndroidApp app = null;
        try {
          app = selendroidApkBuilder.resignApp(file);
        } catch (ShellCommandException e1) {
          throw new SessionNotCreatedException("An error occured while resigning the app '"
              + file.getName() + "'. ", e1);
        }
        String appId = null;
        try {
          appId = app.getAppId();
        } catch (SelendroidException e) {
          log.info("Ignoring app because an error occured reading the app details: "
              + file.getAbsolutePath());
          log.info(e.getMessage());
        }
        if (appId != null && !appsStore.containsKey(appId)) {
          appsStore.put(appId, app);
          log.info("App " + appId + " has been added to selendroid standalone server.");
        }
      } else {
        log.info("Ignoring app because it was not found: " + file.getAbsolutePath());
      }
    }
    if (appsStore.isEmpty()) {
      throw new SelendroidException(
          "Fatal error initializing SelendroidDriver: configured app(s) were not been found.");
    }
  }

  /* package */void initAndroidDevices() throws AndroidDeviceException {
    deviceStore = new DeviceStore();
    List<AndroidEmulator> emulators = DefaultAndroidEmulator.listAvailableAvds();
    deviceStore.addEmulators(emulators);
    List<AndroidDevice> devices = androidDeviceFinder.findConnectedDevices();
    deviceStore.addDevices(devices);
  }

  @Override
  public String getServerVersion() {
    String version = "dev";
    // TODO ddary read version number from jar
    return version;
  }

  @Override
  public String getCpuArch() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getOsVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  public String createNewTestSession(JSONObject caps) throws AndroidSdkException, JSONException {
    SelendroidCapabilities desiredCapabilities = null;
    try {
      desiredCapabilities = new SelendroidCapabilities(caps);
    } catch (JSONException e) {
      throw new SelendroidException("Desired capabilities cannot be parsed.");
    }
    AndroidApp app = appsStore.get(desiredCapabilities.getAut());
    if (app == null) {
      throw new SessionNotCreatedException(
          "The requested application under test is not configured in selendroid server.");
    }
    AndroidDevice device = null;
    try {
      device = getAndroidDevice(desiredCapabilities);
    } catch (AndroidDeviceException e) {
      SessionNotCreatedException error =
          new SessionNotCreatedException("Error occured while finding android device: "
              + e.getMessage());
      log.severe(error.getMessage());
      throw error;
    }
    if (device instanceof AndroidEmulator) {
      AndroidEmulator emulator = (AndroidEmulator) device;
      try {
        if (emulator.isEmulatorStarted()) {
          throw new SessionNotCreatedException("The Emulator '" + emulator
              + "' is already started even though it should be switched off.");
        } else {
          Map<String, Object> config = new HashMap<String, Object>();
          config.put(AndroidEmulator.TIMEOUT_OPTION, serverConfiguration.getTimeoutEmulatorStart());
          if (desiredCapabilities.is(SelendroidCapabilities.DISPLAY)) {
            config.put(AndroidEmulator.DISPLAY_OPTION,
                desiredCapabilities.asMap().get(SelendroidCapabilities.DISPLAY));
          }
          Locale locale = parseLocale(desiredCapabilities);
          emulator.start(locale, deviceStore.nextEmulatorPort(), config);
        }
      } catch (AndroidDeviceException e) {
        try {
          deviceStore.release(device);
        } catch (AndroidDeviceException e1) {}
        throw new SessionNotCreatedException("Error occured while interacting with the emulator: "
            + emulator + ": " + e.getMessage());
      }
    }
    AndroidApp selendroidServer = createSelendroidServerApk(app);

    device.install(app);
    device.install(selendroidServer);
    int port = getNextSelendroidServerPort();
    device.startSelendroid(app, port);
    JSONObject response = null;

    try {
      HttpResponse r = HttpClientUtil.executeCreateSessionRequest(port, desiredCapabilities);
      response = HttpClientUtil.parseJsonResponse(r);
    } catch (Exception e) {
      throw new SessionNotCreatedException(
          "Error occured while creating session on Android device", e);
    }
    System.out.println("Create new session response: " + response.toString(2));
    if (response.getInt(WD_RESP_KEY_STATUS) != 0) {
      throw new SessionNotCreatedException(
          "Error occured while initilizing wd session on android device.");
    }
    String sessionId = response.optString(WD_RESP_KEY_SESSION_ID);
    SelendroidCapabilities requiredCapabilities =
        new SelendroidCapabilities(response.getJSONObject(WD_RESP_KEY_VALUE));
    ActiveSession session = new ActiveSession(sessionId, requiredCapabilities, app, device, port);

    this.sessions.put(sessionId, session);

    return sessionId;
  }

  private AndroidApp createSelendroidServerApk(AndroidApp aut) throws AndroidSdkException {
    if (!selendroidServers.containsKey(aut.getAppId())) {
      try {
        AndroidApp selendroidServer =
            selendroidApkBuilder.createSelendroidServer(aut.getAbsolutePath());
        selendroidServers.put(aut.getAppId(), selendroidServer);
      } catch (Exception e) {
        e.printStackTrace();
        throw new SessionNotCreatedException(
            "An error occured while building the selendroid-server.apk for aut '" + aut + "': "
                + e.getMessage());
      }
    }
    return selendroidServers.get(aut.getAppId());
  }

  private Locale parseLocale(SelendroidCapabilities capa) {
    String[] localeStr = capa.getLocale().split("_");
    Locale locale = new Locale(localeStr[0], localeStr[1]);

    return locale;
  }

  /* package */AndroidDevice getAndroidDevice(SelendroidCapabilities caps)
      throws AndroidDeviceException {
    AndroidDevice device = null;
    Boolean emulator = caps.getEmulator();
    if (emulator == null) {
      emulator = Boolean.TRUE;
      log.warning("'emualtor' capability in desired capabilities. Assuming an emulator was meant.");
    }

    try {
      device = deviceStore.findAndroidDevice(caps);
    } catch (DeviceStoreException e) {
      throw new AndroidDeviceException("Error occured while looking for devices/emulators.", e);
    }

    return device;
  }

  /**
   * For testing only
   */
  /* package */Map<String, AndroidApp> getConfiguredApps() {
    return Collections.unmodifiableMap(appsStore);
  }

  /**
   * For testing only
   */
  /* package */void setDeviceStore(DeviceStore store) {
    this.deviceStore = store;
  }

  private synchronized int getNextSelendroidServerPort() {
    return selendroidServerPort++;
  }

  /** FOR TESTING ONLY */
  public List<ActiveSession> getActiceSessions() {
    return Lists.newArrayList(sessions.values());
  }

  public boolean isValidSession(String sessionId) {
    if (sessionId != null && sessionId.isEmpty() == false) {
      return sessions.containsKey(sessionId);
    }
    return false;
  }

  public void stopSession(String sessionId) throws AndroidDeviceException {
    if (isValidSession(sessionId)) {
      ActiveSession session = sessions.get(sessionId);
      try {
        HttpClientUtil.executeRequest("http://localhost:" + session.getSelendroidServerPort()
            + "/wd/hub/sessions/" + sessionId, HttpMethod.DELETE);
      } catch (Exception e) {
        // can happen, ignore
      }
      deviceStore.release(session.getDevice());

      // remove session
      sessions.remove(session);
      session = null;
    }
  }

  public void quitSelendroid() {
    List<String> sessionsToQuit = Lists.newArrayList(sessions.keySet());
    if (sessionsToQuit != null && sessionsToQuit.isEmpty() == false) {
      for (String sessionId : sessionsToQuit) {
        try {
          stopSession(sessionId);
        } catch (AndroidDeviceException e) {
          log.severe("Error occured while stopping session: " + e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }

  public SelendroidCapabilities getSessionCapabilities(String sessionId) {
    if (sessions.containsKey(sessionId)) {
      return sessions.get(sessionId).getDesiredCapabilities();
    }
    return null;
  }

  public ActiveSession getActiveSession(String sessionId) {
    if (sessionId != null && sessions.containsKey(sessionId)) {
      return sessions.get(sessionId);
    }

    return null;
  }

}
