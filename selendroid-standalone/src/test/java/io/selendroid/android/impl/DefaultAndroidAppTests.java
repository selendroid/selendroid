package io.selendroid.android.impl;

import io.selendroid.android.AndroidApp;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selendroid.exceptions.SelendroidException;

public class DefaultAndroidAppTests {
  private static final String APK_FILE = "src/test/resources/selendroid-test-app.apk";
  private static final String INVALID_APK_FILE =
      "src/test/resources/selendroid-test-app-invalid.apk";

  @Test
  public void testShouldBeAbleToExtractBasePackage() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(APK_FILE));
    Assert.assertEquals(app.getBasePackage(), "org.openqa.selendroid.testapp");
  }

  @Test
  public void testShouldBeAbleToExtractMainAcivity() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(APK_FILE));
    Assert.assertEquals(app.getMainActivity(), "org.openqa.selendroid.testapp.HomeScreenActivity");
  }

  @Test()
  public void testShouldNotBeAbleToExtractBasePackage() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(INVALID_APK_FILE));
    try {
      app.getBasePackage();
      Assert.fail("On an invalid apk the base package should not be found.");
    } catch (SelendroidException e) {
      // expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToExtractMainAcivity() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(INVALID_APK_FILE));
    try {
      app.getMainActivity();
      Assert.fail("On an invalid apk the main activity should not be found.");
    } catch (SelendroidException e) {
      // expected
    }
  }

  @Test
  public void testShouldBeAbleToExtractVersionName() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(APK_FILE));
    Assert.assertEquals("0.4-SNAPSHOT", app.getVersionName());
  }

  @Test
  public void testShouldNotBeAbleToExtractVersionNameFromInvalidApk() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(INVALID_APK_FILE));
    try {
      app.getVersionName();
      Assert.fail("On an invalid apk the versionName should not be found.");
    } catch (SelendroidException e) {
      // expected
    }
  }

  @Test
  public void testShouldBeAbleToGetAppId() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(APK_FILE));
    Assert.assertEquals("org.openqa.selendroid.testapp:0.4-SNAPSHOT", app.getAppId());
  }

  @Test
  public void testShouldNotBeAbleToExtractAppIdFromInvalidApk() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(INVALID_APK_FILE));
    try {
      app.getAppId();
      Assert.fail("On an invalid apk the appId should not be found.");
    } catch (SelendroidException e) {
      // expected
    }
  }
}
