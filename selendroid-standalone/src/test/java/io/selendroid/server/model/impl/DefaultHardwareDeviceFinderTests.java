package io.selendroid.server.model.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import io.selendroid.android.AndroidDevice;
import io.selendroid.device.DeviceTargetPlatform;
import io.selendroid.server.model.DeviceFinder;

import java.util.List;
import java.util.Locale;

import org.junit.Test;

public class DefaultHardwareDeviceFinderTests {
  @Test
  public void testShouldBeAbleTofindConnectedDevices() throws Exception {
    DeviceFinder finder = new DefaultHardwareDeviceFinder();
    List<AndroidDevice> devices = finder.findConnectedDevices();
    assertThat(devices, hasSize(1));
    AndroidDevice samsungGalaxyNexus = devices.get(0);
    assertThat(samsungGalaxyNexus.getScreenSize(), equalTo("720x1280"));
    System.out.println(samsungGalaxyNexus.getLocale());
    System.out.println(new Locale("de", "DE"));
    assertThat(samsungGalaxyNexus.getLocale(), equalTo(new Locale("de", "DE")));
    System.out.println(samsungGalaxyNexus.isDeviceReady());
    assertThat(samsungGalaxyNexus.isDeviceReady(), equalTo(true));
    assertThat(samsungGalaxyNexus.getTargetPlatform(), equalTo(DeviceTargetPlatform.ANDROID17));
  }
}
