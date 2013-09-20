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
import io.selendroid.android.DeviceManager;
import io.selendroid.android.HardwareDeviceListener;

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
  private Map<IDevice, DefaultHardwareDevice> connectedDevices =
      new HashMap<IDevice, DefaultHardwareDevice>();
  private Map<String, IDevice> virtualDevices = new HashMap<String, IDevice>();
  private AndroidDebugBridge bridge;

  public DefaultDeviceManager(String adbPath) {
    this.adbPath = adbPath;
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
      e.printStackTrace();
      Log.e("The IllegalStateException is not a show "
          + "stopper. It has been handled. This is just debug spew." + " Please proceed.", e);
    }

    bridge = AndroidDebugBridge.getBridge();

    if (bridge == null) {
      bridge = AndroidDebugBridge.createBridge(adbPath, false);
    }
    IDevice[] devicesss = bridge.getDevices();
    log.info("has initial list: " + bridge.hasInitialDeviceList());
    for (int i = 0; i < devicesss.length; i++) {
      System.out.println("my devices: " + devicesss[i].getAvdName());
    }
    AndroidDebugBridge.addDeviceChangeListener(this);
    // Add the existing devices to the list of devices we are tracking.
    if (hasDevices()) {
      IDevice[] devices = bridge.getDevices();

      for (int i = 0; i < devices.length; i++) {
        connectedDevices.put(devices[i], new DefaultHardwareDevice(devices[i]));
      }
    } else {
      long timeout = System.currentTimeMillis() + 10000;
      System.out.println("nothing there");
      while (hasDevices() == false && System.currentTimeMillis() >= timeout) {

      }
      System.out.println("after wait");
      if (hasDevices()) {
        System.out.println("devices found");
        IDevice[] devices = bridge.getDevices();

        for (int i = 0; i < devices.length; i++) {
          connectedDevices.put(devices[i], new DefaultHardwareDevice(devices[i]));
        }
      }
    }

  }

  private boolean hasDevices() {
    return bridge.isConnected() && bridge.hasInitialDeviceList();
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
    AndroidDebugBridge.terminate();
    log.info("stopping Device Manager");
    //TODO add thread interrupt and join handling
  }

  @Override
  public void deviceChanged(IDevice device, int changeMask) {
    // Only fire events if the phone properties are available
    if (IDevice.CHANGE_BUILD_INFO == changeMask && device.isEmulator() == false) {
      for (HardwareDeviceListener listener : deviceListeners) {
        listener.onDeviceConnected(connectedDevices.get(device));
      }
    }
  }

  @Override
  public void deviceConnected(IDevice device) {
    if (device == null) {
      return;
    }
    if (device.isEmulator()) {
      virtualDevices.put(device.getSerialNumber(), device);
    } else {
      for (HardwareDeviceListener listener : deviceListeners) {
        listener.onDeviceDisconnected(connectedDevices.get(device));
      }
      connectedDevices.put(device, new DefaultHardwareDevice(device));
    }
  }

  @Override
  public void deviceDisconnected(IDevice device) {
    if (device == null) {
      return;
    }
    if (device.isEmulator()) {
      virtualDevices.remove(device.getSerialNumber());
    } else if (connectedDevices.containsKey(device)) {
      for (HardwareDeviceListener listener : deviceListeners) {
        listener.onDeviceDisconnected(connectedDevices.get(device));
      }
      connectedDevices.remove(device);
    }
  }

  @Override
  public void registerListner(HardwareDeviceListener deviceListener) {
    this.deviceListeners.add(deviceListener);
  }

  @Override
  public void unregisterListener(HardwareDeviceListener deviceListener) {
    if (deviceListeners.contains(deviceListener)) {
      deviceListeners.remove(deviceListener);
    }
  }

  @Override
  public void initialize(HardwareDeviceListener defaultListener) {
    registerListner(defaultListener);
    initializeAdbConnection();
  }

  public IDevice getVirtualDevice(String serial) {
    return virtualDevices.get(serial);
  }
}
