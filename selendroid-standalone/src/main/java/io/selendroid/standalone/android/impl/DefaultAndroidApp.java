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
package io.selendroid.standalone.android.impl;

import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.AndroidSdk;
import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.standalone.exceptions.ShellCommandException;
import io.selendroid.standalone.io.ShellCommand;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;


public class DefaultAndroidApp implements AndroidApp {
  private File apkFile;
  private String mainPackage = null;
  protected String mainActivity = null;
  private String versionName = null;

  public DefaultAndroidApp(File apkFile) {
    this.apkFile = apkFile;
  }

  private String extractApkDetails(String regex) throws ShellCommandException, AndroidSdkException {
    CommandLine line = new CommandLine(AndroidSdk.aapt());

    line.addArgument("dump", false);
    line.addArgument("badging", false);
    line.addArgument(apkFile.getAbsolutePath(), false);
    String output = ShellCommand.exec(line, 20000);

    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(output);
    if (matcher.find()) {
      return matcher.group(1);
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.selendroid.android.impl.AndroidAppA#getBasePackage()
   */
  @Override
  public String getBasePackage() throws AndroidSdkException {
    if (mainPackage == null) {
      try {
        mainPackage = extractApkDetails("package: name='(.*?)'");
      } catch (ShellCommandException e) {
        throw new SelendroidException("The base package name of the apk " + apkFile.getName()
            + " cannot be extracted.");
      }

    }
    return mainPackage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.selendroid.android.impl.AndroidAppA#getMainActivity()
   */
  @Override
  public String getMainActivity() throws AndroidSdkException {
    if (mainActivity == null) {
      try {
        mainActivity = extractApkDetails("launchable-activity: name='(.*?)'");
      } catch (ShellCommandException e) {
        throw new SelendroidException("The main activity of the apk " + apkFile.getName()
            + " cannot be extracted.");
      }
    }
    return mainActivity;
  }


  public void setMainActivity(String mainActivity) {
    this.mainActivity = mainActivity;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.selendroid.android.impl.AndroidAppA#deleteFileFromWithinApk(java.lang.String)
   */
  @Override
  public void deleteFileFromWithinApk(String file) throws ShellCommandException,
      AndroidSdkException {
    CommandLine line = new CommandLine(AndroidSdk.aapt());
    line.addArgument("remove", false);
    line.addArgument(apkFile.getAbsolutePath(), false);
    line.addArgument(file, false);

    ShellCommand.exec(line, 20000);
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.selendroid.android.impl.AndroidAppA#getAbsolutePath()
   */
  @Override
  public String getAbsolutePath() {
    return apkFile.getAbsolutePath();
  }

  @Override
  public String getVersionName() throws AndroidSdkException {
    if (versionName == null) {
      try {
        versionName = extractApkDetails("versionName='(.*?)'");
      } catch (ShellCommandException e) {
        throw new SelendroidException("The versionName of the apk " + apkFile.getName()
            + " cannot be extracted.");
      }
    }
    return versionName;
  }

  public String getAppId() throws AndroidSdkException {
    return getBasePackage() + ":" + getVersionName();
  }
}
