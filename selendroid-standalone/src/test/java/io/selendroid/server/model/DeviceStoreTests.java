package io.selendroid.server.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.selendroid.android.AndroidEmulator;
import io.selendroid.android.impl.DefaultAndroidEmulator;
import io.selendroid.exceptions.AndroidDeviceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selendroid.device.DeviceTargetPlatform;
import org.openqa.selendroid.exceptions.SelendroidException;

/**
 * @author ddary
 */
public class DeviceStoreTests {
  public static final String ANY_STRING = "ANY";

  @Test
  public void testShouldBeAbleToRegisterSingleNotStatedEmulator() throws Exception {
    AndroidEmulator emulator = anEmulator("de", DeviceTargetPlatform.ANDROID10, false);

    DeviceStore deviceStore = new DeviceStore();
    deviceStore.addEmulators(Collections.singletonList(emulator));
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 0);
    Assert.assertEquals(deviceStore.getDevicesList().size(), 1);

    Assert.assertTrue(deviceStore.getDevicesList().containsKey(DeviceTargetPlatform.ANDROID10));
    Assert.assertTrue(deviceStore.getDevicesList().get(DeviceTargetPlatform.ANDROID10)
        .contains(emulator));
  }

  @Test
  public void testShouldBeAbleToRegisterMultipleNotStatedEmulators() throws Exception {
    AndroidEmulator deEmulator10 = anEmulator("de", DeviceTargetPlatform.ANDROID10, false);
    AndroidEmulator enEmulator10 = anEmulator("en", DeviceTargetPlatform.ANDROID10, false);
    AndroidEmulator deEmulator16 = anEmulator("de", DeviceTargetPlatform.ANDROID16, false);

    DeviceStore deviceStore = new DeviceStore();
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator10, enEmulator10,
        deEmulator16}));
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 0);
    // Expecting two entries for two android target platforms
    Assert.assertEquals(deviceStore.getDevicesList().size(), 2);

    Assert.assertTrue(deviceStore.getDevicesList().containsKey(DeviceTargetPlatform.ANDROID10));
    Assert.assertTrue(deviceStore.getDevicesList().get(DeviceTargetPlatform.ANDROID10)
        .contains(deEmulator10));
    Assert.assertTrue(deviceStore.getDevicesList().get(DeviceTargetPlatform.ANDROID10)
        .contains(enEmulator10));
    Assert.assertTrue(deviceStore.getDevicesList().get(DeviceTargetPlatform.ANDROID16)
        .contains(deEmulator16));
  }

  @Test
  public void testShouldBeAbleToRegisterMSingleNotStarteEmulatorAndSkipOthers() throws Exception {
    AndroidEmulator deEmulator10 = anEmulator("de", DeviceTargetPlatform.ANDROID10, false);
    AndroidEmulator enEmulator10 = anEmulator("en", DeviceTargetPlatform.ANDROID10, true);
    AndroidEmulator deEmulator16 = anEmulator("de", DeviceTargetPlatform.ANDROID16, true);

    DeviceStore deviceStore = new DeviceStore();
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator10, enEmulator10,
        deEmulator16}));
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 0);
    Assert.assertEquals(deviceStore.getDevicesList().size(), 1);

    Assert.assertTrue(deviceStore.getDevicesList().containsKey(DeviceTargetPlatform.ANDROID10));
    Assert.assertTrue(deviceStore.getDevicesList().get(DeviceTargetPlatform.ANDROID10)
        .contains(deEmulator10));
  }

  @Test
  public void testShouldBeAbleToInitStoreWhenAllEmulatorsAreRunningAlready() throws Exception {
    AndroidEmulator enEmulator10 = anEmulator("en", DeviceTargetPlatform.ANDROID10, true);
    AndroidEmulator deEmulator16 = anEmulator("de", DeviceTargetPlatform.ANDROID16, true);

    DeviceStore deviceStore = new DeviceStore();
    try {
      deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {enEmulator10, deEmulator16}));
      Assert.fail();
    } catch (SelendroidException e) {
      // expected
      Assert.assertEquals(deviceStore.getDevicesList().size(), 0);
    }
  }

  @Test
  public void storeShouldThrowAnExceptionIfInitializedWithEmptyList() throws AndroidDeviceException {
    DeviceStore store = new DeviceStore();
    try {
      store.addEmulators(new ArrayList<AndroidEmulator>());
      Assert.fail();
    } catch (SelendroidException e) {
      // expected
      Assert.assertEquals(store.getDevicesList().size(), 0);
    }
  }

  @Test
  public void storeShouldThrowAnExceptionIfInitializedWithNull() throws AndroidDeviceException {
    DeviceStore store = new DeviceStore();
    try {
      store.addEmulators(null);
      Assert.fail();
    } catch (SelendroidException e) {
      // expected
      Assert.assertEquals(store.getDevicesList().size(), 0);
    }
  }

  private AndroidEmulator anEmulator(String name, DeviceTargetPlatform platform,
      boolean isEmulatorStarted) throws AndroidDeviceException {
    AndroidEmulator emulator = mock(DefaultAndroidEmulator.class);
    when(emulator.getAvdName()).thenReturn(name);
    when(emulator.getTargetPlatform()).thenReturn(platform);
    when(emulator.isEmulatorStarted()).thenReturn(isEmulatorStarted);

    return emulator;
  }
}
