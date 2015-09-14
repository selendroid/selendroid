/*
 * Copyright 2013-2014 eBay Software Foundation and selendroid committers.
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

import io.selendroid.standalone.android.AndroidDevice;
import io.selendroid.standalone.android.HardwareDeviceListener;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.exceptions.DeviceStoreException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultHardwareDeviceListener implements HardwareDeviceListener {
  private static final Logger log = Logger.getLogger(DefaultHardwareDeviceListener.class.getName());
  private DefaultDeviceStore store = null;
  private SelendroidStandaloneDriver driver;

  public DefaultHardwareDeviceListener(DefaultDeviceStore store, SelendroidStandaloneDriver driver) {
    this.store = store;
    this.driver = driver;
  }


  @Override
  public void onDeviceConnected(AndroidDevice device) {
    try {
      store.addDevice(device);
    } catch (AndroidDeviceException e) {
      log.log(Level.WARNING, "Could not add device to store", e);
    }
  }

  @Override
  public void onDeviceDisconnected(AndroidDevice device) {
    try {
      // if there is an active session on the device,
      // mark it as invalid.
      ActiveSession session = driver.findActiveSession(device);
      if (session != null) {
        session.invalidate();
      }

      // remove device from store
      store.removeAndroidDevice(device);
    } catch (DeviceStoreException e) {
      log.severe("The device cannot be removed: " + e.getMessage());
    }
  }

  @Override
  public void onDeviceChanged(AndroidDevice device) {
    try {
      store.updateDevice(device);
    } catch (AndroidDeviceException e) {
      log.log(Level.WARNING, "Could not update device.", e);
    }
  }
}
