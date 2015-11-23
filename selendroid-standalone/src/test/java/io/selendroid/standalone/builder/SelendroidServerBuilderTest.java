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

import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.AndroidSdk;
import io.selendroid.standalone.android.impl.DefaultAndroidApp;
import io.selendroid.standalone.builder.SelendroidServerBuilder;
import io.selendroid.standalone.io.ShellCommand;

import java.io.File;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class SelendroidServerBuilderTest {
  public static final String APK_FILE = "src/test/resources/selendroid-test-app.apk";
  public static final String SELENDROID_PREBUILD_SERVER =
      "src/test/resources/selendroid-server.apk";
  public static final String ANDROID_APPLICATION_XML_TEMPLATE =
      "src/test/resources/AndroidManifest.xml";
  public static SelendroidConfiguration selendroidConfiguration = null; 
  

  @Test
  public void testShouldBeAbleToCreateCustomizedSelendroidServerAndCleantTUp() throws Exception {
    SelendroidServerBuilder builder = getDefaultBuilder();
    builder.init(new DefaultAndroidApp(new File(APK_FILE)));
    builder.cleanUpPrebuildServer();

    // Verify apk, if the files have been removed
    CommandLine cmd = new CommandLine(AndroidSdk.aapt());
    cmd.addArgument("list", false);
    cmd.addArgument(builder.getSelendroidServer().getAbsolutePath(), false);

    String output = ShellCommand.exec(cmd);

    assertResultDoesNotContainFile(output, "META-INF/CERT.RSA");
    assertResultDoesNotContainFile(output, "META-INF/CERT.SF");
    assertResultDoesNotContainFile(output, "AndroidManifest.xml");
    // just double check that dexed classes are there

    assertResultDoesContainFile(output, "classes.dex");
  }

  @Test
  public void testShouldBeAbleToCreateCustomizedAndroidApplicationXML() throws Exception {
    SelendroidServerBuilder builder = getDefaultBuilder();
    builder.init(new DefaultAndroidApp(new File(APK_FILE)));
    builder.cleanUpPrebuildServer();
    File file = builder.createAndAddCustomizedAndroidManifestToSelendroidServer();
    ZipFile zipFile = new ZipFile(file);
    ZipArchiveEntry entry = zipFile.getEntry("AndroidManifest.xml");
    Assert.assertEquals(entry.getName(), "AndroidManifest.xml");
    Assert.assertTrue("Expecting non empty AndroidManifest.xml file", entry.getSize() > 700);

    // Verify that apk is not yet signed
    CommandLine cmd = new CommandLine(AndroidSdk.aapt());
    cmd.addArgument("list", false);
    cmd.addArgument(builder.getSelendroidServer().getAbsolutePath(), false);

    String output = ShellCommand.exec(cmd);

    assertResultDoesNotContainFile(output, "META-INF/CERT.RSA");
    assertResultDoesNotContainFile(output, "META-INF/CERT.SF");
  }

  @Test
  public void testShouldBeAbleToResignAnSignedApp() throws Exception {
    SelendroidServerBuilder builder = getDefaultBuilder();
    File androidApp = File.createTempFile("testapp", ".apk");
    FileUtils.copyFile(new File(APK_FILE), androidApp);

    AndroidApp resignedApp = builder.resignApp(androidApp);
    assertResignedApp(resignedApp, androidApp);

    // Verify that apk is signed
    CommandLine cmd = new CommandLine(AndroidSdk.aapt());
    cmd.addArgument("list", false);
    cmd.addArgument(resignedApp.getAbsolutePath(), false);

    String output = ShellCommand.exec(cmd);

    assertResultDoesNotContainFile(output, "META-INF/CERT.RSA");
    assertResultDoesNotContainFile(output, "META-INF/CERT.SF");
    assertResultDoesContainFile(output, "META-INF/ANDROIDD.SF");
    assertResultDoesContainFile(output, "META-INF/ANDROIDD.RSA");
    assertResultDoesContainFile(output, "AndroidManifest.xml");
  }
  
  @Test
  public void testShouldBeAbleToResignAnSignedAppWithCustomKeystore() throws Exception {
    SelendroidServerBuilder builder = getDefaultBuilderWithCustomKeystore();
    File androidApp = File.createTempFile("testapp", ".apk");
    System.out.println("App name: " + androidApp.getName());
    FileUtils.copyFile(new File(APK_FILE), androidApp);

    AndroidApp resignedApp = builder.resignApp(androidApp);
    assertResignedApp(resignedApp, androidApp);

    // Verify that apk is signed
    CommandLine cmd = new CommandLine(AndroidSdk.aapt());
    cmd.addArgument("list", false);
    cmd.addArgument(resignedApp.getAbsolutePath(), false);

    String output = ShellCommand.exec(cmd);
    String sigFileName = selendroidConfiguration.getKeystoreAlias().toUpperCase();
    if(sigFileName.length() > 8)
    {
    	sigFileName = sigFileName.substring(0, 8);
    }

    assertResultDoesNotContainFile(output, "META-INF/CERT.RSA");
    assertResultDoesNotContainFile(output, "META-INF/CERT.SF");
    assertResultDoesContainFile(output, "META-INF/"+ sigFileName + ".SF");
    assertResultDoesContainFile(output, "META-INF/" + sigFileName + ".RSA");
    assertResultDoesContainFile(output, "AndroidManifest.xml");
  }

  @Test
  public void testShouldBeAbleToCreateASignedSelendroidServer() throws Exception {
    SelendroidServerBuilder builder = getDefaultBuilder();
    builder.init(new DefaultAndroidApp(new File(APK_FILE)));
    builder.cleanUpPrebuildServer();
    File file = File.createTempFile("testserver", "apk");
    builder.signTestServer(builder.createAndAddCustomizedAndroidManifestToSelendroidServer(), file);

    // Verify that apk is signed
    CommandLine cmd = new CommandLine(AndroidSdk.aapt());
    cmd.addArgument("list", false);
    cmd.addArgument(file.getAbsolutePath(), false);

    String output = ShellCommand.exec(cmd);

    assertResultDoesNotContainFile(output, "META-INF/CERT.RSA");
    assertResultDoesNotContainFile(output, "META-INF/CERT.SF");
    assertResultDoesContainFile(output, "META-INF/ANDROIDD.SF");
    assertResultDoesContainFile(output, "META-INF/ANDROIDD.RSA");
    assertResultDoesContainFile(output, "AndroidManifest.xml");
  }
  
  @Test
  public void testShouldBeAbleToCreateASignedSelendroidServerWithCustomKeystore() throws Exception {
    SelendroidServerBuilder builder = getDefaultBuilderWithCustomKeystore();
    builder.init(new DefaultAndroidApp(new File(APK_FILE)));
    builder.cleanUpPrebuildServer();
    File file = File.createTempFile("testserver1", "apk");
    builder.signTestServer(builder.createAndAddCustomizedAndroidManifestToSelendroidServer(), file);

    // Verify that apk is signed
    CommandLine cmd = new CommandLine(AndroidSdk.aapt());
    cmd.addArgument("list", false);
    cmd.addArgument(file.getAbsolutePath(), false);

    String output = ShellCommand.exec(cmd);
    String sigFileName = selendroidConfiguration.getKeystoreAlias().toUpperCase();
    if(sigFileName.length() > 8)
    {
    	sigFileName = sigFileName.substring(0, 8);
    }

    assertResultDoesNotContainFile(output, "META-INF/CERT.RSA");
    assertResultDoesNotContainFile(output, "META-INF/CERT.SF");
    assertResultDoesContainFile(output, "META-INF/"+ sigFileName + ".SF");
    assertResultDoesContainFile(output, "META-INF/" + sigFileName + ".RSA");
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

  private static void assertResignedApp(AndroidApp resignedApp, File originalApp) {
    String resignedName =  new File(resignedApp.getAbsolutePath()).getName();
    Assert.assertTrue(resignedName.startsWith("resigned-"));
    Assert.assertTrue(resignedName.endsWith(originalApp.getName()));
  }

  public static SelendroidServerBuilder getDefaultBuilder() {
    return new SelendroidServerBuilder(SELENDROID_PREBUILD_SERVER, ANDROID_APPLICATION_XML_TEMPLATE);
  }
  
  public static SelendroidServerBuilder getDefaultBuilderWithCustomKeystore() {
	  selendroidConfiguration = new SelendroidConfiguration(); 
	  selendroidConfiguration.setKeystore("src/test/resources/selendriodtest1.keystore");
	  selendroidConfiguration.setKeystorePassword("selendroid");
	  selendroidConfiguration.setKeystoreAlias("selendroid1");
	  return new SelendroidServerBuilder(SELENDROID_PREBUILD_SERVER, ANDROID_APPLICATION_XML_TEMPLATE,selendroidConfiguration);
  }
}
