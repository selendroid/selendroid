/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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

    emulator.start(new Locale("en_GB"), 5554, null);
    Assert.assertTrue(emulator.isEmulatorStarted());
    emulator.stop();
    Assert.assertFalse(emulator.isEmulatorStarted());
  }
}
