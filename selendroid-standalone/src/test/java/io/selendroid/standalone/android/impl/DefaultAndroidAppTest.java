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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.impl.DefaultAndroidApp;

public class DefaultAndroidAppTest {
  private static final String APK_FILE = "src/test/resources/selendroid-test-app.apk";
  private static final String INVALID_APK_FILE =
      "src/test/resources/selendroid-test-app-invalid.apk";

  @Test
  public void testShouldBeAbleToExtractBasePackage() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(APK_FILE));
    Assert.assertEquals(app.getBasePackage(), "io.selendroid.testapp");
  }

  @Test
  public void testShouldBeAbleToExtractMainAcivity() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(APK_FILE));
    Assert.assertEquals(app.getMainActivity(), "io.selendroid.testapp.HomeScreenActivity");
  }

  @Test()
  public void testShouldNotBeAbleToExtractBasePackage() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(INVALID_APK_FILE));
    try {
      app.getBasePackage();
      Assert.fail("On an invalid apk the base package should not be found.");
    } catch (SelendroidException e) {
      // expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToExtractMainActivity() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(INVALID_APK_FILE));
    try {
      app.getMainActivity();
      Assert.fail("On an invalid apk the main activity should not be found.");
    } catch (SelendroidException e) {
      // expected
    }
  }

  @Test
  public void testShouldBeAbleToExtractVersionName() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(APK_FILE));
    Assert.assertEquals("0.4-SNAPSHOT", app.getVersionName());
  }

  @Test
  public void testShouldNotBeAbleToExtractVersionNameFromInvalidApk() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(INVALID_APK_FILE));
    try {
      app.getVersionName();
      Assert.fail("On an invalid apk the versionName should not be found.");
    } catch (SelendroidException e) {
      // expected
    }
  }

  @Test
  public void testShouldBeAbleToGetAppId() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(APK_FILE));
    Assert.assertEquals("io.selendroid.testapp:0.4-SNAPSHOT", app.getAppId());
  }

  @Test
  public void testShouldNotBeAbleToExtractAppIdFromInvalidApk() throws Exception {
    AndroidApp app = new DefaultAndroidApp(new File(INVALID_APK_FILE));
    try {
      app.getAppId();
      Assert.fail("On an invalid apk the appId should not be found.");
    } catch (SelendroidException e) {
      // expected
    }
  }
}
