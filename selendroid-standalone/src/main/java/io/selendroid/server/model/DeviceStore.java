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

import io.selendroid.android.AndroidDevice;
import io.selendroid.android.AndroidEmulator;
import io.selendroid.exceptions.AndroidDeviceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openqa.selendroid.SelendroidCapabilities;
import org.openqa.selendroid.device.DeviceTargetPlatform;
import org.openqa.selendroid.exceptions.SelendroidException;

public class DeviceStore {
  private static final Logger log = Logger.getLogger(DeviceStore.class.getName());
  private Map<String, AndroidDevice> devicesInUse = new HashMap<String, AndroidDevice>();
  private Map<DeviceTargetPlatform, List<AndroidDevice>> androidDevices =
      new HashMap<DeviceTargetPlatform, List<AndroidDevice>>();

  public DeviceStore() {}

  public void addEmulators(List<AndroidEmulator> emulators) throws AndroidDeviceException {
    if (emulators == null || emulators.isEmpty()) {
      SelendroidException e =
          new SelendroidException(
              "No android virtual devices were found. Please start the android tool and create emulators.");
      log.severe("Error: " + e);
      throw e;
    }
    for (AndroidEmulator emulator : emulators) {
      if (emulator.isEmulatorStarted()) {
        log.info("Skipping emulator because it is already in use: " + emulator);
        continue;
      }
      addAndroidEmulator(emulator);
    }
    if (androidDevices.isEmpty()) {
      throw new SelendroidException("No Android virtual devices that can be used were found. "
          + "Please note that only switched off emulators can be used.");
    }
  }

  protected void addAndroidEmulator(AndroidEmulator emulator) throws AndroidDeviceException {
    if (androidDevices.containsKey(emulator.getTargetPlatform())) {
      if (androidDevices.get(emulator.getTargetPlatform()) == null) {
        androidDevices.put(emulator.getTargetPlatform(), new ArrayList<AndroidDevice>());
      }
      androidDevices.get(emulator.getTargetPlatform()).add((AndroidDevice) emulator);
    } else {
      List<AndroidDevice> device = new ArrayList<AndroidDevice>();
      device.add((AndroidDevice) emulator);
      androidDevices.put(emulator.getTargetPlatform(), device);
    }
  }

  public AndroidDevice findAndroidDevice(SelendroidCapabilities caps) {
    throw new SelendroidException("NOT IMPLEMENTED YET");
  }

  /**
   * For testing only
   */
  /* package */Map<String, AndroidDevice> getDevicesInUse() {
    return devicesInUse;
  }

  /**
   * For testing only
   */
  /* package */Map<DeviceTargetPlatform, List<AndroidDevice>> getDevicesList() {
    return androidDevices;
  }
}
