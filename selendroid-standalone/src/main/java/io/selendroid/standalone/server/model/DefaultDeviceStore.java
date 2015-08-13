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
 */
package io.selendroid.standalone.server.model;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.common.device.DeviceTargetPlatform;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.AndroidDevice;
import io.selendroid.standalone.android.AndroidEmulator;
import io.selendroid.standalone.android.AndroidEmulatorPowerStateListener;
import io.selendroid.standalone.android.DeviceManager;
import io.selendroid.standalone.android.HardwareDeviceListener;
import io.selendroid.standalone.android.impl.DefaultAndroidEmulator;
import io.selendroid.standalone.android.impl.DefaultHardwareDevice;
import io.selendroid.standalone.android.impl.InstalledAndroidApp;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.standalone.exceptions.DeviceStoreException;
import io.selendroid.standalone.server.model.impl.DefaultPortFinder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultDeviceStore extends AbstractDeviceStore {
  private static final Logger log = Logger.getLogger(DefaultDeviceStore.class.getName());
  private AndroidEmulatorPowerStateListener emulatorPowerStateListener = null;
  private DeviceManager deviceManager = null;

  public DefaultDeviceStore(Integer emulatorPort, DeviceManager deviceManager) {
    this.deviceManager = deviceManager;
    androidEmulatorPortFinder = new DefaultPortFinder(emulatorPort, emulatorPort + 30);
  }

  public DefaultDeviceStore(EmulatorPortFinder androidEmulatorPortFinder,
                     DeviceManager deviceManager) {
    this.deviceManager = deviceManager;
    this.androidEmulatorPortFinder = androidEmulatorPortFinder;
  }

  /**
   * After a test session a device should be released. That means id will be removed from the list
   * of devices in use and in case of an emulator it will be stopped.
   *
   * @param device The device to release
   * @see {@link #findAndroidDevice(SelendroidCapabilities)}
   */
  public void release(AndroidDevice device, AndroidApp aut) {
    log.info("Releasing device " + device);
    if (devicesInUse.contains(device)) {

      if (aut != null) {
        // stop the app anyway - better in case people do use snapshots
        try {
          device.kill(aut);
        } catch (Exception e) {
          log.log(Level.WARNING, "Failed to kill android application when releasing device", e);
        }

        if (clearData) {
          try {
            device.clearUserData(aut);
          } catch (AndroidSdkException e) {
            log.log(Level.WARNING, "Failed to clear user data of application", e);
          }
        }
      }

      if (device instanceof AndroidEmulator && !(aut instanceof InstalledAndroidApp) && !keepEmulator) {
        AndroidEmulator emulator = (AndroidEmulator) device;
        try {
          emulator.stop();
        } catch (AndroidDeviceException e) {
          log.severe("Failed to stop emulator: " + e.getMessage());
        }
        androidEmulatorPortFinder.release(emulator.getPort());
      }
      devicesInUse.remove(device);
    }
  }

  /* package */
  @Override
  public void initAndroidDevices(HardwareDeviceListener hardwareDeviceListener,
                                 boolean shouldKeepAdbAlive) throws AndroidDeviceException {
    emulatorPowerStateListener = new DefaultEmulatorPowerStateListener();
    deviceManager.initialize(hardwareDeviceListener, emulatorPowerStateListener);

    List<AndroidEmulator> emulators = DefaultAndroidEmulator.listAvailableAvds();
    addEmulators(emulators);

    if (getDevices().isEmpty()) {
      SelendroidException e =
              new SelendroidException(
                      "No android virtual devices were found. "
                              + "Please start the android tool and create emulators and restart the selendroid-standalone "
                              + "or plugin an Android hardware device via USB.");
      log.warning("Warning: " + e);
    }
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

    release(device, null);
    DeviceTargetPlatform apiLevel = device.getTargetPlatform();
    if (androidDevices.containsKey(apiLevel)) {
      log.info("Removing: " + device);
      androidDevices.get(apiLevel).remove(device);
      if (androidDevices.get(apiLevel).isEmpty()) {
        androidDevices.remove(apiLevel);
      }
    } else {
      for (List<AndroidDevice> targetDevices : androidDevices.values()) {
        if (targetDevices.contains(device)) {
          log.warning("Device in devicestore");
        }
      }
      log.warning("The target platform version of the device is not found in device store.");
      log.warning("The device was propably already removed.");
    }
  }

  @Override
  protected Predicate<AndroidDevice> deviceSatisfiesCapabilities(final SelendroidCapabilities capabilities) {
    return new Predicate<AndroidDevice>() {
      @Override
      public boolean apply(AndroidDevice candidate) {
        ArrayList<Boolean> booleanExpressions = Lists.newArrayList(
                candidate.screenSizeMatches(capabilities.getScreenSize()),
                capabilities.getEmulator() == null ? true : capabilities.getEmulator() ?
                        candidate instanceof DefaultAndroidEmulator : candidate instanceof DefaultHardwareDevice,
                StringUtils.isNotBlank(capabilities.getSerial()) ? capabilities.getSerial().equals(candidate.getSerial()) : true,
                StringUtils.isNotBlank(capabilities.getModel()) ? candidate.getModel().contains(capabilities.getModel()) : true,
                StringUtils.isNotBlank(capabilities.getAPITargetType()) ? candidate.getAPITargetType() != null
                        && candidate.getAPITargetType().contains(capabilities.getAPITargetType()) : true
        );

        return Iterables.all(booleanExpressions, Predicates.equalTo(true));
      }
    };
  }

  class DefaultEmulatorPowerStateListener implements AndroidEmulatorPowerStateListener {

    @Override
    public void onDeviceStarted(String avdName, String serial) {
      AndroidEmulator emulator = findEmulator(avdName);
      if (emulator != null) {
        Integer port = Integer.parseInt(serial.replace("emulator-", ""));
        emulator.setSerial(port);
        emulator.setWasStartedBySelendroid(false);
      }
    }

    AndroidEmulator findEmulator(String avdName) {
      for (AndroidDevice device : getDevices()) {
        if (device instanceof AndroidEmulator) {
          AndroidEmulator emulator = (AndroidEmulator) device;
          if (avdName.equals(emulator.getAvdName())) {
            return emulator;
          }
        }
      }
      return null;
    }

    @Override
    public void onDeviceStopped(String avdName) {
      // do nothing
    }
  }
}
