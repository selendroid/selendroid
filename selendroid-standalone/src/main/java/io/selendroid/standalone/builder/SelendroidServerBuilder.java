/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.standalone.builder;

import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.AndroidSdk;
import io.selendroid.standalone.android.JavaSdk;
import io.selendroid.standalone.android.impl.DefaultAndroidApp;
import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.standalone.exceptions.ShellCommandException;
import io.selendroid.standalone.io.ShellCommand;
import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelendroidServerBuilder {

  public static final String SELENDROID_TEST_APP_PACKAGE = "io.selendroid.testapp";
  private static final Logger log = Logger.getLogger(SelendroidServerBuilder.class.getName());
  public static final String SELENDROID_FINAL_NAME = "selendroid-server.apk";
  public static final String PREBUILD_SELENDROID_SERVER_PATH_PREFIX =
      "/prebuild/selendroid-server-";
  public static final String ANDROID_APPLICATION_XML_TEMPLATE = "/AndroidManifestTemplate.xml";
  public static final String ICON = "android:icon=\"@drawable/selenium_icon\"";
  private String selendroidPrebuildServerPath = null;
  private String selendroidApplicationXmlTemplate = null;
  private AndroidApp selendroidServer = null;
  private AndroidApp applicationUnderTest = null;
  private SelendroidConfiguration serverConfiguration = null;
  private String storepass = "android";
  private String alias = "androiddebugkey";
  private X509Certificate cert509;

  /**
   * FOR TESTING ONLY
   */
  /* package */SelendroidServerBuilder(String selendroidPrebuildServerPath,
                                       String selendroidApplicationXmlTemplate) {
    this(selendroidPrebuildServerPath, selendroidApplicationXmlTemplate, null);
  }

  /**
   * FOR TESTING ONLY
   */
  /* package */SelendroidServerBuilder(String selendroidPrebuildServerPath,
                                       String selendroidApplicationXmlTemplate,
                                       SelendroidConfiguration selendroidConfiguration) {
    this.selendroidPrebuildServerPath = selendroidPrebuildServerPath;
    this.selendroidApplicationXmlTemplate = selendroidApplicationXmlTemplate;
    this.serverConfiguration = selendroidConfiguration;
  }

  public SelendroidServerBuilder(SelendroidConfiguration selendroidConfiguration) {
    this(PREBUILD_SELENDROID_SERVER_PATH_PREFIX + getJarVersionNumber() + ".apk",
            ANDROID_APPLICATION_XML_TEMPLATE, selendroidConfiguration);
  }

  /* package */void init(AndroidApp aut) throws IOException, ShellCommandException {
    applicationUnderTest = aut;
    File customizedServer = File.createTempFile("selendroid-server", ".apk");
    if (deleteTmpFiles()) {
      customizedServer.deleteOnExit(); //Deletes temporary files created
    }
    log.info("Creating customized Selendroid-server: " + customizedServer.getAbsolutePath());
    InputStream is = getResourceAsStream(selendroidPrebuildServerPath);

    IOUtils.copy(is, new FileOutputStream(customizedServer));
    IOUtils.closeQuietly(is);
    selendroidServer = new DefaultAndroidApp(customizedServer);
  }

  public AndroidApp createSelendroidServer(AndroidApp aut) throws IOException,
                                                                  ShellCommandException,
                                                                  AndroidSdkException {
    log.info("create SelendroidServer for apk: " + aut.getAbsolutePath());
    init(aut);
    cleanUpPrebuildServer();
    File selendroidServer = createAndAddCustomizedAndroidManifestToSelendroidServer();
    File outputFile = File.createTempFile(
            String.format("selendroid-server-%s-%s", applicationUnderTest.getBasePackage(), getJarVersionNumber()),
            ".apk"
    );
    if (deleteTmpFiles()) {
      outputFile.deleteOnExit(); //Deletes file when done
    }
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

  public AndroidApp resignApp(File appFile) throws ShellCommandException, AndroidSdkException, IOException {
    AndroidApp app = new DefaultAndroidApp(appFile);
    // Delete existing certificates
    deleteFileFromAppSilently(app, "META-INF/MANIFEST.MF");
    deleteFileFromAppSilently(app, "META-INF/CERT.RSA");
    deleteFileFromAppSilently(app, "META-INF/CERT.SF");
    deleteFileFromAppSilently(app, "META-INF/ANDROIDD.SF");
    deleteFileFromAppSilently(app, "META-INF/ANDROIDD.RSA");
    deleteFileFromAppSilently(app, "META-INF/NDKEYSTO.SF");
    deleteFileFromAppSilently(app, "META-INF/NDKEYSTO.RSA");

    File outputFile = File.createTempFile("resigned-", appFile.getName());
    if (deleteTmpFiles()) {
      outputFile.deleteOnExit();
    }
    return signTestServer(appFile, outputFile);
  }

  /* package */File createAndAddCustomizedAndroidManifestToSelendroidServer() throws IOException,
                                                                                     ShellCommandException,
                                                                                     AndroidSdkException {
    String targetPackageName = applicationUnderTest.getBasePackage();

    File tmpDir = Files.createTempDir();
    if (deleteTmpFiles()) {
      tmpDir.deleteOnExit();
    }

    File customizedManifest = new File(tmpDir, "AndroidManifest.xml");
    if (deleteTmpFiles()) {
      customizedManifest.deleteOnExit();
    }
    log.info("Adding target package '" + targetPackageName + "' to "
             + customizedManifest.getAbsolutePath());

    // add target package
    InputStream inputStream = getResourceAsStream(selendroidApplicationXmlTemplate);
    if (inputStream == null) {
      throw new SelendroidException("AndroidApplication.xml template file was not found.");
    }
    String content = IOUtils.toString(inputStream, Charset.defaultCharset().displayName());

    // find the first occurance of "package" and appending the targetpackagename to begining
    int i = content.toLowerCase().indexOf("package");
    int cnt = 0;
    for (; i < content.length(); i++) {
      if (content.charAt(i) == '\"') {
        cnt++;
      }
      if (cnt == 2) {
        break;
      }
    }
    content = content.substring(0, i) + "." + targetPackageName + content.substring(i);
    log.info("Final Manifest File:\n" + content);
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

    File manifestApkFile = File.createTempFile("manifest", ".apk");
    if (deleteTmpFiles()) {
      manifestApkFile.deleteOnExit();
    }

    createManifestApk.addArgument("package", false);
    createManifestApk.addArgument("-M", false);
    createManifestApk.addArgument(customizedManifest.getAbsolutePath(), false);
    createManifestApk.addArgument("-I", false);
    createManifestApk.addArgument(AndroidSdk.androidJar(), false);
    createManifestApk.addArgument("-F", false);
    createManifestApk.addArgument(manifestApkFile.getAbsolutePath(), false);
    createManifestApk.addArgument("-f", false);
    log.info(ShellCommand.exec(createManifestApk, 20000L));

    ZipFile manifestApk =
        new ZipFile(manifestApkFile);
    ZipArchiveEntry binaryManifestXml = manifestApk.getEntry("AndroidManifest.xml");

    File finalSelendroidServerFile = File.createTempFile("selendroid-server", ".apk");
    if (deleteTmpFiles()) {
      finalSelendroidServerFile.deleteOnExit();
    }

    ZipArchiveOutputStream finalSelendroidServer =
        new ZipArchiveOutputStream(finalSelendroidServerFile);
    finalSelendroidServer.putArchiveEntry(binaryManifestXml);
    IOUtils.copy(manifestApk.getInputStream(binaryManifestXml), finalSelendroidServer);

    ZipFile selendroidPrebuildApk = new ZipFile(selendroidServer.getAbsolutePath());
    Enumeration<ZipArchiveEntry> entries = selendroidPrebuildApk.getEntries();
    for (; entries.hasMoreElements(); ) {
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

    if (!androidKeyStore.isFile()) {
      // create a new keystore
      CommandLine commandline = new CommandLine(JavaSdk.keytool());

      commandline.addArgument("-genkey", false);
      commandline.addArgument("-v", false);
      commandline.addArgument("-keystore", false);
      commandline.addArgument(androidKeyStore.toString(), false);
      commandline.addArgument("-storepass", false);
      commandline.addArgument(storepass, false);
      commandline.addArgument("-alias", false);
      commandline.addArgument(alias, false);
      commandline.addArgument("-keypass", false);
      commandline.addArgument(storepass, false);
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
    commandline.addArgument(getSigAlg(), false);
    commandline.addArgument("-digestalg", false);
    commandline.addArgument("SHA1", false);
    commandline.addArgument("-signedjar", false);
    commandline.addArgument(outputFileName.getAbsolutePath(), false);
    commandline.addArgument("-storepass", false);
    commandline.addArgument(storepass, false);
    commandline.addArgument("-keystore", false);
    commandline.addArgument(androidKeyStore.toString(), false);
    commandline.addArgument(customSelendroidServer.getAbsolutePath(), false);
    commandline.addArgument(alias, false);
    String output = ShellCommand.exec(commandline, 20000);
    if (log.isLoggable(Level.INFO)) {
      log.info("App signing output: " + output);
    }
    log.info("The app has been signed: " + outputFileName.getAbsolutePath());
    return new DefaultAndroidApp(outputFileName);
  }

  private File androidDebugKeystore() {
    if (serverConfiguration == null || serverConfiguration.getKeystore() == null) {
      return new File(FileUtils.getUserDirectory(), File.separatorChar + ".android"
                                                    + File.separatorChar + "debug.keystore");
    } else {
      if (serverConfiguration.getKeystorePassword() != null) {
        storepass = serverConfiguration.getKeystorePassword();
      }
      if (serverConfiguration.getKeystoreAlias() != null) {
        alias = serverConfiguration.getKeystoreAlias();
      }
      // there is a possibility that keystore path may be invalid due to user typo. Should we add a try catch?
      return new File(serverConfiguration.getKeystore());
    }
  }

  /**
   * Cleans the selendroid server by removing certificates and manifest file. <p/> Precondition:
   * {@link #init(AndroidApp)} must be called upfront for initialization
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
    InputStream is = getClass().getResourceAsStream(resource);
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

  // TODO this should go into a utility method
  public static String getJarVersionNumber() {
    Class clazz = SelendroidStandaloneDriver.class;
    String className = clazz.getSimpleName() + ".class";
    String classPath = clazz.getResource(className).toString();
    String version = "";

    if (!classPath.startsWith("jar")) {
      // Class not from JAR
      if(classPath.startsWith("file") && classPath.contains("target")) {
        try {
          version = getVersionFromPom(classPath.substring(5, classPath.lastIndexOf("target")));
        } catch (Exception e) {
          e.printStackTrace();
          return "";
        }
      } else return "dev";
    } else {
        try {
          version = getVersionFromManifest(classPath);
        } catch (IOException e){
          return "";
        }
    }
    return version;
  }

  private static String getVersionFromManifest(String path) throws IOException {
    String manifestPath = path.substring(0, path.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
    Manifest manifest = new Manifest(new URL(manifestPath).openStream());
    Attributes attr = manifest.getMainAttributes();
    return attr.getValue("version");
  }

  private static String getVersionFromPom(String path) throws Exception {
    path += "pom.xml";
    Pattern regex = Pattern.compile("<version>(.*?)</version>", Pattern.DOTALL);
    Matcher matcher = regex.matcher(FileUtils.readFileToString(new File(path)));
    if (matcher.find()) {
      return matcher.group(1);
    } else return "dev";
  }

  private String getSigAlg() {
    String sigAlg = "MD5withRSA";
    FileInputStream in;
    try {
      if (serverConfiguration != null) {
        String keystoreFile = serverConfiguration.getKeystore();
        if (keystoreFile != null) {
          in = new FileInputStream(keystoreFile);

          KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
          String keystorePassword = serverConfiguration.getKeystorePassword();
          char[] keystorePasswordCharArray = (keystorePassword == null)
                                             ? null : keystorePassword.toCharArray();
          if (keystorePasswordCharArray == null) {
            throw new RuntimeException("No keystore password configured.");
          }
          keystore.load(in, keystorePasswordCharArray);
          cert509 =
              (X509Certificate) keystore.getCertificate(serverConfiguration.getKeystoreAlias());
          sigAlg = cert509.getSigAlgName();
        }
      }
    } catch (Exception e) {
      log.log(Level.WARNING, String.format(
          "Error getting signature algorithm for jarsigner. Defaulting to %s. Reason: %s", sigAlg, e.getMessage()));
    }
    return sigAlg;
  }

  private boolean deleteTmpFiles() {
    return serverConfiguration != null
            && serverConfiguration.isDeleteTmpFiles();
  }
}
