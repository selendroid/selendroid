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
package io.selendroid.server.model;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.selendroid.android.AndroidApp;
import io.selendroid.android.DeviceManager;
import io.selendroid.android.impl.DefaultAndroidApp;
import io.selendroid.builder.AndroidDriverAPKBuilder;
import io.selendroid.builder.SelendroidServerBuilder;
import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.exceptions.ShellCommandException;

import java.io.File;
import java.io.IOException;

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

  public static DeviceManager anDeviceManager() throws AndroidDeviceException {
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
    return new SelendroidStandaloneDriver(builder, anDeviceManager(), getAndroidDriverAPKBuilder());
  }
}
