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
package io.selendroid.android.impl;

import io.selendroid.android.AndroidApp;
import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.exceptions.ShellCommandException;

public class InstalledAndroidApp implements AndroidApp {

  private String packageName;
  private String activityName;
  private String version;

  public InstalledAndroidApp(String appInfo) {
    String[] pieces = appInfo.split(":");
    if (pieces.length > 2 || pieces.length == 0) {
      throw new RuntimeException("Format for installed app is:  tld.company.app/ActivityClass:version");
    }
    if (pieces.length == 2) {
      version = pieces[1];
    } else {
      version = "UNKNOWN";
    }
    pieces = pieces[0].split("/");
    if (pieces.length != 2) {
      throw new RuntimeException("Format for installed app is:  tld.company.app/ActivityClass:version");
    }
    packageName = pieces[0];
    activityName = pieces[1];
  }

  @Override
  public String getBasePackage() throws AndroidSdkException {
    return packageName;
  }

  @Override
  public String getMainActivity() throws AndroidSdkException {
    return packageName + "." + activityName;
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
