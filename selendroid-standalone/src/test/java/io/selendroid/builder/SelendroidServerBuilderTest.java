package io.selendroid.builder;

import io.selendroid.android.AndroidSdk;
import io.selendroid.io.ShellCommand;

import java.io.File;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.Assert;
import org.junit.Test;

public class SelendroidServerBuilderTest {
  public static final String APK_FILE = "src/test/resources/selendroid-test-app.apk";
  public static final String SELENDROID_PREBUILD_SERVER =
      "src/test/resources/selendroid-server.apk";
  public static final String ANDROID_APPLICATION_XML_TEMPLATE =
      "src/main/resources/AndroidManifest.xml";

  @Test
  public void testShouldBeAbleToCreateCustomizedSelendroidServerAndCleantTUp() throws Exception {
    SelendroidServerBuilder builder = getDefaultBuilder();
    builder.init(APK_FILE);
    builder.cleanUpPrebuildServer();

    // Verify apk, if the files have been removed
    String line = AndroidSdk.aapt() + " list " + builder.getSelendroidServer().getAbsolutePath();
    String output = ShellCommand.exec(line);

    assertResultDoesNotContainFile(output, "META-INF/CERT.RSA");
    assertResultDoesNotContainFile(output, "META-INF/CERT.SF");
    assertResultDoesNotContainFile(output, "AndroidManifest.xml");
    // just double check that dexed classes are there

    assertResultDoesContainFile(output, "classes.dex");
  }

  @Test
  public void testShouldBeAbleToCreateCustomizedAndroidApplicationXML() throws Exception {
    SelendroidServerBuilder builder = getDefaultBuilder();
    builder.init(APK_FILE);
    builder.cleanUpPrebuildServer();
    File file = builder.createAndAddCustomizedAndroidManifestToSelendroidServer();
    ZipFile zipFile = new ZipFile(file);
    ZipArchiveEntry entry = zipFile.getEntry("AndroidManifest.xml");
    Assert.assertEquals(entry.getName(), "AndroidManifest.xml");
    Assert.assertTrue("Expecting non empty AndroidManifest.xml file", entry.getSize() > 700);

    // Verify that apk is not yet signed
    String line = AndroidSdk.aapt() + " list " + builder.getSelendroidServer().getAbsolutePath();
    String output = ShellCommand.exec(line);

    assertResultDoesNotContainFile(output, "META-INF/CERT.RSA");
    assertResultDoesNotContainFile(output, "META-INF/CERT.SF");
  }

  @Test
  public void testShouldBeAbleToCreateASignedSelendroidServer() throws Exception {
    SelendroidServerBuilder builder = getDefaultBuilder();
    builder.init(APK_FILE);
    builder.cleanUpPrebuildServer();
    File file =
        builder.signTestServer(builder.createAndAddCustomizedAndroidManifestToSelendroidServer());

    // Verify that apk is not yet signed
    String line = AndroidSdk.aapt() + " list " + file.getAbsolutePath();
    String output = ShellCommand.exec(line);

    assertResultDoesNotContainFile(output, "META-INF/CERT.RSA");
    assertResultDoesNotContainFile(output, "META-INF/CERT.SF");
    assertResultDoesContainFile(output, "META-INF/ANDROIDD.SF");
    assertResultDoesContainFile(output, "META-INF/ANDROIDD.RSA");
    assertResultDoesContainFile(output, "AndroidManifest.xml");
  }

  void assertResultDoesNotContainFile(String output, String file) {
    if (output.contains(file)) {
      Assert.fail("Output does contain the file: " + file);
    }
  }

  void assertResultDoesContainFile(String output, String file) {
    if (!output.contains(file)) {
      Assert.fail("Output does not contain the file: " + file);
    }
  }

  public static SelendroidServerBuilder getDefaultBuilder() {
    return new SelendroidServerBuilder(SELENDROID_PREBUILD_SERVER, ANDROID_APPLICATION_XML_TEMPLATE);
  }
}
