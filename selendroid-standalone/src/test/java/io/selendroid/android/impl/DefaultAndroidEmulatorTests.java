package io.selendroid.android.impl;

import io.selendroid.android.AndroidEmulator;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class DefaultAndroidEmulatorTests {
  @Test
  public void shouldBeAbleToListAvds() throws Exception {
    List<AndroidEmulator> avds = DefaultAndroidEmulator.listAvailableAvds();
    Assert.assertFalse("Expecting list of avds not to be empty", avds.isEmpty());
  }

  @Test
  public void testShouldBeAbleToStartEmulator() throws Exception {
    AndroidEmulator emulator =
        new DefaultAndroidEmulator("l10n", "X86", "320x480", "16", new File(
            FileUtils.getUserDirectory(), ".android" + File.separator + "avd" + File.separator
                + "l10n.avd"));


    Assert.assertTrue("expecting emulators exists: ", emulator.isEmulatorAlreadyExistent());
    Assert.assertFalse("expecting emulator is not yet started: ", emulator.isEmulatorStarted());
    emulator.startEmulator(new Locale("en_GB"), 5554);
    Assert.assertTrue(emulator.isEmulatorStarted());
    emulator.stopEmulator();
    Assert.assertFalse(emulator.isEmulatorStarted());
  }
}
