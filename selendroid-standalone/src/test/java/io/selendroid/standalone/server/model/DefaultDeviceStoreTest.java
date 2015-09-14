/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
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

import static io.selendroid.standalone.server.model.DeviceStoreFixture.anDevice;
import static io.selendroid.standalone.server.model.DeviceStoreFixture.anDeviceManager;
import static io.selendroid.standalone.server.model.DeviceStoreFixture.anEmulator;
import static io.selendroid.standalone.server.model.DeviceStoreFixture.withDefaultCapabilities;
import static io.selendroid.standalone.server.model.DeviceStoreFixture.withModelCapabilities;
import static io.selendroid.standalone.server.model.DeviceStoreFixture.withWrongModelCapabilities;
import static io.selendroid.standalone.server.model.DeviceStoreFixture.withGoogleAPITypeCapabilities;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.common.device.DeviceTargetPlatform;
import io.selendroid.standalone.android.AndroidDevice;
import io.selendroid.standalone.android.AndroidEmulator;
import io.selendroid.standalone.android.impl.AbstractAndroidDeviceEmulator;
import io.selendroid.standalone.android.impl.DefaultHardwareDevice;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.exceptions.DeviceStoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Dimension;

/**
 * @author ddary
 */
public class DefaultDeviceStoreTest {
  public static final String ANY_STRING = "ANY";
  private static final Integer EMULATOR_PORT = 5560;

  @Test
  public void shouldAddStartedEmulator() throws Exception {
    AndroidEmulator emulator = anEmulator("de", DeviceTargetPlatform.ANDROID10, false, null);

    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addEmulators(Collections.singletonList(emulator));
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 0);
    Assert.assertEquals(deviceStore.getDevicesList().size(), 1);

    Assert.assertTrue(deviceStore.getDevicesList().containsKey(DeviceTargetPlatform.ANDROID10));
    Assert.assertTrue(deviceStore.getDevicesList().get(DeviceTargetPlatform.ANDROID10)
        .contains(emulator));
  }

  @Test
  public void shouldIncrementEmulatorPortsByTwo() throws Exception {
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    Assert.assertEquals(5560, deviceStore.nextEmulatorPort().intValue());
    Assert.assertEquals(5562, deviceStore.nextEmulatorPort().intValue());
    Assert.assertEquals(5564, deviceStore.nextEmulatorPort().intValue());
  }

  @Test
  public void shouldReleaseActiveEmulators() throws Exception {
    AndroidEmulator deEmulator = anEmulator("de", DeviceTargetPlatform.ANDROID16, false, null);
    when(deEmulator.getPort()).thenReturn(5554);

    EmulatorPortFinder finder = mock(EmulatorPortFinder.class);
    when(finder.next()).thenReturn(5554);
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(finder, anDeviceManager());
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator}));
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 0);
    AndroidDevice foundDevice = deviceStore.findAndroidDevice(withDefaultCapabilities());
    Assert.assertEquals(deEmulator, foundDevice);
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 1);
    deviceStore.release(foundDevice, null);
    // make sure the emulator has been stopped
    verify(deEmulator, times(1)).stop();
    verify(finder, times(1)).release(5554);
    // Make sure the device has been removed from devices in use
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 0);
  }

  @Test
  public void shouldReleaseActiveEmulatorButKeepItRunning() throws Exception {
    AndroidEmulator deEmulator = anEmulator("de", DeviceTargetPlatform.ANDROID16, false, null);
    when(deEmulator.getPort()).thenReturn(5554);

    EmulatorPortFinder finder = mock(EmulatorPortFinder.class);
    when(finder.next()).thenReturn(5554);
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(finder, anDeviceManager());
    deviceStore.setKeepEmulator(true); // given should keep emulator is set

    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator}));
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 0);
    AndroidDevice foundDevice = deviceStore.findAndroidDevice(withDefaultCapabilities());
    Assert.assertEquals(deEmulator, foundDevice);
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 1);
    deviceStore.release(foundDevice, null);
    // make sure the emulator has NOT been stopped
    verify(deEmulator, times(0)).stop();
    verify(finder, times(0)).release(5554);
    // Make sure the device has been removed from devices in use
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 0);
  }

  @Test
  public void shouldRegisterMultipleNotStatedEmulators() throws Exception {
    AndroidEmulator deEmulator10 = anEmulator("de", DeviceTargetPlatform.ANDROID10, false, null);
    AndroidEmulator enEmulator10 = anEmulator("en", DeviceTargetPlatform.ANDROID10, false, null);
    AndroidEmulator deEmulator16 = anEmulator("de", DeviceTargetPlatform.ANDROID16, false, null);

    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
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
  public void shouldRegisterStartedAndStoppedEmulators() throws Exception {
    AndroidEmulator deEmulator10 = anEmulator("de", DeviceTargetPlatform.ANDROID10, false, null);
    AndroidEmulator enEmulator10 = anEmulator("en", DeviceTargetPlatform.ANDROID10, true, null);
    AndroidEmulator deEmulator16 = anEmulator("de", DeviceTargetPlatform.ANDROID16, true, null);

    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator10, enEmulator10,
        deEmulator16}));
    Assert.assertEquals(deviceStore.getDevicesInUse().size(), 0);

    Assert.assertTrue("Should have an entry for target version 10.", deviceStore.getDevicesList()
        .containsKey(DeviceTargetPlatform.ANDROID10));
    List<AndroidDevice> devicesApi10 =
        deviceStore.getDevicesList().get(DeviceTargetPlatform.ANDROID10);
    Assert.assertTrue("Entry of target version 10 should contain de emulator.",
        devicesApi10.contains(deEmulator10));
    Assert.assertTrue("Entry of target version 10 should contain en emulator.",
        devicesApi10.contains(enEmulator10));

    Assert.assertTrue("Should have an entry for target version 16.", deviceStore.getDevicesList()
        .containsKey(DeviceTargetPlatform.ANDROID16));
    Assert.assertTrue("Entry of target version 16 should contain the emulator.", deviceStore
        .getDevicesList().get(DeviceTargetPlatform.ANDROID16).contains(deEmulator16));
  }

  @Test
  public void shouldNotIgnoreRunningEmulators() throws Exception {
    AndroidEmulator enEmulator10 = anEmulator("en", DeviceTargetPlatform.ANDROID10, true, null);
    AndroidEmulator deEmulator16 = anEmulator("de", DeviceTargetPlatform.ANDROID16, true, null);

    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {enEmulator10, deEmulator16}));

    // Nothing has been added.
    Assert.assertEquals(deviceStore.getDevicesList().size(), 2);
  }

  @Test
  public void storeShouldDoNothingIfInitializedWithEmptyList() throws AndroidDeviceException {
    DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());

    store.addEmulators(new ArrayList<AndroidEmulator>());
    Assert.assertEquals(store.getDevicesList().size(), 0);
  }

  @Test
  public void storeShouldDoNothingIfInitializedWithNull() throws AndroidDeviceException {
    DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());

    store.addEmulators(null);
    Assert.assertEquals(store.getDevicesList().size(), 0);
  }



  @Test
  public void shouldFindSwitchedOffEmulator() throws Exception {
    // prepare device store
    AbstractAndroidDeviceEmulator deEmulator16 = anEmulator("de", DeviceTargetPlatform.ANDROID16, false, null);
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator16}));

    // find by Capabilities
    AndroidDevice device = deviceStore.findAndroidDevice(withDefaultCapabilities());
    // The right device is found
    assertThat(device, equalTo((AndroidDevice) deEmulator16));
    // the device is in use when found
    assertThat(deviceStore.getDevicesInUse(), contains((AndroidDevice) deEmulator16));
  }

  @Test
  public void shouldThrowAnExceptionIfTargetPlatformIsMissingInCapabilities() throws Exception {
    // prepare device store
    AbstractAndroidDeviceEmulator deEmulator16 = anEmulator("de", DeviceTargetPlatform.ANDROID16, false, null);
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator16}));

    SelendroidCapabilities capa = new SelendroidCapabilities();
    try {
      deviceStore.findAndroidDevice(capa);
      Assert.fail();
    } catch (DeviceStoreException e) {
      Assert
          .assertEquals(
              "No devices are found. This can happen if the devices are in use or no device screen matches the required capabilities.",
              e.getMessage());
    }
  }

  @Test
  public void shouldNotFindDeviceIfTargetPlatformIsNotSuported() throws Exception {
    // prepare device store
    AbstractAndroidDeviceEmulator deEmulator10 = anEmulator("de", DeviceTargetPlatform.ANDROID10, false, null);
    AndroidEmulator enEmulator10 = anEmulator("en", DeviceTargetPlatform.ANDROID10, false, null);
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator10, enEmulator10}));

    // find by Capabilities
    try {
      deviceStore.findAndroidDevice(withDefaultCapabilities());
      Assert.fail();
    } catch (DeviceStoreException e) {
      assertThat(
          e.getMessage(),
          equalTo("No devices are found. This can happen if the devices are in use or no device screen matches the required capabilities."));
    }

    assertThat(deviceStore.getDevicesInUse(), hasSize(0));
  }

  @Test
  public void shouldNotFindDeviceIfScreenSizeIsNotSupported() throws Exception {
    // prepare device store
    AbstractAndroidDeviceEmulator deEmulator10 = anEmulator("de", DeviceTargetPlatform.ANDROID16, false, null);
    AndroidEmulator enEmulator10 = anEmulator("en", DeviceTargetPlatform.ANDROID10, false, null);
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
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

  @Test
  public void testShouldBeAbleToAddDevices() throws Exception {
    AndroidDevice device = mock(AndroidDevice.class);
    when(device.getTargetPlatform()).thenReturn(DeviceTargetPlatform.ANDROID16);
    when(device.isDeviceReady()).thenReturn(Boolean.TRUE);

    DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    store.addDevice(device);
    assertThat(store.getDevicesList().values(), hasSize(1));
    assertThat(store.getDevicesInUse(), hasSize(0));
    assertThat(store.getDevicesList().values().iterator().next(), contains(device));
  }

  @Test
  public void testShouldBeAbleToUpdateDevices() throws Exception {
    AndroidEmulator emulator = anEmulator("de", DeviceTargetPlatform.ANDROID10, false, null);

    Map<String,String> prop = new HashMap<String, String>(); // used for phone properties
    AndroidDevice device = anDevice("01234ABC", prop);

    DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    store.addEmulators(Arrays.asList(new AndroidEmulator[] {emulator}));

    // Emulating ddmlib behavior.
    // Add device with empty property because HardwareDeviceListener#onDeviceConnected is called
    // before phone properties are available
    store.addDevice(device);
    assertThat(store.getDevicesList().get(null), hasSize(1));
    assertThat(store.getDevicesList().get(null), contains(device));

    // After phone properties are available, HardwareDeviceListener#onDeviceChanged is called
    prop.put("ro.build.version.sdk", "16");
    prop.put("ro.product.model", "en");
    store.updateDevice(device);

    assertThat(store.getDevicesList().get(null), hasSize(0));
    assertThat(store.getDevicesList().get(DeviceTargetPlatform.ANDROID16), hasSize(1));
    assertThat(store.getDevicesList().get(DeviceTargetPlatform.ANDROID16), contains(device));
  }

  @Test
  public void testShouldBeAbleToRemoveDevices() throws Exception {
    AbstractAndroidDeviceEmulator emulator = anEmulator("de", DeviceTargetPlatform.ANDROID10, false, null);
    AndroidDevice device = anDevice("en", DeviceTargetPlatform.ANDROID16);

    DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    store.addEmulators(Arrays.asList(new AndroidEmulator[] {emulator}));
    store.addDevice(device);
    store.removeAndroidDevice(device);

    assertThat(store.getDevicesList().values(), hasSize(1));
    assertThat(store.getDevicesList().values().iterator().next(), contains((AndroidDevice) emulator));
  }

  @Test
  public void shouldIgnoreNotReadyDevices() throws Exception {
    AndroidDevice device = mock(AndroidDevice.class);
    when(device.getTargetPlatform()).thenReturn(DeviceTargetPlatform.ANDROID16);
    when(device.isDeviceReady()).thenReturn(Boolean.FALSE);

    DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    store.addDevice(device);
    assertThat(store.getDevicesList().values(), hasSize(0));
    assertThat(store.getDevicesInUse(), hasSize(0));
  }

  @Test
  public void shouldFindRealDeviceForCapabilities() throws Exception {
    AndroidDevice device = mock(DefaultHardwareDevice.class);
    when(device.getTargetPlatform()).thenReturn(DeviceTargetPlatform.ANDROID16);
    when(device.isDeviceReady()).thenReturn(Boolean.TRUE);
    when(device.getScreenSize()).thenReturn(new Dimension(320, 480));
    when(device.screenSizeMatches("320x480")).thenReturn(Boolean.TRUE);

    DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    store.addDevice(device);
    assertThat(store.getDevicesList().values(), hasSize(1));
    assertThat(store.getDevicesInUse(), hasSize(0));
    SelendroidCapabilities capa = withDefaultCapabilities();
    capa.setEmulator(false);
    AndroidDevice foundDevice = store.findAndroidDevice(capa);
    assertThat(foundDevice, equalTo(device));
    assertThat(store.getDevicesInUse(), hasSize(1));
  }

  @Test
  public void shouldNotFindRealDeviceForCapabilities() throws Exception {
    AndroidDevice device = mock(AndroidDevice.class);
    when(device.getTargetPlatform()).thenReturn(DeviceTargetPlatform.ANDROID16);
    when(device.isDeviceReady()).thenReturn(Boolean.TRUE);
    when(device.getScreenSize()).thenReturn(new Dimension(320, 500));
    when(device.screenSizeMatches("320x500")).thenReturn(Boolean.FALSE);

    DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    store.addDevice(device);
    assertThat(store.getDevicesList().values(), hasSize(1));
    assertThat(store.getDevicesInUse(), hasSize(0));
    try {
      store.findAndroidDevice(withDefaultCapabilities());
      Assert.fail();
    } catch (DeviceStoreException e) {
      // expected
    }

    assertThat(store.getDevicesInUse(), hasSize(0));
  }

  @Test
  public void shouldFindRealDeviceBySerial() throws Exception {
      AndroidDevice device1 = mock(DefaultHardwareDevice.class);
      String device1Serial = "device1Serial";
      AndroidDevice device2 = mock(DefaultHardwareDevice.class);
      String device2Serial = "device2Serial";

      for (AndroidDevice device : Lists.newArrayList(device1, device2)) {
          when(device.getTargetPlatform()).thenReturn(DeviceTargetPlatform.ANDROID16);
          when(device.isDeviceReady()).thenReturn(Boolean.TRUE);
          when(device.getScreenSize()).thenReturn(new Dimension(320, 480));
          when(device.screenSizeMatches("320x480")).thenReturn(Boolean.TRUE);
      }

      when(device1.getSerial()).thenReturn(device1Serial);
      when(device2.getSerial()).thenReturn(device2Serial);

      DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
      store.addDevice(device1);
      store.addDevice(device2);
      assertThat(store.getDevicesList().values(), hasSize(1));
      assertThat(store.getDevicesList().get(DeviceTargetPlatform.ANDROID16), hasSize(2));
      assertThat(store.getDevicesInUse(), hasSize(0));

      SelendroidCapabilities capa = withDefaultCapabilities();
      capa.setEmulator(false);
      capa.setSerial(device2Serial);

      AndroidDevice foundDevice = store.findAndroidDevice(capa);
      assertThat(foundDevice, equalTo(device2));
      assertThat(Iterables.getOnlyElement(store.getDevicesInUse()), is(device2));
  }

  @Test(expected = DeviceStoreException.class)
  public void willNotReturnADeviceInUse() throws Exception {
      AndroidDevice device = mock(DefaultHardwareDevice.class);
      String serial = "device1Serial";

      when(device.getTargetPlatform()).thenReturn(DeviceTargetPlatform.ANDROID16);
      when(device.isDeviceReady()).thenReturn(Boolean.TRUE);
      when(device.getScreenSize()).thenReturn(new Dimension(320, 480));
      when(device.screenSizeMatches("320x480")).thenReturn(Boolean.TRUE);
      when(device.getSerial()).thenReturn(serial);

      DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
      store.addDevice(device);
      store.getDevicesInUse().add(device);

      SelendroidCapabilities capa = withDefaultCapabilities();
      capa.setEmulator(false);
      capa.setSerial(serial);
      store.findAndroidDevice(withDefaultCapabilities());
  }

  @Test(expected = IllegalArgumentException.class)
  public void findAndroidDeviceThrowsIllegalArgumentExceptionIfCapabilitiesNull() throws Exception {
      DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
      store.findAndroidDevice(null);
  }

  @Test(expected = DeviceStoreException.class)
  public void findAndroidDeviceThrowsDeviceStoreExceptionIfNoDevices() throws Exception {
      DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
      store.getDevices().clear();
      store.findAndroidDevice(withDefaultCapabilities());
  }


  @Test
  public void shouldRemoveHardwareDevice() throws Exception {
    DefaultHardwareDevice device = anDevice("de", DeviceTargetPlatform.ANDROID16);
    DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    store.addDevice(device);
    assertThat(store.getDevicesList().values(), hasSize(1));
    store.removeAndroidDevice(device);
    assertThat(store.getDevicesList().values(), hasSize(0));
  }

  @Test
  public void shouldNotRemoveAnEmulator() throws Exception {
    AbstractAndroidDeviceEmulator deEmulator10 = anEmulator("de", DeviceTargetPlatform.ANDROID16, false, null);
    DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    store.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator10}));

    assertThat(store.getDevicesList().values(), hasSize(1));
    try {
      store.removeAndroidDevice(deEmulator10);
      Assert.fail("Only hardware devices should be able to be removed.");
    } catch (DeviceStoreException e) {
      // expected
    }
  }

  @Test
  public void shouldFindStartedEmulator() throws Exception {
    // prepare device store
    AbstractAndroidDeviceEmulator deEmulator16 = anEmulator("de", DeviceTargetPlatform.ANDROID16, true, null);
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {deEmulator16}));

    // find by Capabilities
    AndroidDevice device = deviceStore.findAndroidDevice(withDefaultCapabilities());
    // The right device is found
    assertThat(device, equalTo((AndroidDevice) deEmulator16));
    // the device is in use when found
    assertThat(deviceStore.getDevicesInUse(), contains((AndroidDevice) deEmulator16));
  }

  @Test
  public void shouldFindAnEmulatorWithSpecifiedModel() throws Exception {
    // adding Nexus 5 to device store
    AbstractAndroidDeviceEmulator emulator = anEmulator("emulator", DeviceTargetPlatform.ANDROID16, true, null);
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {emulator}));

    // find by Capabilities
    AndroidDevice foundEmulator = deviceStore.findAndroidDevice(withModelCapabilities());
    // the right device is found
    assertThat(foundEmulator, equalTo((AndroidDevice) emulator));
  }

  @Test
  public void shouldFindARealDeviceWithSpecifiedModel() throws Exception {
    // adding Nexus 5 to device store
    DefaultHardwareDevice device = anDevice("Nexus 5", DeviceTargetPlatform.ANDROID16);
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addDevice(device);

 // find by Capabilities
    AndroidDevice foundDevice = deviceStore.findAndroidDevice(withModelCapabilities());
 // the right device is found
    assertThat(foundDevice, equalTo((AndroidDevice) device));
  }

  @Test
  public void shouldNotFindAnEmulatorWithWrongModel() throws Exception {
    // adding Nexus 5 to device store
    AbstractAndroidDeviceEmulator emulator = anEmulator("emulator", DeviceTargetPlatform.ANDROID16, true, null);
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] {emulator}));

    try {
      deviceStore.findAndroidDevice(withWrongModelCapabilities());
      Assert.fail();
    } catch (DeviceStoreException e) {
      assertThat(
          e.getMessage(),
          equalTo("No devices are found. This can happen if the devices are in use or no device screen matches the required capabilities."));
    }
  }
  @Test
  public void shouldNotFindARealDeviceWithWrongModel() throws Exception {
    // adding Nexus 5 to device store
    AndroidDevice device = anDevice("Nexus 5", DeviceTargetPlatform.ANDROID16);
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addDevice(device);

    try {
      deviceStore.findAndroidDevice(withWrongModelCapabilities());
      Assert.fail();
    } catch (DeviceStoreException e) {
      assertThat(
          e.getMessage(),
          equalTo("No devices are found. This can happen if the devices are in use or no device screen matches the required capabilities."));
    }
  }
  
  @Test
  public void shouldFindAnEmulatorWithMatchedAPIType() throws Exception {
    AbstractAndroidDeviceEmulator emulator = anEmulator("emulator", DeviceTargetPlatform.ANDROID16, true, "google");
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] { emulator }));

    AndroidDevice foundEmulator = deviceStore.findAndroidDevice(withGoogleAPITypeCapabilities());
    assertThat(foundEmulator, equalTo((AndroidDevice) emulator));
  }

  @Test
  public void shouldNotFindAnEmulatorWithWrongAPIType() throws Exception {
    AbstractAndroidDeviceEmulator emulator = anEmulator("emulator", DeviceTargetPlatform.ANDROID16, true, "android");
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addEmulators(Arrays.asList(new AndroidEmulator[] { emulator }));

    try {
      deviceStore.findAndroidDevice(withGoogleAPITypeCapabilities());
      Assert.fail();
    } catch (DeviceStoreException e) {
      assertThat(
          e.getMessage(),
          equalTo("No devices are found. This can happen if the devices are in use or no device screen matches the required capabilities."));
    }
  }

  @Test
  public void shouldNotFindARealDeviceWithAPIType() throws Exception {
    AndroidDevice device = anDevice("RealDevice", DeviceTargetPlatform.ANDROID16);
    DefaultDeviceStore deviceStore = new DefaultDeviceStore(EMULATOR_PORT, anDeviceManager());
    deviceStore.addDevice(device);

    try {
      deviceStore.findAndroidDevice(withGoogleAPITypeCapabilities());
      Assert.fail();
    } catch (DeviceStoreException e) {
      assertThat(
          e.getMessage(),
          equalTo("No devices are found. This can happen if the devices are in use or no device screen matches the required capabilities."));
    }
  }
}
