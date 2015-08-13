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
package io.selendroid.standalone.server.model;

import com.android.ddmlib.IDevice;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.common.device.DeviceTargetPlatform;
import io.selendroid.standalone.android.AndroidEmulator;
import io.selendroid.standalone.android.DeviceManager;
import io.selendroid.standalone.android.impl.AbstractAndroidDeviceEmulator;
import io.selendroid.standalone.android.impl.DefaultAndroidEmulator;
import io.selendroid.standalone.android.impl.DefaultHardwareDevice;
import io.selendroid.standalone.exceptions.AndroidDeviceException;

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

  protected static DefaultHardwareDevice anDevice(String serial, Map<String, String> prop)
      throws AndroidDeviceException {
    IDevice device = mock(IDevice.class);
    when(device.getSerialNumber()).thenReturn(serial);

    return new FakeHardwareDevice(device, prop);
  }

  protected static AbstractAndroidDeviceEmulator anEmulator(String name, DeviceTargetPlatform platform,
      boolean isEmulatorStarted, String apiTargetType) throws AndroidDeviceException {
    AbstractAndroidDeviceEmulator emulator = mock(AbstractAndroidDeviceEmulator.class);
    when(emulator.getAvdName()).thenReturn(name);
    when(emulator.getModel()).thenReturn("Nexus 5");
    when(emulator.getTargetPlatform()).thenReturn(platform);
    when(emulator.isEmulatorStarted()).thenReturn(isEmulatorStarted);
    when(emulator.isDeviceReady()).thenReturn(false);
    when(emulator.screenSizeMatches("320x480")).thenReturn(true);
    when(emulator.getAPITargetType()).thenReturn(apiTargetType);

    return emulator;
  }

  protected static SelendroidCapabilities withDefaultCapabilities() {
    SelendroidCapabilities capabilities = new SelendroidCapabilities();
    capabilities.setPlatformVersion(DeviceTargetPlatform.ANDROID16);
    capabilities.setScreenSize("320x480");
    return capabilities;
  }

  protected static SelendroidCapabilities withModelCapabilities() {
    SelendroidCapabilities capabilities = new SelendroidCapabilities();
    capabilities.setPlatformVersion(DeviceTargetPlatform.ANDROID16);
    capabilities.setModel("Nexus 5");
    capabilities.setScreenSize("320x480");
    return capabilities;
  }

  protected static SelendroidCapabilities withWrongModelCapabilities() {
    SelendroidCapabilities capabilities = new SelendroidCapabilities();
    capabilities.setPlatformVersion(DeviceTargetPlatform.ANDROID16);
    capabilities.setModel("Nexus 7");
    capabilities.setScreenSize("320x480");
    return capabilities;
  }
  
  protected static SelendroidCapabilities withGoogleAPITypeCapabilities() {
    SelendroidCapabilities capabilities = new SelendroidCapabilities();
    capabilities.setPlatformVersion(DeviceTargetPlatform.ANDROID16);
    capabilities.setAPITargetType("google");
    capabilities.setScreenSize("320x480");
    return capabilities;
  }

  protected static DeviceManager anDeviceManager() throws AndroidDeviceException {
    DeviceManager finder = mock(DeviceManager.class);
    when(finder.getVirtualDevice("emulator-5554")).thenReturn(null);

    return finder;
  }

  static class FakeHardwareDevice extends DefaultHardwareDevice {
    private Map<String, String> prop;
    public FakeHardwareDevice(IDevice device, Map<String, String> prop) {
      super(device);
      this.prop = prop;
    }
    @Override
    public String getProp(String name) {
      return prop.get(name);
    }
  }
}
