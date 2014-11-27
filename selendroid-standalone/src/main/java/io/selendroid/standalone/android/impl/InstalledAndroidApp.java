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

import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.standalone.exceptions.ShellCommandException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstalledAndroidApp implements AndroidApp {

  private String packageName;
  private String activityName;
  private String version;

  public InstalledAndroidApp(String appInfo) {
    Pattern infoPattern = Pattern.compile("(.+):(.+)/(.+)");
    Matcher patternMatcher = infoPattern.matcher(appInfo);
    if (patternMatcher.matches()) {
      packageName = patternMatcher.group(1);
      version = patternMatcher.group(2);
      activityName = patternMatcher.group(3);
    } else {
      throw new RuntimeException("Format for installed app is:  tld.company.app:version/ActivityClass");
    }
  }

  @Override
  public String getBasePackage() throws AndroidSdkException {
    return packageName;
  }

  @Override
  public String getMainActivity() throws AndroidSdkException {
    return (activityName.contains(".")) ? activityName : packageName + "." + activityName;
  }

  public void setMainActivity(String mainActivity) {
    this.activityName = mainActivity;
  }

  @Override
  public String getVersionName() throws AndroidSdkException {
    return version;
  }

  @Override
  public void deleteFileFromWithinApk(String file) throws ShellCommandException, AndroidSdkException {
    // no-op
  }

  @Override
  public String getAppId() throws AndroidSdkException {
    return packageName + ":" + version;
  }

  @Override
  public String getAbsolutePath() {
    return null;
  }
}