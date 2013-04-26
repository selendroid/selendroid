package io.selendroid.builder;

import io.selendroid.android.AndroidSdk;
import io.selendroid.io.ShellCommand;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SelendroidServerBuilderTest {
  private static final String apkFile = "src/test/resources/selendroid-test-app.apk";
  private static final String selendroidPrebuidlServer = "src/test/resources/selendroid-server.apk";

  @Test
  public void testShouldBeAbleToCreateCustomizedSelendroidServerAndCleantTUp() throws Exception {
    SelendroidServerBuilder builder = new SelendroidServerBuilder(selendroidPrebuidlServer);
    builder.init(apkFile);
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

  void assertResultDoesNotContainFile(String output, String file) {
    if (output.contains(file)) {
      Assert.fail("Output does contain the file: " + file);
    }
  }
}
