/*
 * Copyright 2013 selendroid committers.
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
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.io.ShellCommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selendroid.exceptions.SelendroidException;

import com.beust.jcommander.internal.Lists;

public class SelendroidServerBuilder {
  public static final String SELENDROID_TEST_APP_PACKAGE = "org.openqa.selendroid.testapp";
  private static final Logger log = Logger.getLogger(SelendroidServerBuilder.class.getName());
  public static final String SELENDROID_FINAL_NAME = "selendroid-server.apk";
  public static final String PREBUILD_SELENDROID_SERVER_PATH =
      "/prebuild/selendroid-server-0.4-SNAPSHOT.apk";
  public static final String ANDROID_APPLICATION_XML_TEMPLATE = "/AndroidManifest.xml";
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
    applicationUnderTest = new DefaultAndroidApp(new File(apkName));
    File customizedServer = File.createTempFile("selendroid-server", ".apk");

    log.info("Creating customized Selendroid-server: " + customizedServer.getAbsolutePath());
    InputStream is = getResourceAsStream(selendroidPrebuildServerPath);

    IOUtils.copy(is, new FileOutputStream(customizedServer));
    IOUtils.closeQuietly(is);
    selendroidServer = new DefaultAndroidApp(customizedServer);
  }

  public AndroidApp createSelendroidServer(String autFilePath) throws IOException,
      ShellCommandException, AndroidSdkException {
    log.info("create SelendroidServer for apk: " + autFilePath);
    init(autFilePath);
    cleanUpPrebuildServer();
    File file = createAndAddCustomizedAndroidManifestToSelendroidServer();
    return new DefaultAndroidApp(signTestServer(file));
  }

  /* package */File createAndAddCustomizedAndroidManifestToSelendroidServer() throws IOException,
      ShellCommandException, AndroidSdkException {
    String targetPackageName = applicationUnderTest.getBasePackage();
    File tempdir =
        new File(FileUtils.getTempDirectoryPath() + targetPackageName + System.currentTimeMillis());

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
    OutputStream outputStream = new FileOutputStream(customizedManifest);
    IOUtils.write(content, outputStream, Charset.defaultCharset().displayName());
    IOUtils.closeQuietly(inputStream);
    IOUtils.closeQuietly(outputStream);

    // adding the xml to an empty apk
    List<String> createManifestApk = Lists.newArrayList();
    createManifestApk.add(AndroidSdk.aapt());
    createManifestApk.add("package -M");
    createManifestApk.add(customizedManifest.getAbsolutePath());
    createManifestApk.add("-I");
    createManifestApk.add(AndroidSdk.androidJar());
    createManifestApk.add("-F");
    createManifestApk.add(tempdir.getAbsolutePath() + File.separatorChar + "manifest.apk");
    createManifestApk.add("-f");
    log.info(ShellCommand.exec(createManifestApk));

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

  /* package */File signTestServer(File customSelendroidServer) throws ShellCommandException, AndroidSdkException {
    File androidKeyStore = androidDebugKeystore();

    if (androidKeyStore.isFile() == false) {
      // create a new keystore
      List<String> createKeyStore = Lists.newArrayList();
      createKeyStore.add(JavaSdk.keytool());
      createKeyStore.add("-genkey");
      createKeyStore.add("-v");
      createKeyStore.add("-keystore");
      createKeyStore.add(androidKeyStore.toString());
      createKeyStore.add("-storepass android");
      createKeyStore.add("-alias androiddebugkey");
      createKeyStore.add("-keypass android");
      createKeyStore.add("-dname \"CN=Android Debug,O=Android,C=US\"");
      createKeyStore.add("-storetype JKS");
      createKeyStore.add("-sigalg MD5withRSA");
      createKeyStore.add("-keyalg RSA");
      String output = ShellCommand.exec(createKeyStore);
      log.info("A new keystore has been created: " + output);
    }
    File file =
        new File(getCurrentDir() + "selendroid-server-" + applicationUnderTest.getBasePackage()
            + "-0.4.0.apk");
    // Sign the jar
    List<String> signApkCommand = Lists.newArrayList();
    signApkCommand.add(JavaSdk.jarsigner());
    signApkCommand.add("-sigalg MD5withRSA");
    signApkCommand.add("-digestalg SHA1");
    signApkCommand.add("-signedjar");
    signApkCommand.add(file.getAbsolutePath());
    signApkCommand.add("-storepass android");
    signApkCommand.add("-keystore");
    signApkCommand.add(androidKeyStore.toString());
    signApkCommand.add(customSelendroidServer.getAbsolutePath());
    signApkCommand.add("androiddebugkey");
    String output = ShellCommand.exec(signApkCommand);
    if (log.isLoggable(Level.INFO)) {
      log.info("Server signing output: " + output);
    }
    log.info("The test server has been signed: " + file.getAbsolutePath());
    return file;
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
   * Precondition: {@link #init(String)} must be called upfront for initialization
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
}
