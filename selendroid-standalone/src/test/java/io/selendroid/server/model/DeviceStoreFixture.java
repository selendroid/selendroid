/*
 * Copyright 2014 eBay Software Foundation and selendroid committers.
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.selendroid.SelendroidCapabilities;
import io.selendroid.android.DeviceManager;
import io.selendroid.android.impl.DefaultAndroidEmulator;
import io.selendroid.android.impl.DefaultHardwareDevice;
import io.selendroid.device.DeviceTargetPlatform;
import io.selendroid.exceptions.AndroidDeviceException;

public class DeviceStoreFixture {
  protected static DefaultHardwareDevice anDevice(String name, DeviceTargetPlatform platform)
      throws AndroidDeviceException {
    DefaultHardwareDevice device = mock(DefaultHardwareDevice.class);
    when(device.getModel()).thenReturn(name);
    when(device.getTargetPlatform()).thenReturn(platform);
    when(device.isDeviceReady()).thenReturn(true);
    when(device.screenSizeMatches("320x480")).thenReturn(true);

    return device;
  }

  protected static DefaultAndroidEmulator anEmulator(String name, DeviceTargetPlatform platform,
      boolean isEmulatorStarted) throws AndroidDeviceException {
    DefaultAndroidEmulator emulator = mock(DefaultAndroidEmulator.class);
    when(emulator.getAvdName()).thenReturn(name);
    when(emulator.getTargetPlatform()).thenReturn(platform);
    when(emulator.isEmulatorStarted()).thenReturn(isEmulatorStarted);
    when(emulator.isDeviceReady()).thenReturn(false);
    when(emulator.screenSizeMatches("320x480")).thenReturn(true);

    return emulator;
  }
  
  protected static SelendroidCapabilities withDefaultCapabilities() {
    SelendroidCapabilities capabilities = new SelendroidCapabilities();
    capabilities.setAndroidTarget(DeviceTargetPlatform.ANDROID16.name());
    capabilities.setScreenSize("320x480");
    return capabilities;
  }
  
  protected static DeviceManager anDeviceManager() throws AndroidDeviceException {
    DeviceManager finder = mock(DeviceManager.class);
    when(finder.getVirtualDevice("emulator-5554")).thenReturn(null);

    return finder;
  }
}
