package io.selendroid.builder;

import io.selendroid.android.AndroidApp;
import io.selendroid.android.AndroidSdk;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.io.ShellCommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class SelendroidServerBuilder {
  public static final String PREBUILD_SELENDROID_SERVER_PATH = "/prebuild/selendroid-server.apk";
  private String selendroidPrebuildServerPath = null;
  private AndroidApp selendroidServer = null;
  private AndroidApp applicationUnderTest = null;

  /**
   * FOR TESTING ONLY
   */
  /* package */SelendroidServerBuilder(String selendroidPrebuildServerPath) {
    this.selendroidPrebuildServerPath = selendroidPrebuildServerPath;
  }

  public SelendroidServerBuilder() {
    this.selendroidPrebuildServerPath = PREBUILD_SELENDROID_SERVER_PATH;
  }

  /* package */void init(String apkName) throws IOException, ShellCommandException {
    applicationUnderTest = new AndroidApp(new File(apkName));
    File customizedServer = File.createTempFile("selendroid-server", "apk");

    System.out.println("Creating cutsomized Selendroid-server: "
        + customizedServer.getAbsolutePath());
    InputStream is = null;
    // switch needed for testability
    if (selendroidPrebuildServerPath.startsWith("/prebuild/")) {
      is = getClass().getResourceAsStream(selendroidPrebuildServerPath);
    } else {
      is = new FileInputStream(new File(selendroidPrebuildServerPath));
    }
    if (is == null) {
      throw new SelendroidException("File not found");
    }
    IOUtils.copy(is, new FileOutputStream(customizedServer));
    selendroidServer = new AndroidApp(customizedServer);
  }

  public void createSelendroidServer(String apkName) throws IOException, ShellCommandException {
    init(apkName);
    cleanUpPrebuildServer();
  }

  /**
   * Cleans the selendroid server by removing certificates and manifest file.
   * 
   * Precondition: {@link #init(String)} must be called upfront for initialization
   * 
   * @throws ShellCommandException
   */
  /* package */void cleanUpPrebuildServer() throws ShellCommandException {
    selendroidServer.deleteFileFromWithinApk("META-INF/CERT.RSA");
    selendroidServer.deleteFileFromWithinApk("META-INF/CERT.SF");
    selendroidServer.deleteFileFromWithinApk("AndroidManifest.xml");
  }

  /**
   * for testing only
   */
  /* package */AndroidApp getSelendroidServer() {
    return selendroidServer;
  }

  /**
   * for testing only
   */
  /* package */AndroidApp getApplicationUnderTest() {
    return applicationUnderTest;
  }


}
