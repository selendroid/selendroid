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
package io.selendroid.standalone.server.model;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
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
import java.util.Collections;
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
  private boolean clearData = true;
  private boolean keepEmulator = false;
  private AndroidEmulatorPowerStateListener emulatorPowerStateListener = null;
  private DeviceManager deviceManager = null;

  public DeviceStore(Integer emulatorPort, DeviceManager deviceManager) {
    this.deviceManager = deviceManager;
    androidEmulatorPortFinder = new DefaultPortFinder(emulatorPort, emulatorPort + 30);
  }

  public DeviceStore(EmulatorPortFinder androidEmulatorPortFinder,
                     DeviceManager deviceManager) {
    this.deviceManager = deviceManager;
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

  /* package */void initAndroidDevices(HardwareDeviceListener hardwareDeviceListener,
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
      log.info("Adding: " + androidDevice);
      addDeviceToStore(androidDevice);
    }
  }

  public synchronized void updateDevice(AndroidDevice device) throws AndroidDeviceException {
    boolean deviceRemoved = false;
    for (DeviceTargetPlatform targetPlatform : androidDevices.keySet()) {
      List<AndroidDevice> platformDevices = androidDevices.get(targetPlatform);
      // Attempt to remove the device from this target platform;
      deviceRemoved |= platformDevices.remove(device);
    }

    if (deviceRemoved) {
      addDeviceToStore(device);
    } else {
      log.warning("Attempted to update device which did could not be found in the device store");
    }
  }

  public void addEmulators(List<AndroidEmulator> emulators) throws AndroidDeviceException {
    if (emulators == null || emulators.isEmpty()) {
      log.info("No emulators has been found.");
      return;
    }
    for (AndroidEmulator emulator : emulators) {
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
      List<AndroidDevice> platformDevices = androidDevices.get(device.getTargetPlatform());
      if (!platformDevices.contains(device)) {
        platformDevices.add(device);
      }
    } else {
      androidDevices.put(device.getTargetPlatform(), Lists.newArrayList(device));
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
  public synchronized AndroidDevice findAndroidDevice(SelendroidCapabilities caps) throws DeviceStoreException {

    Preconditions.checkArgument(caps != null, "Error: capabilities are null");

    if (androidDevices.isEmpty()) {
      throw new DeviceStoreException("Fatal Error: Device Store does not contain any Android Device.");
    }

    String platformVersion = caps.getPlatformVersion();

    Iterable<AndroidDevice> candidateDevices = Strings.isNullOrEmpty(platformVersion) ?
        Iterables.concat(androidDevices.values()) : androidDevices.get(DeviceTargetPlatform.fromPlatformVersion(platformVersion));

    candidateDevices = Objects.firstNonNull(candidateDevices, Collections.EMPTY_LIST);

    FluentIterable<AndroidDevice> allMatchingDevices = FluentIterable.from(candidateDevices)
        .filter(deviceNotInUse())
        .filter(deviceSatisfiesCapabilities(caps));

    if (!allMatchingDevices.isEmpty()) {

      AndroidDevice matchingDevice = allMatchingDevices.filter(deviceRunning()).first()
          .or(allMatchingDevices.first()).get();

      if (!deviceRunning().apply(matchingDevice)) {
        log.info("Using potential match: " + matchingDevice);
      }

      devicesInUse.add(matchingDevice);
      return matchingDevice;

    } else {
      throw new DeviceStoreException("No devices are found. "
          + "This can happen if the devices are in use or no device screen "
          + "matches the required capabilities.");
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

  public void setClearData(boolean clearData) {
    this.clearData = clearData;
  }

  public void setKeepEmulator(boolean keepEmulator) {
    this.keepEmulator = keepEmulator;
  }

  private Predicate<AndroidDevice> deviceNotInUse() {
    return new Predicate<AndroidDevice>() {
      @Override
      public boolean apply(AndroidDevice candidate) {
        return !devicesInUse.contains(candidate);
      }
    };
  }

  private Predicate<AndroidDevice> deviceSatisfiesCapabilities(final SelendroidCapabilities capabilities) {
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

  private Predicate<AndroidDevice> deviceRunning() {
    return new Predicate<AndroidDevice>() {
      @Override
      public boolean apply(AndroidDevice candidate) {
        return !(candidate instanceof DefaultAndroidEmulator && !((DefaultAndroidEmulator) candidate).isEmulatorStarted());
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
