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
package io.selendroid.standalone.android.impl;

import com.android.ddmlib.IDevice;

import io.netty.handler.codec.http.HttpMethod;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.AndroidDevice;
import io.selendroid.standalone.android.AndroidSdk;
import io.selendroid.standalone.android.impl.DefaultAndroidApp;
import io.selendroid.standalone.android.impl.DefaultHardwareDevice;
import io.selendroid.standalone.io.ShellCommand;
import io.selendroid.standalone.server.util.HttpClientUtil;
import io.selendroid.standalone.util.SelendroidAssert;

import org.apache.commons.exec.CommandLine;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Works only if - android_home and java_home are set and - if one emulator is already running - and
 * selendroid project is build (apks are existent)
 * 
 * @author ddary
 * 
 */
public class DefaultHardwareDeviceTests {
  public static final String SELENDROID_SERVER_PACKAGE = "io.selendroid";
  public static final String AUT_PACKAGE = "io.selendroid.testapp";
  private String serial = "emulator-5554";
  private int port = 7070;
  private AndroidApp selendroidServer = new DefaultAndroidApp(new File(
      "../selendroid-server/target/selendroid-server-0.4-SNAPSHOT.apk"));
  private AndroidApp aut = new DefaultAndroidApp(new File(
      "../selendroid-test-app/target/selendroid-test-app-0.4-SNAPSHOT.apk"));

  private void cleanUpDevice(AndroidDevice emulator) throws Exception {
    emulator.uninstall(selendroidServer);
    emulator.uninstall(aut);
    String installedAPKs = listInstalledPackages();
    Assert.assertFalse(installedAPKs.contains(SELENDROID_SERVER_PACKAGE));
    Assert.assertFalse(installedAPKs.contains(AUT_PACKAGE));
  }

  @Test
  public void testShouldBeAbleToStartSelendroid() throws Exception {
    IDevice device = mock(IDevice.class);
    when(device.getSerialNumber()).thenReturn(serial);
    AndroidDevice emulator = new DefaultHardwareDevice(device);
    Assert.assertTrue(emulator.isDeviceReady());
    cleanUpDevice(emulator);

    // install apps
    emulator.install(selendroidServer);
    emulator.install(aut);
    String installedAPKs = listInstalledPackages();
    Assert.assertTrue(installedAPKs.contains(SELENDROID_SERVER_PACKAGE));
    Assert.assertTrue(installedAPKs.contains(AUT_PACKAGE));

    // start selendroid
    emulator.startSelendroid(aut, port, new SelendroidCapabilities(), "localhost");
    String url = "http://localhost:" + port + "/wd/hub/status";
    HttpResponse response = HttpClientUtil.executeRequest(url, HttpMethod.GET);
    SelendroidAssert.assertResponseIsOk(response);
    Assert.assertTrue(emulator.isSelendroidRunning());
  }

  private String listInstalledPackages() throws Exception {
    CommandLine command = new CommandLine(AndroidSdk.adb().getAbsolutePath());
    command.addArgument("-s", false);
    command.addArgument(serial, false);
    command.addArgument("shell", false);
    command.addArgument("pm", false);
    command.addArgument("list", false);
    command.addArgument("packages", false);
    return ShellCommand.exec(command);
  }
}
