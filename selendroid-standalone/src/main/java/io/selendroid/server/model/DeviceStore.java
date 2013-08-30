/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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
import io.selendroid.android.AndroidApp;
import io.selendroid.android.AndroidDevice;
import io.selendroid.android.AndroidEmulator;
import io.selendroid.android.impl.DefaultAndroidEmulator;
import io.selendroid.android.impl.DefaultHardwareDevice;
import io.selendroid.android.impl.InstalledAndroidApp;
import io.selendroid.device.DeviceTargetPlatform;
import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.exceptions.DeviceStoreException;
import io.selendroid.server.model.impl.DefaultPortFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceStore {
  private static final Logger log = Logger.getLogger(DeviceStore.class.getName());
  private List<AndroidDevice> devicesInUse = new ArrayList<AndroidDevice>();
  private Map<DeviceTargetPlatform, List<AndroidDevice>> androidDevices =
      new HashMap<DeviceTargetPlatform, List<AndroidDevice>>();
  private EmulatorPortFinder androidEmulatorPortFinder = null;
  private Boolean installedApp = false;

  public DeviceStore(Boolean debug, Integer emulatorPort) {
    if (debug) {
      log.setLevel(Level.FINE);
    }
    androidEmulatorPortFinder = new DefaultPortFinder(emulatorPort, emulatorPort + 30);
  }

  public DeviceStore(EmulatorPortFinder androidEmulatorPortFinder, Boolean debug) {
    if (debug) {
      log.setLevel(Level.FINE);
    }
    this.androidEmulatorPortFinder = androidEmulatorPortFinder;
  }

  public Integer nextEmulatorPort() {
    return androidEmulatorPortFinder.next();
  }

  /**
   * After a test session a device should be released. That means id will be removed from the list
   * of devices in use and in case of an emulator it will be stopped.
   * 
   * @param device The device to release
   * @throws AndroidDeviceException
   * @see {@link #findAndroidDevice(SelendroidCapabilities)}
   */
  public void release(AndroidDevice device, AndroidApp aut) throws AndroidDeviceException {
    if (devicesInUse.contains(device)) {
      if (device instanceof AndroidEmulator) {
        if (installedApp) {
          // kill process instead of shutting down emulator
          try {
            ((AndroidEmulator) device).kill((InstalledAndroidApp) aut);
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          AndroidEmulator emulator = (AndroidEmulator) device;
          emulator.stop();
          androidEmulatorPortFinder.release(emulator.getPort());
        }
      }
      devicesInUse.remove(device);
    }
  }

  public synchronized void addDevice(AndroidDevice androidDevice) throws AndroidDeviceException {
    if (androidDevice == null) {
      log.info("No Android devices were found.");
      return;
    }
    if (androidDevice instanceof AndroidEmulator) {
      throw new AndroidDeviceException(
          "For adding emulator instances please use #addEmulator method.");
    }
    if (androidDevice.isDeviceReady() == true) {
      System.out.println("Adding: " + androidDevice);
      addDeviceToStore(androidDevice);
    }
  }

  public void addEmulators(List<AndroidEmulator> emulators) throws AndroidDeviceException {
    addEmulators(emulators, false);
  }

  public void addEmulators(List<AndroidEmulator> emulators, Boolean installedApp)
      throws AndroidDeviceException {
    this.installedApp = installedApp;
    if (emulators == null || emulators.isEmpty()) {
      log.info("No emulators has been found.");
      return;
    }
    for (AndroidEmulator emulator : emulators) {
      if (emulator.isEmulatorStarted()) {
        if (!installedApp) {
          log.info("Skipping emulator because it is already in use: " + emulator);
          continue;
        }
      }

      log.info("Adding: " + emulator);
      addDeviceToStore((AndroidDevice) emulator);
    }
  }

  /**
   * Internal method to add an actual device to the store.
   * 
   * @param device The device to add.
   * @throws AndroidDeviceException
   */
  protected synchronized void addDeviceToStore(AndroidDevice device) throws AndroidDeviceException {
    if (androidDevices.containsKey(device.getTargetPlatform())) {
      if (androidDevices.get(device.getTargetPlatform()) == null) {
        androidDevices.put(device.getTargetPlatform(), new ArrayList<AndroidDevice>());
      }
      androidDevices.get(device.getTargetPlatform()).add((AndroidDevice) device);
    } else {
      List<AndroidDevice> devices = new ArrayList<AndroidDevice>();
      devices.add((AndroidDevice) device);
      androidDevices.put(device.getTargetPlatform(), devices);
    }
  }

  /**
   * Finds a device for the requested capabilities. <b>important note:</b> if the device is not any
   * longer used, call the {@link #release(AndroidDevice, AndroidApp)} method.
   * 
   * @param caps The desired test session capabilities.
   * @return Matching device for a test session.
   * @throws DeviceStoreException
   * @see {@link #release(AndroidDevice, AndroidApp)}
   */
  public synchronized AndroidDevice findAndroidDevice(SelendroidCapabilities caps)
      throws DeviceStoreException {
    if (caps == null) {
      throw new IllegalArgumentException("Error: capabilities are null");
    }
    if (androidDevices.isEmpty()) {
      throw new DeviceStoreException(
          "Fatal Error: Device Store does not contain any Android Device.");
    }
    String androidTarget = caps.getAndroidTarget();
    List<AndroidDevice> devices = null;
    if (androidTarget == null || androidTarget.isEmpty()) {
      devices = new ArrayList<AndroidDevice>();
      for (List<AndroidDevice> list : androidDevices.values()) {
        devices.addAll(list);
      }
    } else {
      DeviceTargetPlatform platform = DeviceTargetPlatform.valueOf(androidTarget);
      devices = androidDevices.get(platform);
    }
    if (devices == null) {
      devices = new ArrayList<AndroidDevice>();
    }

    // keep a list of emulators that aren't started to be used as backup
    // when installedApp is used, want to default to the already running emulator
    List<AndroidDevice> potentialMatches = new ArrayList<AndroidDevice>();
    for (AndroidDevice device : devices) {
      log.fine("Evaluating if this device is a match for us: " + device.toString());
      if (isEmulatorSwitchedOff(device) && device.screenSizeMatches(caps.getScreenSize())) {
        if (devicesInUse.contains(device)) {
          log.fine("Device is in use.");
          continue;
        }
        if (caps.getEmulator() == null
            || (caps.getEmulator() == true && device instanceof DefaultAndroidEmulator)
            || (caps.getEmulator() == false && device instanceof DefaultHardwareDevice)) {
          if (installedApp && device instanceof AndroidEmulator) {
            potentialMatches.add(device);
            continue;
          }
          log.fine("device found.");
          devicesInUse.add(device);
          return device;
        }
      } else if (installedApp) {
        if (devicesInUse.contains(device)) {
          log.fine("device already in use");
          continue;
        }
        devicesInUse.add(device);
        return device;
      } else {
        log.info("emulator switched off: " + isEmulatorSwitchedOff(device));
      }
    }
    if (potentialMatches.size() > 0) {
      devicesInUse.add(potentialMatches.get(0));
      return potentialMatches.get(0);
    }
    throw new DeviceStoreException("No devices are found. "
        + "This can happen if the devices are in use or no device screen "
        + "matches the required capabilities.");
  }

  private boolean isEmulatorSwitchedOff(AndroidDevice device) throws DeviceStoreException {
    if (device instanceof AndroidEmulator) {
      try {
        return !((AndroidEmulator) device).isEmulatorStarted();
      } catch (AndroidDeviceException e) {
        throw new DeviceStoreException(e);
      }
    }
    return true;
  }

  public List<AndroidDevice> getDevices() {
    List<AndroidDevice> devices = new ArrayList<AndroidDevice>();
    for (Map.Entry<DeviceTargetPlatform, List<AndroidDevice>> entry : androidDevices.entrySet()) {
      devices.addAll(entry.getValue());
    }
    return devices;
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

  /**
   * Removes the given device from store so that it cannot be any longer be used for testing. This
   * can happen if e.g. the hardware device gets unplugged from the computer.
   * 
   * @param device the device to remove.
   * @throws DeviceStoreException when parameter is not type of 'DefaultHardwareDevice'.
   */
  public void removeAndroidDevice(AndroidDevice device) throws DeviceStoreException {
    if (device == null) {
      return;
    }
    boolean hardwareDevice = device instanceof DefaultHardwareDevice;
    if (hardwareDevice == false) {
      throw new DeviceStoreException("Only devices of type 'DefaultHardwareDevice' can be removed.");
    }
    try {
      release(device, null);
    } catch (AndroidDeviceException e) {
      throw new DeviceStoreException("An error occured while releasing device", e);
    }
    DeviceTargetPlatform apiLevel = device.getTargetPlatform();
    if (androidDevices.containsKey(apiLevel)) {
      log.info("Removing: " + device);
      androidDevices.get(apiLevel).remove(device);
      if (androidDevices.get(apiLevel).isEmpty()) {
        androidDevices.remove(apiLevel);
      }
    } else {
      log.warning("The target platform version of the device is not found in device store.");
      log.warning("The device was propably already removed.");
    }
  }
}
