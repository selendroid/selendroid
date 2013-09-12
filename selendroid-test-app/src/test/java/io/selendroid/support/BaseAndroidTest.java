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

import io.selendroid.SelendroidCapabilities;
import io.selendroid.SelendroidDriver;
import io.selendroid.SelendroidLauncher;
import io.selendroid.device.DeviceTargetPlatform;
import io.selendroid.waiter.TestWaiter;
import static io.selendroid.waiter.TestWaiter.waitFor;
import io.selendroid.waiter.WaitingConditions;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseAndroidTest {
  protected WebDriver driver = null;
  protected SelendroidLauncher selendroidServerLauncher = null;
  final String pathSeparator = File.separator;
  public static final String NATIVE_APP = "NATIVE_APP";
  public static final String WEBVIEW = "WEBVIEW";


  @BeforeMethod(alwaysRun = true)
  public void setup() throws Exception {
    driver = new SelendroidDriver("http://localhost:8080/wd/hub", getDefaultCapabilities());
  }

  @AfterMethod(alwaysRun = true)
  public void teardown() {
    if (driver != null) {
      driver.quit();
    }
  }

  protected void openWebdriverTestPage(String page) {
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    String activityClass = "io.selendroid.testapp." + "WebViewActivity";
    driver.switchTo().window(NATIVE_APP);
    driver.get("and-activity://" + activityClass);
    waitFor(WaitingConditions.driverUrlToBe(driver, "and-activity://WebViewActivity"));
    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Go to home screen")));
    WebElement spinner = driver.findElement(By.id("spinner_webdriver_test_data"));
    spinner.click();
    WebElement entry = TestWaiter.waitForElement(By.linkText(page), 10, driver);
    entry.click();

    driver.switchTo().window(WEBVIEW);
  }

  protected void openStartActivity() {
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    String activityClass = "io.selendroid.testapp." + "HomeScreenActivity";
    driver.switchTo().window(NATIVE_APP);
    driver.get("and-activity://" + activityClass);
    waitFor(WaitingConditions.driverUrlToBe(driver, "and-activity://HomeScreenActivity"));
  }

  protected DesiredCapabilities getDefaultCapabilities() {
    return SelendroidCapabilities.emulator(DeviceTargetPlatform.ANDROID16,
        "io.selendroid.testapp:0.4-SNAPSHOT");
  }

  protected void takeScreenShot(String message) throws Exception {
    File screenshot = ((SelendroidDriver) driver).getScreenshotAs(OutputType.FILE);
    String nameScreenshot = UUID.randomUUID().toString() + ".png";
    String path = getPath(nameScreenshot);
    FileUtils.copyFile(screenshot, new File(path));

    Reporter.log(message + "<br/><a href='" + path + "'> <img src='" + path
        + "' height='100' width='100'/> </a>");
  }

  private String getPath(String nameTest) throws IOException {
    File directory = new File(".");

    String newFileNamePath =
        directory.getCanonicalPath() + pathSeparator + "target" + pathSeparator
            + "surefire-reports" + pathSeparator + "screenShots" + pathSeparator + nameTest;
    return newFileNamePath;
  }
}
