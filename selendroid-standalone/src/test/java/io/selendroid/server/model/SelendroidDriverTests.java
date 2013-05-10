package io.selendroid.server.model;

import io.selendroid.SelendroidConfiguration;
import io.selendroid.android.AndroidApp;

import java.io.File;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selendroid.exceptions.SelendroidException;

public class SelendroidDriverTests {
  private static final String APK_FILE = "src/test/resources/selendroid-test-app.apk";
  private static final String INVALID_APK_FILE =
      "src/test/resources/selendroid-test-app-invalid.apk";

  @Test
  public void testShouldBeAbleToInitDriver() throws Exception {
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.addSupportedApp(new File(APK_FILE).getAbsolutePath());
    SelendroidDriver driver = new SelendroidDriver(conf);
    assertThatTestappHasBeenSuccessfullyRegistered(driver);
  }

  @Test
  public void testShouldBeAbleToInitDriverAndIgnoreInvalidEntry() throws Exception {
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.addSupportedApp(new File(APK_FILE).getAbsolutePath());
    conf.addSupportedApp(new File(INVALID_APK_FILE).getAbsolutePath());
    SelendroidDriver driver = new SelendroidDriver(conf);
    assertThatTestappHasBeenSuccessfullyRegistered(driver);
  }

  @Test
  public void testShouldNotbBeAbleInitDriverWithoutAnyConfig() {
    try {
      new SelendroidDriver(new SelendroidConfiguration());
    } catch (SelendroidException e) {
      Assert.assertEquals("Configuration error - no apps has been configured.", e.getMessage());
    }
  }

  @Test
  public void testShouldnotBeAbleToInitDriverIfNoValidAppIsAvailable() throws Exception {
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.addSupportedApp(new File(INVALID_APK_FILE).getAbsolutePath());
    try {
      new SelendroidDriver(conf);
      Assert
          .fail("With complete invalid app configuration the driver should not be able to initialize.");
    } catch (SelendroidException e) {
      Assert.assertEquals(
          "Fatal error initializing SelendroidDriver: configured app(s) were not been found.",
          e.getMessage());
    }
  }

  protected void assertThatTestappHasBeenSuccessfullyRegistered(SelendroidDriver driver) {
    Map<String, AndroidApp> apps = driver.getConfiguredApps();
    Assert.assertTrue("expecting 1 test app has been registered but was " + apps.size(),
        apps.size() == 1);
    final String key = "org.openqa.selendroid.testapp:0.4-SNAPSHOT";
    Assert.assertTrue("expecting test app has been registered with the right key",
        apps.containsKey(key));
  }
}
