package io.selendroid.common.device;

import io.selendroid.common.device.DeviceTargetPlatform;

import org.junit.Assert;
import org.junit.Test;

public class DeviceTargetPlatformTest {
  @Test
  public void shouldLoadFromNumberValue() {
    DeviceTargetPlatform platform = DeviceTargetPlatform.fromPlatformVersion("17");
    Assert.assertEquals(DeviceTargetPlatform.ANDROID17, platform);
  }
  @Test
  public void shouldLoadFromStringAndNumberValue() {
    DeviceTargetPlatform platform = DeviceTargetPlatform.fromPlatformVersion("ANDROID17");
    Assert.assertEquals(DeviceTargetPlatform.ANDROID17, platform);
  }
}
