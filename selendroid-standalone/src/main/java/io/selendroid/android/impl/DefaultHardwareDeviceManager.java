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
package io.selendroid.android.impl;

import io.selendroid.android.AndroidDevice;
import io.selendroid.android.HardwareDeviceListener;
import io.selendroid.android.HardwareDeviceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log;

public class DefaultHardwareDeviceManager extends Thread
    implements
      IDeviceChangeListener,
      HardwareDeviceManager {
  private static final Logger log = Logger.getLogger(DefaultHardwareDeviceManager.class.getName());
  private String adbPath;
  private List<HardwareDeviceListener> deviceListeners = new ArrayList<HardwareDeviceListener>();
  private Map<IDevice, DefaultHardwareDevice> connectedDevices =
      new HashMap<IDevice, DefaultHardwareDevice>();
  private AndroidDebugBridge bridge;

  public DefaultHardwareDeviceManager(String adbPath) {
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

    // Add the existing devices to the list of devices we are tracking.
    if (bridge.isConnected() && bridge.hasInitialDeviceList()) {
      IDevice[] devices = bridge.getDevices();

      for (int i = 0; i < devices.length; i++) {
        connectedDevices.put(devices[i], new DefaultHardwareDevice(devices[i]));
      }
    }

    AndroidDebugBridge.addDeviceChangeListener(this);
  }

  /**
   * Shutdown the AndroidDebugBridge and clean up all connected devices.
   */
  public void shutdown() {
    for (HardwareDeviceListener listener : deviceListeners) {
      for (AndroidDevice device : connectedDevices.values()) {
        listener.onDeviceDisconnected(connectedDevices.get(device));
      }
    }

    AndroidDebugBridge.removeDeviceChangeListener(this);
    AndroidDebugBridge.terminate();
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
    if (device == null || device.isEmulator()) {
      return;
    }
    for (HardwareDeviceListener listener : deviceListeners) {
      listener.onDeviceDisconnected(connectedDevices.get(device));
    }
    connectedDevices.put(device, new DefaultHardwareDevice(device));
  }

  @Override
  public void deviceDisconnected(IDevice device) {
    if (device == null || connectedDevices.containsKey(device) == false || device.isEmulator()) {
      return;
    }
    for (HardwareDeviceListener listener : deviceListeners) {
      listener.onDeviceDisconnected(connectedDevices.get(device));
    }
    connectedDevices.remove(device);
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
}
