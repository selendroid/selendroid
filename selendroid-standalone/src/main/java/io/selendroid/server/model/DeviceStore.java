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
import io.selendroid.exceptions.DeviceStoreException;

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
  private List<AndroidDevice> devicesInUse = new ArrayList<AndroidDevice>();
  private Map<DeviceTargetPlatform, List<AndroidDevice>> androidDevices =
      new HashMap<DeviceTargetPlatform, List<AndroidDevice>>();
  private static final int ANDROID_EMULATOR_PORT = 5554;
  private Integer nextEmulatorPort = null;

  public DeviceStore() {}

  public Integer nextEmulatorPort() {
    if (nextEmulatorPort == null) {
      nextEmulatorPort = ANDROID_EMULATOR_PORT;
    }else{
     nextEmulatorPort += 2;
    }
    return nextEmulatorPort;
  }

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
      log.info("Adding: " + emulator);
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


  public synchronized AndroidDevice findAndroidDevice(SelendroidCapabilities caps)
      throws DeviceStoreException {
    if (caps == null) {
      throw new IllegalArgumentException("Error: capabilities are null");
    }
    if (androidDevices.isEmpty()) {
      throw new DeviceStoreException(
          "Fatal Error: Device Store does not contain any Android Device.");
    }
    DeviceTargetPlatform platform = DeviceTargetPlatform.valueOf(caps.getAndroidTarget());
    if (!androidDevices.containsKey(platform)) {
      throw new DeviceStoreException(
          "Device store does not contain a device of requested platform: " + platform);
    }
    for (AndroidDevice device : androidDevices.get(platform)) {
      if (device.isDeviceReady() == false && device.screenSizeMatches(caps.getScreenSize())) {
        if (devicesInUse.contains(device)) {
          continue;
        }
        devicesInUse.add(device);
        return device;
      }
    }
    throw new DeviceStoreException("No devices are found. "
        + "This can happen if the devices are in use or no device screen "
        + "matches the required capabilities.");
  }

  /**
   * For testing only
   */
  /* package */List<AndroidDevice> getDevicesInUse() {
    return devicesInUse;
  }

  /**
   * For testing only
   */
  /* package */Map<DeviceTargetPlatform, List<AndroidDevice>> getDevicesList() {
    return androidDevices;
  }
}
