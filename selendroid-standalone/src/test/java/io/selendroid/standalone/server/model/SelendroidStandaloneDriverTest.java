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

import static io.selendroid.standalone.server.model.SelendroidStandaloneDriverFixture.getAndroidApkServerBuilder;
import static io.selendroid.standalone.server.model.SelendroidStandaloneDriverFixture.getDeviceManager;
import static io.selendroid.standalone.server.model.SelendroidStandaloneDriverFixture.getSelendroidStandaloneDriver;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.common.device.DeviceTargetPlatform;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.exceptions.DeviceStoreException;
import io.selendroid.standalone.server.support.DeviceForTest;
import io.selendroid.standalone.server.support.TestSessionListener;

import java.io.File;
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
  private static final String TEST_APP_LAUNCH_ACTIVITY = "HomeScreenActivity";
  public static final String APK_FILE = "src/test/resources/selendroid-test-app.apk";
  public static final String INVALID_APK_FILE =
      "src/test/resources/selendroid-test-app-invalid.apk";
  private static final Integer EMULATOR_PORT = 5560;

  @Test
  public void shouldInitDriver() throws Exception {
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.addSupportedApp(new File(APK_FILE).getAbsolutePath());
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();
    driver.initApplicationsUnderTest(conf);
    assertThatTestAppHasBeenSuccessfullyRegistered(driver);
  }

  @Test
  public void shouldInitDriverAndIgnoreInvalidEntry() throws Exception {
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.addSupportedApp(new File(APK_FILE).getAbsolutePath());
    conf.addSupportedApp(new File(INVALID_APK_FILE).getAbsolutePath());
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();
    driver.initApplicationsUnderTest(conf);
    assertThatTestAppHasBeenSuccessfullyRegistered(driver);
  }

  @Test
  public void shouldInitDriverWithoutAnyConfig() throws Exception {
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();
    try {
      driver.initApplicationsUnderTest(new SelendroidConfiguration());
    } catch (SelendroidException e) {
      Assert.assertEquals("Configuration error - no apps has been configured.", e.getMessage());
    }
  }

  @Test
  public void shouldInitDriverIfNoValidAppIsAvailable() throws Exception {
    SelendroidConfiguration conf = new SelendroidConfiguration();
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver(getAndroidApkServerBuilder());

    driver.initApplicationsUnderTest(conf);

    Assert.assertTrue("Expecting only one app to be configured",
        driver.getConfiguredApps().size() == 1);
    Assert.assertEquals("Expecting AndroidDriver app to be configured", driver.getConfiguredApps()
        .get(BrowserType.ANDROID).getAppId(), BrowserType.ANDROID);
  }

  protected void assertThatTestAppHasBeenSuccessfullyRegistered(SelendroidStandaloneDriver driver) {
    Map<String, AndroidApp> apps = driver.getConfiguredApps();
    Assert.assertTrue("expecting 2 test app has been registered but was " + apps.size(),
        apps.size() == 2);

    Assert.assertTrue("expecting test app has been registered with the right key",
        apps.containsKey(TEST_APP_ID));
  }

  @Test
  public void shouldCreateNewTestSession() throws Exception {
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.addSupportedApp(new File(APK_FILE).getAbsolutePath());

    SelendroidCapabilities caps = new SelendroidCapabilities();
    caps.setAut(TEST_APP_ID);

    createTestSession(conf, caps);
  }

  @Test
  public void shouldCreateNewTestSessionIfNoApkPassed() throws Exception {
    SelendroidCapabilities caps = new SelendroidCapabilities();
    caps.setAut(TEST_APP_ID);
    caps.setLaunchActivity(TEST_APP_LAUNCH_ACTIVITY);
    createTestSession(new SelendroidConfiguration(), caps);
  }

  private void createTestSession(SelendroidConfiguration conf, SelendroidCapabilities caps) throws Exception{
    // Setting up driver with test app and device stub
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();
    driver.initApplicationsUnderTest(conf);
    DefaultDeviceStore store = new DefaultDeviceStore(EMULATOR_PORT, getDeviceManager());

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
    caps.setPlatformVersion(DeviceTargetPlatform.ANDROID16);
    try {
      String sessionId = driver.createNewTestSession(new JSONObject(caps.asMap()), 0);
      Assert.assertNotNull(UUID.fromString(sessionId));
    } finally {
      // this will also stop the http server
      emulator.stop();
    }
  }

  @Test
  public void shouldIdentifyStartedAndStoppedEmulators() throws Exception {
    // Setting up driver with test app and device stub
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.addSupportedApp(new File(APK_FILE).getAbsolutePath());
    driver.initApplicationsUnderTest(conf);
    driver.initAndroidDevices();
  }

  @Test
  public void shouldRetrySessionCreationTheConfiguredAmountOfTimesOnFailure() throws Exception {
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();

    // Configure a few retries
    SelendroidConfiguration conf = new SelendroidConfiguration();
    int configuredRetries = 5;
    conf.setServerStartRetries(configuredRetries);
    driver.initApplicationsUnderTest(conf);

    DefaultDeviceStore deviceStore = swapWithFailingDeviceDriver(driver);

    try {
        driver.createNewTestSession(createCapabilities());
    } catch (Exception e) {
    }

    verify(deviceStore, times(configuredRetries + 1))
      .findAndroidDevice(any(SelendroidCapabilities.class));
  }

  @Test
  public void shouldTryAtLeastOneTimeIfNoRetriesConfigured() throws Exception {
    SelendroidStandaloneDriver driver = getSelendroidStandaloneDriver();

    // Configure a few retries
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.setServerStartRetries(0);
    driver.initApplicationsUnderTest(conf);

    DefaultDeviceStore deviceStore = swapWithFailingDeviceDriver(driver);

    try {
        driver.createNewTestSession(createCapabilities());
    } catch (Exception e) {
    }

    verify(deviceStore, times(1))
      .findAndroidDevice(any(SelendroidCapabilities.class));
  }

  private DefaultDeviceStore swapWithFailingDeviceDriver(SelendroidStandaloneDriver driver) throws Exception {
    //count the amount of calls to DefaultDeviceStore to check how many times it was retried
    DefaultDeviceStore deviceStore = mock(DefaultDeviceStore.class);

    // throw to simulate failure, triggering a retry
    when(deviceStore.findAndroidDevice(any(SelendroidCapabilities.class)))
          .thenThrow(new DeviceStoreException("Empty store"));

    driver.setDeviceStore(deviceStore);

    return deviceStore;
  }

  private JSONObject createCapabilities() {
    SelendroidCapabilities caps = new SelendroidCapabilities();
    caps.setAut(TEST_APP_ID);
    caps.setLaunchActivity(TEST_APP_LAUNCH_ACTIVITY);

    return new JSONObject(caps.asMap());
  }
}
