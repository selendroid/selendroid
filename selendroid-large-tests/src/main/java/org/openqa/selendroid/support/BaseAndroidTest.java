/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.support;

import static org.openqa.selendroid.waiter.TestWaiter.waitFor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selendroid.waiter.TestWaiter;
import org.openqa.selendroid.waiter.WaitingConditions;
import org.openqa.selendroid.webviewdrivertests.HtmlTestData;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.android.AndroidDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseAndroidTest {
  protected WebDriver driver = null;
  final String pathSeparator = File.separator;
  public static final String NATIVE_APP = "NATIVE_APP";
  public static final String WEBVIEW = "WEBVIEW";


  @BeforeMethod(alwaysRun = true)
  public void setup() throws MalformedURLException {
    driver = new AndroidDriver(new URL("http://localhost:8080/wd/hub"), getDefaultCapabilities());
  }

  @AfterMethod(alwaysRun = true)
  public void teardown() {
    driver.quit();
  }

  protected void openWebdriverTestPage(String page) {
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    String activityClass = "org.openqa.selendroid.testapp." + "WebViewActivity";
    driver.switchTo().window(NATIVE_APP);
    driver.get("and-activity://" + activityClass);
    waitFor(WaitingConditions.driverUrlToBe(driver, "and-activity://WebViewActivity"));
    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Go to home screen")));
    WebElement spinner = driver.findElement(By.id("spinner_webdriver_test_data"));
    spinner.click();
    // Hack: to work around the bug that an already open page will not be opened again.
//    driver.findElement(By.linkText(HtmlTestData.ABOUT_BLANK)).click();
//    try {
//      Thread.sleep(500);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
//    spinner.click();

    WebElement entry = TestWaiter.waitForElement(By.linkText(page), 10, driver);
    entry.click();

    driver.switchTo().window(WEBVIEW);
  }

  protected void openStartActivity() {
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    String activityClass = "org.openqa.selendroid.testapp." + "HomeScreenActivity";
    driver.switchTo().window(NATIVE_APP);
    driver.get("and-activity://" + activityClass);
    waitFor(WaitingConditions.driverUrlToBe(driver, "and-activity://HomeScreenActivity"));
  }

  protected DesiredCapabilities getDefaultCapabilities() {
    DesiredCapabilities capa = DesiredCapabilities.android();
    capa.setCapability("aut", "selendroid-test-app");
    capa.setCapability("locale", "de_DE");
    capa.setCapability("maxInstances", "1");
    capa.setCapability("browserName", "selendroid");
    return capa;
  }

  protected void takeScreenShot(String message) throws Exception {
    WebDriver augmentedDriver = new Augmenter().augment(driver);
    File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
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
