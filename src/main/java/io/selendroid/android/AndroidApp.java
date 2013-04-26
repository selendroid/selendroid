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
package io.selendroid.android;

import io.selendroid.io.ShellCommand;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AndroidApp {
  private File apkFile;
  private String mainPackage = null;
  private String mainActivity = null;

  public AndroidApp(File apkFile) {
    this.apkFile = apkFile;
  }

  private String extractApkDetails(String regex) {
    String line = AndroidSdk.aapt() + " dump badging " + apkFile.getAbsolutePath();
    String output = ShellCommand.exec(line);

    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(output);
    if (matcher.find()) {
      return matcher.group(1);
    }

    return null;
  }

  public String getBasePackage() {
    if (mainPackage == null) {
      mainPackage = extractApkDetails("package: name='(.*?)'");
    }
    return mainPackage;
  }

  public String getMainActivity() {
    if (mainActivity == null) {
      mainActivity = extractApkDetails("launchable-activity: name='(.*?)'");
    }
    return mainActivity;
  }
}
