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

import io.selendroid.SelendroidConfiguration;
import io.selendroid.android.AndroidApp;
import io.selendroid.android.AndroidDevice;
import io.selendroid.android.AndroidEmulator;
import io.selendroid.android.impl.DefaultAndroidApp;
import io.selendroid.android.impl.DefaultAndroidEmulator;
import io.selendroid.exceptions.AndroidDeviceException;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.xerces.impl.dtd.models.DFAContentModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selendroid.SelendroidCapabilities;
import org.openqa.selendroid.device.DeviceTargetPlatform;
import org.openqa.selendroid.exceptions.SelendroidException;
import org.openqa.selendroid.server.Versionable;
import org.openqa.selenium.SessionNotCreatedException;

public class SelendroidDriver implements Versionable {
  private static final Logger log = Logger.getLogger(SelendroidDriver.class.getName());
  private Map<String, AndroidApp> apps = new HashMap<String, AndroidApp>();
  private Map<String, AndroidApp> selendroidServes = new HashMap<String, AndroidApp>();
  private Map<String, ActiveSession> sessions = new HashMap<String, ActiveSession>();
  private DeviceStore deviceStore = null;

  public SelendroidDriver(SelendroidConfiguration serverConfiguration) {
    initApplicationsUnderTest(serverConfiguration);
  }

  /**
   * For testing only
   */
  /* package */SelendroidDriver() {}

  /* package */void initApplicationsUnderTest(SelendroidConfiguration serverConfiguration) {
    if (serverConfiguration.getSupportedApps() == null
        || serverConfiguration.getSupportedApps().isEmpty()) {
      throw new SelendroidException("Configuration error - no apps has been configured.");
    }
    for (String appPath : serverConfiguration.getSupportedApps()) {
      File file = new File(appPath);
      if (file.exists()) {
        AndroidApp app = new DefaultAndroidApp(file);
        String appId = null;
        try {
          appId = app.getAppId();
        } catch (SelendroidException e) {
          log.info("Ignoring app because an error occured reading the app details: "
              + file.getAbsolutePath());
          log.info(e.getMessage());
        }
        if (appId != null && !apps.containsKey(appId)) {
          apps.put(appId, app);
          log.info("App " + appId + " has been added to selendroid standalone server.");
        }
      } else {
        log.info("Ignoring app because it was not found: " + file.getAbsolutePath());
      }
    }
    if (apps.isEmpty()) {
      throw new SelendroidException(
          "Fatal error initializing SelendroidDriver: configured app(s) were not been found.");
    }
  }

  /* package */void initAndroidDevices() throws AndroidDeviceException {
    deviceStore = new DeviceStore();
    List<AndroidEmulator> emulators = DefaultAndroidEmulator.listAvailableAvds();
    deviceStore.addEmulators(emulators);
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

  public String createNewTestSession(JSONObject caps) {
    SelendroidCapabilities desiredCapabilities = null;
    try {
      desiredCapabilities = new SelendroidCapabilities(caps);
    } catch (JSONException e) {
      throw new SelendroidException("Desired capabilities cannot be parsed.");
    }
    AndroidApp app = apps.get(desiredCapabilities.getAut());
    if (app == null) {
      throw new SessionNotCreatedException(
          "The requested application under test is not configured in selendroid server.");
    }
    try {
      AndroidDevice device = getAndroidDevice(desiredCapabilities);
    } catch (AndroidDeviceException e) {
      SessionNotCreatedException error =
          new SessionNotCreatedException("Error occured while finding android device: "
              + e.getMessage());
      log.severe(error.getMessage());
      throw error;
    }

    return null;
  }

  /* package */AndroidDevice getAndroidDevice(SelendroidCapabilities caps)
      throws AndroidDeviceException {
    AndroidDevice device = null;
    if (caps.getEmulator()) {
      device = deviceStore.findAndroidDevice(caps);
      if (!device.isDeviceReady()) {
        log.severe("Error - Emulator that should be switched off seems to run; " + device);
      }
    } else {
      throw new AndroidDeviceException(
          "Currently only emulators are supported in selendroid standalone.");
    }
    return device;
  }

  /**
   * For testing only
   */
  /* package */Map<String, AndroidApp> getConfiguredApps() {
    return Collections.unmodifiableMap(apps);
  }
}
