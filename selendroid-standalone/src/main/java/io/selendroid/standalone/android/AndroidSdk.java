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

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.standalone.exceptions.AndroidSdkException;

import static io.selendroid.standalone.android.OS.platformExecutableSuffixBat;
import static io.selendroid.standalone.android.OS.platformExecutableSuffixExe;

public class AndroidSdk {
  private static final String ANDROID_HOME = "ANDROID_HOME";
  private static final Logger log = Logger.getLogger(AndroidSdk.class.getName());
  private static String sAndroidHome;
  private static String sAndroidSdkVersion;
  private static String sBuildToolsVersion;
  private static String sAdbHome;

  private static final String PLATFORM_VERSION_REGEX = "android-(\\d+)$";
  private static final String BUILD_TOOLS_VERSION_REGEX = "(\\d+)\\.(\\d+)\\.(\\d+)";
  private static String sAvdManager;


  public static File adb() {
    return new File(
      sAdbHome != null ? new File(sAdbHome) : platformToolsHome(),
      "adb" + platformExecutableSuffixExe());
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
    if (sAndroidSdkVersion != null) {
      return new File(platformsFolder(), sAndroidSdkVersion);
    }
    return findLatestAndroidPlatformFolder();
  }

  private static File platformsFolder() {
    return new File(androidHome(), "platforms");
  }

  public static File findLatestAndroidPlatformFolder() {
    return findLatestAndroidPlatformFolder(platformsFolder());
  }

  public static File findLatestAndroidPlatformFolder(File platformsFolder) {
    File[] platformVersions = platformsFolder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().matches(PLATFORM_VERSION_REGEX);
      }
    });

    if (platformVersions == null || platformVersions.length == 0) {
      throw new SelendroidException("No valid Android platform folder found in " + platformsFolder.getName());
    }

    Pattern pattern = Pattern.compile(PLATFORM_VERSION_REGEX);
    Arrays.sort(
      platformVersions,
      new Comparator<File>() {
        @Override
        public int compare(File f1, File f2) {
          Matcher m1 = pattern.matcher(f1.getName());
          Matcher m2 = pattern.matcher(f2.getName());

          m1.find();
          m2.find();

          return Integer.compare(
            Integer.parseInt(m1.group(1)),
            Integer.parseInt(m2.group(1)));
        }
      });

    return platformVersions[platformVersions.length - 1].getAbsoluteFile();
  }

  public static File buildToolsFolder() {
    if (sBuildToolsVersion != null) {
      return new File(buildToolsHome(), sBuildToolsVersion);
    }
    return findLatestBuildToolsFolder();
  }

  public static File findLatestBuildToolsFolder() {
    return findLatestBuildToolsFolder(buildToolsHome());
  }

  public static File findLatestBuildToolsFolder(File buildToolsHome) {
    File[] buildToolsVersions = buildToolsHome.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().matches(BUILD_TOOLS_VERSION_REGEX);
      }
    });

    if (buildToolsVersions == null || buildToolsVersions.length == 0) {
      throw new SelendroidException("No valid build-tools versions found in " + buildToolsHome.getName());
    }

    Pattern pattern = Pattern.compile(BUILD_TOOLS_VERSION_REGEX);
    Arrays.sort(
      buildToolsVersions,
      new Comparator<File>() {
        @Override
        public int compare(File f1, File f2) {
          Matcher m1 = pattern.matcher(f1.getName());
          Matcher m2 = pattern.matcher(f2.getName());

          m1.find();
          m2.find();

          for (int i = 1; i <= 3; i++) {
            int compare = Integer.compare(
              Integer.parseInt(m1.group(i)),
              Integer.parseInt(m2.group(i)));

            if (compare != 0) {
              return compare;
            }
          }

          return 0;
        }
      });

    return buildToolsVersions[buildToolsVersions.length - 1].getAbsoluteFile();
  }

  public static void setAndroidHome(String androidHome) {
    sAndroidHome = androidHome;
  }

  public static void setAdbHome(String adbHome) {
    sAdbHome = adbHome;
  }

  public static void setAndroidSdkVersion(String androidSdkVersion) {
    sAndroidSdkVersion = androidSdkVersion;
  }

  public static void setBuildToolsVersion(String buildToolsVersion) {
    sBuildToolsVersion = buildToolsVersion;
  }
  public static File avdManager() {
    if (sAvdManager != null) {
      return new File(sAvdManager + platformExecutableSuffixExe());
    }
    else {
      return android();
    }
  }

  public static int getAndroidVersionNumber() {
    if (androidSdkFolder() != null) {
      String versionString = androidSdkFolder().getName();
      log.info("Android SDK folder name is: " + versionString);
      Pattern p = Pattern.compile(PLATFORM_VERSION_REGEX);
      Matcher matcher = p.matcher(versionString);
      if (matcher.matches()) {
        return Integer.parseInt(matcher.group(1));
      } else {
        log.warning("Could not identify Android SDK version number " +
                "from the sdk folder name !");
      }
    }
   throw new SelendroidException("Could not identify the Android SDK version number");
  }

  public static void setAvdManagerHome(String avdManager) {
    AndroidSdk.sAvdManager = avdManager;
  }
}
