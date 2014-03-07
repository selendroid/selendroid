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
package io.selendroid.android.impl;

import io.selendroid.android.AndroidDevice;
import io.selendroid.android.AndroidEmulatorPowerStateListener;
import io.selendroid.android.DeviceManager;
import io.selendroid.android.HardwareDeviceListener;
import io.selendroid.android.TelnetClient;
import io.selendroid.exceptions.AndroidDeviceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log;

public class DefaultDeviceManager extends Thread implements IDeviceChangeListener, DeviceManager {
  private static final Logger log = Logger.getLogger(DefaultDeviceManager.class.getName());
  private String adbPath;
  private List<HardwareDeviceListener> deviceListeners = new ArrayList<HardwareDeviceListener>();
  private List<AndroidEmulatorPowerStateListener> emulatorPowerStateListener =
      new ArrayList<AndroidEmulatorPowerStateListener>();
  private Map<IDevice, DefaultHardwareDevice> connectedDevices =
      new HashMap<IDevice, DefaultHardwareDevice>();
  private Map<String, IDevice> virtualDevices = new HashMap<String, IDevice>();
  private AndroidDebugBridge bridge;
  private boolean shouldKeepAdbAlive;

  public DefaultDeviceManager(String adbPath, boolean shouldKeepAdbAlive) {
    this.adbPath = adbPath;
    this.shouldKeepAdbAlive = shouldKeepAdbAlive;
  }

  /**
   * Initializes the AndroidDebugBridge and registers the DefaultHardwareDeviceManager with the
   * AndroidDebugBridge device change listener.
   */
  protected void initializeAdbConnection() {
    // Get a device bridge instance. Initialize, create and restart.
    try {
      AndroidDebugBridge.init(false);
    } catch (IllegalStateException e) {
      // When we keep the adb connection alive the AndroidDebugBridge may have been already
      // initialized at this point and it generates an exception. Do not print it.
      if (!shouldKeepAdbAlive) {
        e.printStackTrace();
        Log.e("The IllegalStateException is not a show "
            + "stopper. It has been handled. This is just debug spew. Please proceed.", e);
      }
    }

    bridge = AndroidDebugBridge.getBridge();

    if (bridge == null) {
      bridge = AndroidDebugBridge.createBridge(adbPath, false);
    }
    IDevice[] devices = bridge.getDevices();

    AndroidDebugBridge.addDeviceChangeListener(this);

    // Add the existing devices to the list of devices we are tracking.
    if (devices.length > 0) {
      for (int i = 0; i < devices.length; i++) {
        deviceConnected(devices[i]);
        log.info("my devices: " + devices[i].getAvdName());
      }
    } else {
      long timeout = System.currentTimeMillis() + 2000;
      while ((devices = bridge.getDevices()).length == 0 && System.currentTimeMillis() < timeout) {
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      if (devices.length > 0) {
        for (int i = 0; i < devices.length; i++) {
          deviceConnected(devices[i]);
          log.info("my devices: " + devices[i].getAvdName());
        }
      }
    }

  }

  /**
   * Shutdown the AndroidDebugBridge and clean up all connected devices.
   */
  public void shutdown() {
    log.info("Notifying device listener about shutdown");
    for (HardwareDeviceListener listener : deviceListeners) {
      for (AndroidDevice device : connectedDevices.values()) {
        listener.onDeviceDisconnected(connectedDevices.get(device));
      }
    }
    log.info("Removing Device Manager listener from ADB");
    AndroidDebugBridge.removeDeviceChangeListener(this);
    if (!shouldKeepAdbAlive) {
      AndroidDebugBridge.terminate();
    }
    log.info("stopping Device Manager");
    // TODO add thread interrupt and join handling
  }

  @Override
  public void deviceChanged(IDevice device, int changeMask) {
    // Only fire events if the phone properties are available
    if (IDevice.CHANGE_BUILD_INFO == changeMask) {
      if (device.isEmulator()) {

      } else {
        for (HardwareDeviceListener listener : deviceListeners) {
          listener.onDeviceConnected(connectedDevices.get(device));
        }
      }
    }
  }

  @Override
  public void deviceConnected(IDevice device) {
    if (device == null) {
      return;
    }
    if (device.isEmulator()) {
      String serial = device.getSerialNumber();
      Integer port = Integer.parseInt(serial.replace("emulator-", ""));
      String avdName = null;
      TelnetClient client = null;
      try {
        client = new TelnetClient(port);
        avdName = client.sendCommand("avd name");
      } catch (AndroidDeviceException e) {
        // ignore
      } finally {
        client.close();
      }
      virtualDevices.put(avdName, device);
      for (AndroidEmulatorPowerStateListener listener : emulatorPowerStateListener) {
        listener.onDeviceStarted(avdName, device.getSerialNumber());
      }
    } else {
      connectedDevices.put(device, new DefaultHardwareDevice(device));
      for (HardwareDeviceListener listener : deviceListeners) {
        listener.onDeviceConnected(connectedDevices.get(device));
      }
    }
  }

  @Override
  public void deviceDisconnected(IDevice device) {
    if (device == null) {
      return;
    }
    if (device.isEmulator()) {
      virtualDevices.remove(device.getAvdName());
      for (AndroidEmulatorPowerStateListener listener : emulatorPowerStateListener) {
        listener.onDeviceStopped(device.getSerialNumber());
      }
    } else if (connectedDevices.containsKey(device)) {
      for (HardwareDeviceListener listener : deviceListeners) {
        listener.onDeviceDisconnected(connectedDevices.get(device));
      }
      connectedDevices.remove(device);
    }
  }

  @Override
  public void registerListener(HardwareDeviceListener deviceListener) {
    this.deviceListeners.add(deviceListener);
  }

  @Override
  public void unregisterListener(HardwareDeviceListener deviceListener) {
    if (deviceListeners.contains(deviceListener)) {
      deviceListeners.remove(deviceListener);
    }
  }

  @Override
  public void registerListener(AndroidEmulatorPowerStateListener deviceListener) {
    this.emulatorPowerStateListener.add(deviceListener);
  }

  @Override
  public void unregisterListener(AndroidEmulatorPowerStateListener deviceListener) {
    if (emulatorPowerStateListener.contains(deviceListener)) {
      emulatorPowerStateListener.remove(deviceListener);
    }
  }

  @Override
  public void initialize(HardwareDeviceListener defaultHardwareListener,
      AndroidEmulatorPowerStateListener emulatorListener) {
    registerListener(defaultHardwareListener);
    registerListener(emulatorListener);
    initializeAdbConnection();
  }

  public IDevice getVirtualDevice(String avdName) {
    return virtualDevices.get(avdName);
  }
}
