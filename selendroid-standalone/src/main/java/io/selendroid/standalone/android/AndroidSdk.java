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
package io.selendroid.standalone.android;

import static io.selendroid.standalone.android.OS.platformExecutableSuffixBat;
import static io.selendroid.standalone.android.OS.platformExecutableSuffixExe;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.standalone.exceptions.AndroidSdkException;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;

public class AndroidSdk {
  private static final String ANDROID_FOLDER_PREFIX = "android-";
  private static final String ANDROID_HOME = "ANDROID_HOME";

  private static String sAndroidHome;
  private static String sAndroidSdkVersion;
  private static String sBuildToolsVersion;


  public static File adb() {
    return new File(platformToolsHome(), "adb" + platformExecutableSuffixExe());
  }

  public static File aapt() throws AndroidSdkException {
    StringBuffer command = new StringBuffer();
    command.append("aapt");
    command.append(platformExecutableSuffixExe());
    File platformToolsAapt = new File(platformToolsHome(), command.toString());

    if (platformToolsAapt.isFile()) {
      return platformToolsAapt;
    }

    return new File(buildToolsFolder(), command.toString());
  }

  public static File buildToolsFolder() {
    File buildToolsFolder = buildToolsHome();

    if (sBuildToolsVersion != null) {
      return new File(buildToolsFolder, sBuildToolsVersion);
    } else {
      return findLatestAndroidPlatformFolder(
        buildToolsFolder,
        String.format(
          "Command 'aapt' was not found inside the Android SDK: %s. "
          + "Please update to the latest development tools and try again.",
          buildToolsFolder));
    }
  }

  public static File android() {
    StringBuffer command = new StringBuffer();
    command.append(toolsHome());

    return new File(toolsHome(), "android" + platformExecutableSuffixBat());
  }

  public static File emulator() {
    return new File(toolsHome(), "emulator" + platformExecutableSuffixExe());
  }

  private static File toolsHome() {
    StringBuffer command = new StringBuffer();
    command.append(androidHome());
    command.append(File.separator);
    command.append("tools");
    command.append(File.separator);
    return new File(command.toString());
  }

  private static File buildToolsHome() {
    StringBuffer command = new StringBuffer();
    command.append(androidHome());
    command.append(File.separator);
    command.append("build-tools");
    command.append(File.separator);

    return new File(command.toString());
  }

  private static File platformToolsHome() {
    StringBuffer command = new StringBuffer();
    command.append(androidHome());
    command.append(File.separator);
    command.append("platform-tools");
    command.append(File.separator);
    return new File(command.toString());
  }

  public static String androidHome() {
    if (sAndroidHome != null) {
      return sAndroidHome;
    }

    String androidHome =  System.getenv(ANDROID_HOME);

    if (androidHome == null) {
      throw new SelendroidException("Environment variable '" + ANDROID_HOME + "' was not found!");
    }
    return androidHome;
  }

  /**
   * @return path to android.jar of latest android api.
   */
  public static String androidJar() {
    return new File(androidSdkFolder(), "android.jar").getAbsolutePath();
  }

  public static File androidSdkFolder() {
    String platformsRootFolder = androidHome() + File.separator + "platforms";
    File platformsFolder = new File(platformsRootFolder);

    if (sAndroidSdkVersion != null) {
      return new File(platformsFolder, sAndroidSdkVersion);
    } else {
      return findLatestAndroidPlatformFolder(
        platformsFolder,
        "No installed Android APIs have been found.");
    }
  }

  protected static File findLatestAndroidPlatformFolder(File rootFolder, String errorMessage) {
    File[] androidApis = rootFolder.listFiles(new AndroidFileFilter());
    if (androidApis == null || androidApis.length == 0) {
      throw new SelendroidException(errorMessage);
    }
    Arrays.sort(androidApis, Collections.reverseOrder());
    return androidApis[0].getAbsoluteFile();
  }

  public static void setAndroidHome(String androidHome) {
    sAndroidHome = androidHome;
  }

  public static void setAndroidSdkVersion(String androidSdkVersion) {
    sAndroidSdkVersion = androidSdkVersion;
  }

  public static void setBuildToolsVersion(String buildToolsVersion) {
    sBuildToolsVersion = buildToolsVersion;
  }

  public static int getAndroidVersionNumber() {


    return 0;
  }

  public static File avdManager() {

    return null;
  }

  public static class AndroidFileFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
      String fileName = pathname.getName();

      String regex = "\\d{2}\\.\\d{1}\\.\\d{1}";
      if (fileName.matches(regex) || fileName.startsWith(ANDROID_FOLDER_PREFIX)) {
        return true;
      }
      return false;
    }
  }
}
