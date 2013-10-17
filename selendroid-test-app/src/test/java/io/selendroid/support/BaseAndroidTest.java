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
package io.selendroid.support;

import static io.selendroid.waiter.TestWaiter.waitFor;
import io.selendroid.SelendroidCapabilities;
import io.selendroid.SelendroidDriver;
import io.selendroid.SelendroidLauncher;
import io.selendroid.device.DeviceTargetPlatform;
import io.selendroid.waiter.WaitingConditions;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;


public class BaseAndroidTest {
  private WebDriver driver = null;
  protected SelendroidLauncher selendroidServerLauncher = null;
  final String pathSeparator = File.separator;
  public static final String NATIVE_APP = "NATIVE_APP";
  public static final String WEBVIEW = "WEBVIEW";

  public WebDriver driver() {
    return driver;
  }

  @Before
  public void setup() throws Exception {
    driver = new SelendroidDriver("http://localhost:38080/wd/hub", getDefaultCapabilities());
    driver().manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  @After
  public void teardown() {
    if (driver() != null) {
      driver().quit();
    }
  }

  protected void openWebdriverTestPage(String page) {
    driver().switchTo().window(NATIVE_APP);
    driver().get("and-activity://" + "io.selendroid.testapp." + "WebViewActivity");
    waitFor(WaitingConditions.driverUrlToBe(driver(), "and-activity://WebViewActivity"));

    driver().switchTo().window(WEBVIEW);
    driver().get(page);
    waitFor(WaitingConditions.driverUrlToBe(driver(), page));
  }

  protected void openStartActivity() {
    driver().switchTo().window(NATIVE_APP);
    driver().get("and-activity://io.selendroid.testapp.HomeScreenActivity");
    waitFor(WaitingConditions.driverUrlToBe(driver(), "and-activity://HomeScreenActivity"), 5,
        TimeUnit.SECONDS);
  }

  protected DesiredCapabilities getDefaultCapabilities() {
    return SelendroidCapabilities.device(DeviceTargetPlatform.ANDROID15,
        "io.selendroid.testapp:0.6.0-SNAPSHOT");
  }
}
