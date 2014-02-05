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
package io.selendroid.server.model;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.selendroid.SelendroidCapabilities;
import io.selendroid.SelendroidConfiguration;
import io.selendroid.android.AndroidApp;
import io.selendroid.android.DeviceManager;
import io.selendroid.android.impl.DefaultAndroidApp;
import io.selendroid.android.impl.InstalledAndroidApp;
import io.selendroid.builder.AndroidDriverAPKBuilder;
import io.selendroid.builder.SelendroidServerBuilder;
import io.selendroid.device.DeviceTargetPlatform;
import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.support.DeviceForTest;
import io.selendroid.server.support.TestSessionListener;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.remote.BrowserType;

public class SelendroidStandaloneDriverTest {
  public static final String TEST_APP_ID = "io.selendroid.testapp:0.4-SNAPSHOT";
  private static final String TEST_APP_INSTALLED =
      "io.selendroid.testapp/HomeScreenActivity:0.4-SNAPSHOT";
  private static final String APK_FILE = "src/test/resources/selendroid-test-app.apk";
  private static final String INVALID_APK_FILE =
      "src/test/resources/selendroid-test-app-invalid.apk";
  private static final Integer EMULATOR_PORT = 5560;

  @Test
  public void testShouldBeAbleToInitDriver() throws Exception {
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.addSupportedApp(new File(APK_FILE).getAbsolutePath());
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();
    driver.initApplicationsUnderTest(conf);
    assertThatTestappHasBeenSuccessfullyRegistered(driver);
  }

  @Test
  public void testShouldBeAbleToInitDriverAndIgnoreInvalidEntry() throws Exception {
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.addSupportedApp(new File(APK_FILE).getAbsolutePath());
    conf.addSupportedApp(new File(INVALID_APK_FILE).getAbsolutePath());
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();
    driver.initApplicationsUnderTest(conf);
    assertThatTestappHasBeenSuccessfullyRegistered(driver);
  }

  @Test
  public void testShouldNotbBeAbleInitDriverWithoutAnyConfig() throws Exception {
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();
    try {
      driver.initApplicationsUnderTest(new SelendroidConfiguration());
    } catch (SelendroidException e) {
      Assert.assertEquals("Configuration error - no apps has been configured.", e.getMessage());
    }
  }

  @Test
  public void testShouldNotBeAbleToInitDriverIfNoValidAppIsAvailableAndNoWebViewApp() throws Exception {
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.setNoWebViewApp(true);
    conf.addSupportedApp(new File("NonExistentialFile").getAbsolutePath());
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();
    try {
      driver.initApplicationsUnderTest(conf);
      Assert
          .fail("With complete invalid app configuration the driver should not be able to initialize.");
    } catch (SelendroidException e) {
      Assert.assertEquals(
          "Fatal error initializing SelendroidDriver: configured app(s) have not been found.",
          e.getMessage());
    }
  }

  @Test
  public void testShouldBeAbleToInitDriverIfNoValidAppIsAvailable() throws Exception {
    SelendroidConfiguration conf = new SelendroidConfiguration();
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver(getAndroidApkServerBuilder());

    driver.initApplicationsUnderTest(conf);

    Assert.assertTrue("Expecting only one app to be configured", driver.getConfiguredApps().size() == 1);
    Assert.assertEquals("Expecting AndroidDriver app to be configured",
        driver.getConfiguredApps().get(BrowserType.ANDROID).getAppId(), BrowserType.ANDROID );
  }

  protected void assertThatTestappHasBeenSuccessfullyRegistered(SelendroidStandaloneDriver driver) {
    Map<String, AndroidApp> apps = driver.getConfiguredApps();
    Assert.assertTrue("expecting 2 test app has been registered but was " + apps.size(),
        apps.size() == 2);

    Assert.assertTrue("expecting test app has been registered with the right key",
        apps.containsKey(TEST_APP_ID));
  }

  @Test
  public void assertThatANewtestSessionCanBeCreated() throws Exception {
    // Setting up driver with test app and device stub
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.addSupportedApp(new File(APK_FILE).getAbsolutePath());
    driver.initApplicationsUnderTest(conf);
    DeviceStore store = new DeviceStore(false, EMULATOR_PORT);

    DeviceForTest emulator = new DeviceForTest(DeviceTargetPlatform.ANDROID16);
    Random random = new Random();
    final UUID definedSessionId = new UUID(random.nextLong(), random.nextLong());

    emulator.testSessionListener = new TestSessionListener(definedSessionId.toString(), "test") {
      @Override
      public SelendroidResponse executeSelendroidRequest(Properties params) {
        return null;
      }
    };
    store.addDeviceToStore(emulator);
    driver.setDeviceStore(store);

    // testing new session creation
    SelendroidCapabilities capa = new SelendroidCapabilities();
    capa.setAut(TEST_APP_ID);
    capa.setAndroidTarget(DeviceTargetPlatform.ANDROID16.name());
    try {
      String sessionId = driver.createNewTestSession(new JSONObject(capa.asMap()), 0);
      Assert.assertNotNull(UUID.fromString(sessionId));
    } finally {
      // this will also stop the http server
      emulator.stop();
    }
  }

  @Test
  public void assertThatANewtestSessionCanBeCreatedWithAlreadyInstalledApp() throws Exception {
    // Setting up driver with test app and device stub
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.setInstalledApp(TEST_APP_INSTALLED);
    driver.initApplicationsUnderTest(conf);
    DeviceStore store = new DeviceStore(false, EMULATOR_PORT);

    DeviceForTest emulator = new DeviceForTest(DeviceTargetPlatform.ANDROID16);
    Random random = new Random();
    final UUID definedSessionId = new UUID(random.nextLong(), random.nextLong());

    emulator.testSessionListener = new TestSessionListener(definedSessionId.toString(), "test") {
      @Override
      public SelendroidResponse executeSelendroidRequest(Properties params) {
        return null;
      }
    };
    store.addDeviceToStore(emulator);
    driver.setDeviceStore(store);

    // testing new session creation
    SelendroidCapabilities capa = new SelendroidCapabilities();
    capa.setAut(TEST_APP_ID);
    capa.setAndroidTarget(DeviceTargetPlatform.ANDROID16.name());
    try {
      String sessionId = driver.createNewTestSession(new JSONObject(capa.asMap()), 0);
      Assert.assertNotNull(UUID.fromString(sessionId));
    } finally {
      // this will also stop the http server
      emulator.stop();
    }
  }

  protected SelendroidStandaloneDriver getSelendroidStandaloneDriver() throws IOException, ShellCommandException,
      AndroidSdkException, AndroidDeviceException {
    return getSelendroidStandaloneDriver(getApkBuilder());
  }

  protected SelendroidStandaloneDriver getSelendroidStandaloneDriver(SelendroidServerBuilder builder) throws IOException,
      ShellCommandException, AndroidSdkException, AndroidDeviceException {
    return new SelendroidStandaloneDriver(builder, anDeviceManager(), getAndroidDriverAPKBuilder());
  }

  protected SelendroidServerBuilder getApkBuilder() throws IOException, ShellCommandException,
      AndroidSdkException {
    SelendroidServerBuilder builder = mock(SelendroidServerBuilder.class);
    AndroidApp server = mock(AndroidApp.class);
    AndroidApp resignedApp = mock(AndroidApp.class);
    when(resignedApp.getAppId()).thenReturn(TEST_APP_ID);

    when(builder.createSelendroidServer(new DefaultAndroidApp(new File(APK_FILE)))).thenReturn(
        server);
    when(builder.resignApp(any(File.class))).thenReturn(resignedApp);
    return builder;
  }

  protected AndroidDriverAPKBuilder getAndroidDriverAPKBuilder() {
    AndroidDriverAPKBuilder builder = mock(AndroidDriverAPKBuilder.class);
    when(builder.extractAndroidDriverAPK()).thenReturn(new File(""));
    return builder;
  }

  protected SelendroidServerBuilder getAndroidApkServerBuilder() throws IOException,
      ShellCommandException, AndroidSdkException {
    SelendroidServerBuilder builder = mock(SelendroidServerBuilder.class);
    AndroidApp server = mock(AndroidApp.class);
    AndroidApp resignedApp = mock(InstalledAndroidApp.class);
    when(resignedApp.getAppId()).thenReturn(BrowserType.ANDROID);

    when(builder.createSelendroidServer(new DefaultAndroidApp(new File(APK_FILE)))).thenReturn(
        server);
    when(builder.resignApp(any(File.class))).thenReturn(resignedApp);
    return builder;
  }

  public DeviceManager anDeviceManager() throws AndroidDeviceException {
    DeviceManager finder = mock(DeviceManager.class);
    when(finder.getVirtualDevice("emulator-5554")).thenReturn(null);

    return finder;
  }
}
