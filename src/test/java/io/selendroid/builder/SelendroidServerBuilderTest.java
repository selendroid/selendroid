package io.selendroid.builder;

import io.selendroid.android.AndroidSdk;
import io.selendroid.io.ShellCommand;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SelendroidServerBuilderTest {
  private static final String APK_FILE = "src/test/resources/selendroid-test-app.apk";
  private static final String SELENDROID_PREBUILD_SERVER =
      "src/test/resources/selendroid-server.apk";
  public static final String ANDROID_APPLICATION_XML_TEMPLATE = "src/main/resources/AndroidManifest.xml";

  @Test
  public void testShouldBeAbleToCreateCustomizedSelendroidServerAndCleantTUp() throws Exception {
    SelendroidServerBuilder builder =
        new SelendroidServerBuilder(SELENDROID_PREBUILD_SERVER, ANDROID_APPLICATION_XML_TEMPLATE);
    builder.init(APK_FILE);
    builder.cleanUpPrebuildServer();

    // Verify apk, if the files have been removed
    String line = AndroidSdk.aapt() + " list " + builder.getSelendroidServer().getAbsolutePath();
    String output = ShellCommand.exec(line);

    assertResultDoesNotContainFile(output, "META-INF/CERT.RSA");
    assertResultDoesNotContainFile(output, "META-INF/CERT.SF");
    assertResultDoesNotContainFile(output, "AndroidManifest.xml");
    // just double check that dexed classes are there
    try {
      assertResultDoesNotContainFile(output, "classes.dex");
      Assert.fail();
    } catch (java.lang.AssertionError e) {
      // expected, file should be there
    }
  }
  
  @Test
  public void testShouldBeAbleToCreateCustomizedAndroidApplicationXML() throws Exception {
    SelendroidServerBuilder builder =
        new SelendroidServerBuilder(SELENDROID_PREBUILD_SERVER, ANDROID_APPLICATION_XML_TEMPLATE);
    builder.init(APK_FILE);
    builder.createAndAddCustomizedAndroidManifestToSelendroidServer();
  }

  void assertResultDoesNotContainFile(String output, String file) {
    if (output.contains(file)) {
      Assert.fail("Output does contain the file: " + file);
    }
  }
}
