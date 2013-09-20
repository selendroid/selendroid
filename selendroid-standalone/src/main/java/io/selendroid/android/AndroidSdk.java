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
package io.selendroid.android;

import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.exceptions.SelendroidException;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;

public class AndroidSdk {
  public static final String ANDROID_FOLDER_PREFIX = "android-";
  public static final String ANDROID_HOME = "ANDROID_HOME";

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

    File buildToolsFolder = new File(buildToolsHome());

    return new File(
        findLatestAndroidPlatformFolder(
            buildToolsFolder,
            "Command 'aapt' was not found inside the Android SDK. Please update to the latest development tools and try again."),
        command.toString());
  }

  public static String android() {
    StringBuffer command = new StringBuffer();
    command.append(toolsHome());
    command.append("android");
    command.append(platformExecutableSuffixBat());
    return command.toString();
  }

  public static String emulator() {
    StringBuffer command = new StringBuffer();
    command.append(toolsHome());
    command.append("emulator");
    command.append(platformExecutableSuffixExe());
    return command.toString();
  }

  private static String toolsHome() {
    StringBuffer command = new StringBuffer();
    command.append(androidHome());
    command.append(File.separator);
    command.append("tools");
    command.append(File.separator);
    return command.toString();
  }

  private static String buildToolsHome() {
    StringBuffer command = new StringBuffer();
    command.append(androidHome());
    command.append(File.separator);
    command.append("build-tools");
    command.append(File.separator);

    return command.toString();
  }

  private static String platformToolsHome() {
    StringBuffer command = new StringBuffer();
    command.append(androidHome());
    command.append(File.separator);
    command.append("platform-tools");
    command.append(File.separator);
    return command.toString();
  }

  public static String androidHome() {
    String androidHome = System.getenv(ANDROID_HOME);

    if (androidHome == null) {
      throw new SelendroidException("Environment variable '" + ANDROID_HOME + "' was not found!");
    }
    return androidHome;
  }

  /* package */static String platformExecutableSuffixExe() {
    return isWindows() ? ".exe" : "";
  }

  public static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
  }

  /* package */static String platformExecutableSuffixBat() {
    return isWindows() ? ".bat" : "";
  }

  /**
   * @return path to android.jar of latest android api.
   */
  public static String androidJar() {
    String platformsRootFolder = androidHome() + File.separator + "platforms";
    File platformsFolder = new File(platformsRootFolder);

    return new File(findLatestAndroidPlatformFolder(platformsFolder,
        "No installed Android APIs have been found."), "android.jar").getAbsolutePath();
  }

  protected static File findLatestAndroidPlatformFolder(File rootFolder, String errorMessage) {
    File[] androidApis = rootFolder.listFiles(new AndroidFileFilter());
    if (androidApis == null || androidApis.length == 0) {
      throw new SelendroidException(errorMessage);
    }
    Arrays.sort(androidApis, Collections.reverseOrder());
    return androidApis[0].getAbsoluteFile();
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