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
package io.selendroid.nativetests;

import io.selendroid.support.BaseAndroidTest;

import org.junit.Assert;
import org.junit.Test;


public class BackgroundAppTest extends BaseAndroidTest {

  /**
   * Requires ANDROID_HOME to be set in enviroment.
   * @throws Exception
   */
  @Test
  public void testBackgroundAppFunctionality() throws Exception {
    openStartActivity();
    driver().backgroundApp();
    String currentActivity = adbCurrentActivity();
    Assert.assertTrue(currentActivity.contains("com.android.launcher2.Launcher"));
    driver().resumeApp();
    currentActivity = adbCurrentActivity();
    Assert.assertTrue(currentActivity.contains("io.selendroid.testapp.HomeScreenActivity"));
  }

  private String adbCurrentActivity() throws Exception {
    return execCmd(System.getenv("ANDROID_HOME")+"/platform-tools/"+"adb shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'");
  }

  public String execCmd(String cmd) throws java.io.IOException {
    Process proc = Runtime.getRuntime().exec(cmd);
    java.io.InputStream is = proc.getInputStream();
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    String val = "";
    if (s.hasNext()) {
        val = s.next();
    }
    else {
        val = "";
    }
    return val;
  }


}
