package io.selendroid.server.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.selendroid.android.AndroidDevice;
import io.selendroid.android.AndroidEmulator;
import io.selendroid.android.impl.DefaultAndroidEmulator;
import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.exceptions.DeviceStoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selendroid.SelendroidCapabilities;
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
  public void testShouldBeAbleToIncrementEmulatorPortsByTwo() {
    DeviceStore deviceStore = new DeviceStore();
    Assert.assertEquals(5554, deviceStore.nextEmulatorPort().intValue());
    Assert.assertEquals(5556, deviceStore.nextEmulatorPort().intValue());
    Assert.assertEquals(5558, deviceStore.nextEmulatorPort().intValue());
  }

  @Test
  public void testShouldBeAbleToReleaseActiveEmulators() throws Exception {
    AndroidEmulator deEmulator = anEmulator("de", DeviceTargetPlatform.ANDROID16, false);


    DeviceStore deviceStore = new DeviceStore();
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator}));
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 0);
    AndroidDevice foundDevice = deviceStore.findAndroidDevice(withDefaultCapabilities());
    Assert.assertEquals(deEmulator, foundDevice);
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 1);
    deviceStore.release(foundDevice);
    // make sure the emulator has been stopped
    verify(deEmulator, times(1)).stopEmulator();
    // Make sure the device has been removed from devices in use
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 0);
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

  private DefaultAndroidEmulator anEmulator(String name, DeviceTargetPlatform platform,
      boolean isEmulatorStarted) throws AndroidDeviceException {
    DefaultAndroidEmulator emulator = mock(DefaultAndroidEmulator.class);
    when(emulator.getAvdName()).thenReturn(name);
    when(emulator.getTargetPlatform()).thenReturn(platform);
    when(emulator.isEmulatorStarted()).thenReturn(isEmulatorStarted);
    when(emulator.isDeviceReady()).thenReturn(false);
    when(emulator.screenSizeMatches("320x480")).thenReturn(true);

    return emulator;
  }

  @Test
  public void storeShouldBeAbleToFindDeviceForCapabilities() throws Exception {
    // prepare device store
    DefaultAndroidEmulator deEmulator16 = anEmulator("de", DeviceTargetPlatform.ANDROID16, false);
    DeviceStore deviceStore = new DeviceStore();
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator16}));

    // find by Capabilities
    AndroidDevice device = deviceStore.findAndroidDevice(withDefaultCapabilities());
    // The right device is found
    assertThat(device, equalTo((AndroidDevice) deEmulator16));
    // the device is in use when found
    assertThat(deviceStore.getDevicesInUse(), contains((AndroidDevice) deEmulator16));
  }

  @Test
  public void storeShouldNotBeAbleToFindDeviceIfTargetPlatformIsNotSuported() throws Exception {
    // prepare device store
    DefaultAndroidEmulator deEmulator10 = anEmulator("de", DeviceTargetPlatform.ANDROID10, false);
    AndroidEmulator enEmulator10 = anEmulator("en", DeviceTargetPlatform.ANDROID10, false);
    DeviceStore deviceStore = new DeviceStore();
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator10, enEmulator10}));

    // find by Capabilities
    try {
      deviceStore.findAndroidDevice(withDefaultCapabilities());
      Assert.fail();
    } catch (DeviceStoreException e) {
      assertThat(e.getMessage(),
          equalTo("Device store does not contain a device of requested platform: ANDROID16"));
    }

    assertThat(deviceStore.getDevicesInUse(), hasSize(0));
  }

  @Test
  public void storeShouldNotBeAbleToFindDeviceIfScreenSizeIsNotSupported() throws Exception {
    // prepare device store
    DefaultAndroidEmulator deEmulator10 = anEmulator("de", DeviceTargetPlatform.ANDROID16, false);
    AndroidEmulator enEmulator10 = anEmulator("en", DeviceTargetPlatform.ANDROID10, false);
    DeviceStore deviceStore = new DeviceStore();
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator10, enEmulator10}));

    // find by Capabilities
    SelendroidCapabilities capa = withDefaultCapabilities();
    capa.setScreenSize("768x1024");
    try {
      deviceStore.findAndroidDevice(capa);
      Assert.fail();
    } catch (DeviceStoreException e) {
      assertThat(e.getMessage(), containsString("No devices are found."));
    }

    assertThat(deviceStore.getDevicesInUse(), hasSize(0));
    assertThat(deviceStore.getDevicesList().values(), hasSize(2));
  }

  protected SelendroidCapabilities withDefaultCapabilities() {
    SelendroidCapabilities capabilities = new SelendroidCapabilities();
    capabilities.setAndroidTarget(DeviceTargetPlatform.ANDROID16.name());
    capabilities.setScreenSize("320x480");
    return capabilities;
  }
}
