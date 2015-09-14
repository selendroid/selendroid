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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.DeviceManager;
import io.selendroid.standalone.android.impl.DefaultAndroidApp;
import io.selendroid.standalone.builder.AndroidDriverAPKBuilder;
import io.selendroid.standalone.builder.SelendroidServerBuilder;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.standalone.exceptions.ShellCommandException;
import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;

import java.io.File;
import java.io.IOException;

import io.selendroid.standalone.server.model.impl.DefaultInitAndroidDevicesStrategy;
import org.openqa.selenium.remote.BrowserType;

public class SelendroidStandaloneDriverFixture {
  protected static SelendroidServerBuilder getAndroidApkServerBuilder() throws IOException,
      ShellCommandException, AndroidSdkException {
    SelendroidServerBuilder builder = mock(SelendroidServerBuilder.class);
    AndroidApp server = mock(AndroidApp.class);
    AndroidApp resignedApp = mock(AndroidApp.class);
    when(resignedApp.getAppId()).thenReturn(BrowserType.ANDROID);

    when(
        builder.createSelendroidServer(new DefaultAndroidApp(new File(
            SelendroidStandaloneDriverTest.APK_FILE)))).thenReturn(server);
    when(builder.resignApp(any(File.class))).thenReturn(resignedApp);
    return builder;
  }

  public static DeviceManager getDeviceManager() throws AndroidDeviceException {
    DeviceManager finder = mock(DeviceManager.class);
    when(finder.getVirtualDevice("emulator-5554")).thenReturn(null);

    return finder;
  }

  protected static SelendroidServerBuilder getApkBuilder() throws IOException,
      ShellCommandException, AndroidSdkException {
    SelendroidServerBuilder builder = mock(SelendroidServerBuilder.class);
    AndroidApp server = mock(AndroidApp.class);
    AndroidApp resignedApp = mock(AndroidApp.class);
    when(resignedApp.getAppId()).thenReturn(SelendroidStandaloneDriverTest.TEST_APP_ID);

    when(
        builder.createSelendroidServer(new DefaultAndroidApp(new File(
            SelendroidStandaloneDriverTest.APK_FILE)))).thenReturn(server);
    when(builder.resignApp(any(File.class))).thenReturn(resignedApp);
    return builder;
  }

  protected static AndroidDriverAPKBuilder getAndroidDriverAPKBuilder() {
    AndroidDriverAPKBuilder builder = mock(AndroidDriverAPKBuilder.class);
    when(builder.extractAndroidDriverAPK()).thenReturn(new File(""));
    return builder;
  }

  protected static SelendroidStandaloneDriver getSelendroidStandaloneDriver() throws IOException,
      ShellCommandException, AndroidSdkException, AndroidDeviceException {
    return getSelendroidStandaloneDriver(getApkBuilder());
  }

  protected static SelendroidStandaloneDriver getSelendroidStandaloneDriver(
      SelendroidServerBuilder builder) throws IOException, ShellCommandException,
      AndroidSdkException, AndroidDeviceException {
    return new SelendroidStandaloneDriver(builder, getDeviceManager(), getAndroidDriverAPKBuilder(),
            new DefaultInitAndroidDevicesStrategy());
  }
}
