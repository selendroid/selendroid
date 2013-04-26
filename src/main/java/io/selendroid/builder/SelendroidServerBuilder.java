package io.selendroid.builder;

import io.selendroid.android.AndroidApp;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.ShellCommandException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

public class SelendroidServerBuilder {
  public static final String PREBUILD_SELENDROID_SERVER_PATH = "/prebuild/selendroid-server.apk";
  public static final String ANDROID_APPLICATION_XML_TEMPLATE = "AndroidManifest.xml";
  private String selendroidPrebuildServerPath = null;
  private String selendroidApplicationXmlTemplate = null;
  private AndroidApp selendroidServer = null;
  private AndroidApp applicationUnderTest = null;

  /**
   * FOR TESTING ONLY
   */
  /* package */SelendroidServerBuilder(String selendroidPrebuildServerPath,
      String selendroidApplicationXmlTemplate) {
    this.selendroidPrebuildServerPath = selendroidPrebuildServerPath;
    this.selendroidApplicationXmlTemplate = selendroidApplicationXmlTemplate;
  }

  public SelendroidServerBuilder() {
    this.selendroidPrebuildServerPath = PREBUILD_SELENDROID_SERVER_PATH;
    this.selendroidApplicationXmlTemplate = ANDROID_APPLICATION_XML_TEMPLATE;
  }

  /* package */void init(String apkName) throws IOException, ShellCommandException {
    applicationUnderTest = new AndroidApp(new File(apkName));
    File customizedServer = File.createTempFile("selendroid-server", "apk");

    System.out.println("Creating customized Selendroid-server: "
        + customizedServer.getAbsolutePath());
    InputStream is = null;
    // switch needed for testability
    if (selendroidPrebuildServerPath.startsWith("/prebuild/")) {
      is = getClass().getResourceAsStream(selendroidPrebuildServerPath);
    } else {
      is = new FileInputStream(new File(selendroidPrebuildServerPath));
    }
    if (is == null) {
      throw new SelendroidException("Prebuild selendroid server was not found.");
    }
    IOUtils.copy(is, new FileOutputStream(customizedServer));
    IOUtils.closeQuietly(is);
    selendroidServer = new AndroidApp(customizedServer);
  }

  public void createSelendroidServer(String apkName) throws IOException, ShellCommandException {
    init(apkName);
    cleanUpPrebuildServer();
  }

  /* package */void createAndAddCustomizedAndroidManifestToSelendroidServer() throws IOException {
    File customizedManifest = File.createTempFile("AndroidManifest", "xml");
    System.out.println("Creating customized AndroidManifest.xml: "
        + customizedManifest.getAbsolutePath());
    String targetPackageName = applicationUnderTest.getBasePackage();
    System.out.println("Adding target package '" + targetPackageName + "' to AndroidManifest.xml");

    // add target package
    InputStream inputStream = new FileInputStream(selendroidApplicationXmlTemplate);
    String content = IOUtils.toString(inputStream, Charset.defaultCharset().displayName());
    content = content.replaceAll("org.openqa.selendroid.testapp", targetPackageName);
    OutputStream outputStream=new FileOutputStream(customizedManifest);
    IOUtils.write(content, outputStream, Charset.defaultCharset().displayName());
    IOUtils.closeQuietly(inputStream);
    IOUtils.closeQuietly(outputStream);
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
