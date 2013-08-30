/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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
package io.selendroid.builder;

import io.selendroid.android.AndroidApp;
import io.selendroid.android.AndroidSdk;
import io.selendroid.android.JavaSdk;
import io.selendroid.android.impl.DefaultAndroidApp;
import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.io.ShellCommand;
import io.selendroid.server.model.SelendroidStandaloneDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class SelendroidServerBuilder {
  public static final String SELENDROID_TEST_APP_PACKAGE = "io.selendroid.testapp";
  private static final Logger log = Logger.getLogger(SelendroidServerBuilder.class.getName());
  public static final String SELENDROID_FINAL_NAME = "selendroid-server.apk";
  public static final String PREBUILD_SELENDROID_SERVER_PATH_PREFIX =
      "/prebuild/selendroid-server-";
  public static final String ANDROID_APPLICATION_XML_TEMPLATE = "/AndroidManifest.xml";
  public static final String ICON = "android:icon=\"@drawable/selenium_icon\"";
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
    this.selendroidPrebuildServerPath =
        PREBUILD_SELENDROID_SERVER_PATH_PREFIX + getJarVersionNumber() + ".apk";
    this.selendroidApplicationXmlTemplate = ANDROID_APPLICATION_XML_TEMPLATE;
  }

  /* package */void init(AndroidApp aut) throws IOException, ShellCommandException {
    applicationUnderTest = aut;
    File customizedServer = File.createTempFile("selendroid-server", ".apk");

    log.info("Creating customized Selendroid-server: " + customizedServer.getAbsolutePath());
    InputStream is = getResourceAsStream(selendroidPrebuildServerPath);

    IOUtils.copy(is, new FileOutputStream(customizedServer));
    IOUtils.closeQuietly(is);
    selendroidServer = new DefaultAndroidApp(customizedServer);
  }

  public AndroidApp createSelendroidServer(AndroidApp aut) throws IOException,
      ShellCommandException, AndroidSdkException {
    log.info("create SelendroidServer for apk: " + aut.getAbsolutePath());
    init(aut);
    cleanUpPrebuildServer();
    File selendroidServer = createAndAddCustomizedAndroidManifestToSelendroidServer();
    File outputFile =
        new File(getCurrentDir() + "selendroid-server-" + applicationUnderTest.getBasePackage()
            + "-" + getJarVersionNumber() + ".apk");

    return signTestServer(selendroidServer, outputFile);
  }

  private void deleteFileFromAppSilently(AndroidApp app, String file) throws AndroidSdkException {
    if (app == null) {
      throw new IllegalArgumentException("Required parameter 'app' is null.");
    }
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Required parameter 'file' is null or empty.");
    }
    try {
      app.deleteFileFromWithinApk(file);
    } catch (ShellCommandException e) {
      // don't care, can happen if file does not exist
    }
  }

  public AndroidApp resignApp(File appFile) throws ShellCommandException, AndroidSdkException {
    AndroidApp app = new DefaultAndroidApp(appFile);
    // Delete existing certificates
    deleteFileFromAppSilently(app, "META-INF/MANIFEST.MF");
    deleteFileFromAppSilently(app, "META-INF/CERT.RSA");
    deleteFileFromAppSilently(app, "META-INF/CERT.SF");
    deleteFileFromAppSilently(app, "META-INF/ANDROIDD.SF");
    deleteFileFromAppSilently(app, "META-INF/ANDROIDD.RSA");


    File outputFile = new File(appFile.getParentFile(), "resigned-" + appFile.getName());
    return signTestServer(appFile, outputFile);
  }

  /* package */File createAndAddCustomizedAndroidManifestToSelendroidServer() throws IOException,
      ShellCommandException, AndroidSdkException {
    String targetPackageName = applicationUnderTest.getBasePackage();
    File tempdir =
        new File(FileUtils.getTempDirectoryPath() + File.separatorChar + targetPackageName
            + System.currentTimeMillis());

    if (!tempdir.exists()) {
      tempdir.mkdirs();
    }

    File customizedManifest = new File(tempdir, "AndroidManifest.xml");
    log.info("Adding target package '" + targetPackageName + "' to "
        + customizedManifest.getAbsolutePath());

    // add target package
    InputStream inputStream = getResourceAsStream(selendroidApplicationXmlTemplate);
    if (inputStream == null) {
      throw new SelendroidException("AndroidApplication.xml template file was not found.");
    }
    String content = IOUtils.toString(inputStream, Charset.defaultCharset().displayName());

    content = content.replaceAll(SELENDROID_TEST_APP_PACKAGE, targetPackageName);
    // Seems like this needs to be done
    if (content.contains(ICON)) {
      content = content.replaceAll(ICON, "");
    }

    OutputStream outputStream = new FileOutputStream(customizedManifest);
    IOUtils.write(content, outputStream, Charset.defaultCharset().displayName());
    IOUtils.closeQuietly(inputStream);
    IOUtils.closeQuietly(outputStream);

    // adding the xml to an empty apk
    CommandLine createManifestApk = new CommandLine(AndroidSdk.aapt());

    createManifestApk.addArgument("package", false);
    createManifestApk.addArgument("-M", false);
    createManifestApk.addArgument(customizedManifest.getAbsolutePath(), false);
    createManifestApk.addArgument("-I", false);
    createManifestApk.addArgument(AndroidSdk.androidJar(), false);
    createManifestApk.addArgument("-F", false);
    createManifestApk.addArgument(tempdir.getAbsolutePath() + File.separatorChar + "manifest.apk",
        false);
    createManifestApk.addArgument("-f", false);
    log.info(ShellCommand.exec(createManifestApk, 20000L));

    ZipFile manifestApk =
        new ZipFile(new File(tempdir.getAbsolutePath() + File.separatorChar + "manifest.apk"));
    ZipArchiveEntry binaryManifestXml = manifestApk.getEntry("AndroidManifest.xml");

    File finalSelendroidServerFile = new File(tempdir.getAbsolutePath() + "selendroid-server.apk");
    ZipArchiveOutputStream finalSelendroidServer =
        new ZipArchiveOutputStream(finalSelendroidServerFile);
    finalSelendroidServer.putArchiveEntry(binaryManifestXml);
    IOUtils.copy(manifestApk.getInputStream(binaryManifestXml), finalSelendroidServer);

    ZipFile selendroidPrebuildApk = new ZipFile(selendroidServer.getAbsolutePath());
    Enumeration<ZipArchiveEntry> entries = selendroidPrebuildApk.getEntries();
    for (; entries.hasMoreElements();) {
      ZipArchiveEntry dd = entries.nextElement();
      finalSelendroidServer.putArchiveEntry(dd);

      IOUtils.copy(selendroidPrebuildApk.getInputStream(dd), finalSelendroidServer);
    }

    finalSelendroidServer.closeArchiveEntry();
    finalSelendroidServer.close();
    manifestApk.close();
    log.info("file: " + finalSelendroidServerFile.getAbsolutePath());
    return finalSelendroidServerFile;
  }

  /* package */AndroidApp signTestServer(File customSelendroidServer, File outputFileName)
      throws ShellCommandException, AndroidSdkException {
    if (outputFileName == null) {
      throw new IllegalArgumentException("outputFileName parameter is null.");
    }
    File androidKeyStore = androidDebugKeystore();

    if (androidKeyStore.isFile() == false) {
      // create a new keystore
      CommandLine commandline = new CommandLine(JavaSdk.keytool());

      commandline.addArgument("-genkey", false);
      commandline.addArgument("-v", false);
      commandline.addArgument("-keystore", false);
      commandline.addArgument(androidKeyStore.toString(), false);
      commandline.addArgument("-storepass", false);
      commandline.addArgument("android", false);
      commandline.addArgument("-alias", false);
      commandline.addArgument("androiddebugkey", false);
      commandline.addArgument("-keypass", false);
      commandline.addArgument("android", false);
      commandline.addArgument("-dname", false);
      commandline.addArgument("CN=Android Debug,O=Android,C=US", false);
      commandline.addArgument("-storetype", false);
      commandline.addArgument("JKS", false);
      commandline.addArgument("-sigalg", false);
      commandline.addArgument("MD5withRSA", false);
      commandline.addArgument("-keyalg", false);
      commandline.addArgument("RSA", false);
      commandline.addArgument("-validity", false);
      commandline.addArgument("9999", false);

      String output = ShellCommand.exec(commandline, 20000);
      log.info("A new keystore has been created: " + output);
    }

    // Sign the jar
    CommandLine commandline = new CommandLine(JavaSdk.jarsigner());

    commandline.addArgument("-sigalg", false);
    commandline.addArgument("MD5withRSA", false);
    commandline.addArgument("-digestalg", false);
    commandline.addArgument("SHA1", false);
    commandline.addArgument("-signedjar", false);
    commandline.addArgument(outputFileName.getAbsolutePath(), false);
    commandline.addArgument("-storepass", false);
    commandline.addArgument("android", false);
    commandline.addArgument("-keystore", false);
    commandline.addArgument(androidKeyStore.toString(), false);
    commandline.addArgument(customSelendroidServer.getAbsolutePath(), false);
    commandline.addArgument("androiddebugkey", false);
    String output = ShellCommand.exec(commandline, 20000);
    if (log.isLoggable(Level.INFO)) {
      log.info("App signing output: " + output);
    }
    log.info("The app has been signed: " + outputFileName.getAbsolutePath());
    return new DefaultAndroidApp(outputFileName);
  }



  private File androidDebugKeystore() {
    return new File(FileUtils.getUserDirectory(), File.separatorChar + ".android"
        + File.separatorChar + "debug.keystore");
  }

  private String getCurrentDir() {
    return new File("").getAbsolutePath() + File.separatorChar;
  }

  /**
   * Cleans the selendroid server by removing certificates and manifest file.
   * 
   * Precondition: {@link #init(AndroidApp)} must be called upfront for initialization
   * 
   * @throws ShellCommandException
   * @throws AndroidSdkException
   */
  /* package */void cleanUpPrebuildServer() throws ShellCommandException, AndroidSdkException {
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

  /**
   * Loads resources as stream and the main reason for having the method is because it can be use
   * while testing and in production for loading files from within jar file.
   * 
   * @param resource The resource to load.
   * @return The input stream of the resource.
   * @throws SelendroidException if resource was not found.
   */
  private InputStream getResourceAsStream(String resource) {
    InputStream is = null;

    is = getClass().getResourceAsStream(resource);
    // switch needed for testability
    if (is == null) {
      try {
        is = new FileInputStream(new File(resource));
      } catch (FileNotFoundException e) {
        // do nothing
      }
    }
    if (is == null) {
      throw new SelendroidException("The resource '" + resource + "' was not found.");
    }
    return is;
  }

  public String getJarVersionNumber() {
    Class clazz = SelendroidStandaloneDriver.class;
    String className = clazz.getSimpleName() + ".class";
    String classPath = clazz.getResource(className).toString();
    if (!classPath.startsWith("jar")) {
      // Class not from JAR
      return "dev";
    }
    String manifestPath =
        classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
    Manifest manifest = null;
    try {
      manifest = new Manifest(new URL(manifestPath).openStream());
    } catch (Exception e) {
      return "";
    }
    Attributes attr = manifest.getMainAttributes();
    String value = attr.getValue("version");
    return value;
  }
}
