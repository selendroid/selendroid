package io.selendroid.android;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AndroidAppTests {
  private static final String apkFile = "src/test/resources/selendroid-test-app.apk";

  @Test
  public void ShouldBeAbleToExtractBasePackage() throws Exception {
    AndroidApp app = new AndroidApp(new File(apkFile));
    Assert.assertEquals(app.getBasePackage(), "org.openqa.selendroid.testapp");
  }

  @Test
  public void ShouldBeAbleToExtractMainAcivity() throws Exception {
    AndroidApp app = new AndroidApp(new File(apkFile));
    Assert.assertEquals(app.getMainActivity(), "org.openqa.selendroid.testapp.HomeScreenActivity");
  }
}
