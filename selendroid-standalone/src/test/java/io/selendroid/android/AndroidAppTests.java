package io.selendroid.android;

import io.selendroid.exceptions.SelendroidException;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AndroidAppTests {
  private static final String apkFile = "src/test/resources/selendroid-test-app.apk";
  private static final String inValidApkFile = "src/test/resources/selendroid-test-app-invalid.apk";

  @Test
  public void testShouldBeAbleToExtractBasePackage() throws Exception {
    AndroidApp app = new AndroidApp(new File(apkFile));
    Assert.assertEquals(app.getBasePackage(), "org.openqa.selendroid.testapp");
  }

  @Test
  public void testShouldBeAbleToExtractMainAcivity() throws Exception {
    AndroidApp app = new AndroidApp(new File(apkFile));
    Assert.assertEquals(app.getMainActivity(), "org.openqa.selendroid.testapp.HomeScreenActivity");
  }

  @Test(expectedExceptions = {SelendroidException.class})
  public void testShouldNotBeAbleToExtractBasePackage() throws Exception {
    AndroidApp app = new AndroidApp(new File(inValidApkFile));
    app.getBasePackage();
  }

  @Test(expectedExceptions = {SelendroidException.class})
  public void testShouldNotBeAbleToExtractMainAcivity() throws Exception {
    AndroidApp app = new AndroidApp(new File(inValidApkFile));
    app.getMainActivity();
  }
}
