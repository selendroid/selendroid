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
package io.selendroid.standalone.server.e2e;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.common.device.DeviceTargetPlatform;

/*
 * Test is currently only executed in manual mode
 */
public class SessionCreationE2ETests {
  public static final String TEST_APP_ID = "io.selendroid.testapp:0.5.0-SNAPSHOT";

  @Ignore("Fail. Requires an active android emulator")
  @Test
  public void assertThatSessionCanBeExecutedOnAndroid10Emulator() throws Exception {
    testMethod(SelendroidCapabilities.emulator(DeviceTargetPlatform.ANDROID10, TEST_APP_ID));
  }

  @Ignore("Fail. Requires an active android emulator")
  @Test
  public void assertThatSessionCanBeExecutedOnAndroid16Emulator() throws Exception {
    testMethod(SelendroidCapabilities.emulator(DeviceTargetPlatform.ANDROID16, TEST_APP_ID));
  }

  @Ignore("Fail. Requires an active android emulator")
  @Test
  public void assertThatSessionCanBeExecutedOnAndroid17Device() throws Exception {
    SelendroidCapabilities capa =
        SelendroidCapabilities.device(DeviceTargetPlatform.ANDROID17, TEST_APP_ID);

    testMethod(capa);
  }

  private void testMethod(SelendroidCapabilities capa) throws Exception {
    WebDriver driver = new RemoteWebDriver(new URL("http://localhost:5555/wd/hub"), capa);
    String activityClass = "io.selendroid.testapp." + "HomeScreenActivity";
    driver.get("and-activity://" + activityClass);
    driver.getCurrentUrl();
    try {
      driver.findElement(By.id("not there"));
      Assert.fail();
    } catch (NoSuchElementException e) {
      // expected
    }

    WebElement inputField = driver.findElement(By.id("my_text_field"));
    Assert.assertEquals("true", inputField.getAttribute("enabled"));
    inputField.sendKeys("Selendroid");
    Assert.assertEquals("Selendroid", inputField.getText());
    driver.findElement(By.id("buttonStartWebview")).click();
    driver.switchTo().window("WEBVIEW");
    WebElement element = driver.findElement(By.id("name_input"));
    element.clear();
    ((JavascriptExecutor) driver)
        .executeScript("var inputs = document.getElementsByTagName('input');"
            + "for(var i = 0; i < inputs.length; i++) { "
            + "    inputs[i].value = 'helloJavascript';" + "}");
    Assert.assertEquals("helloJavascript", element.getAttribute("value"));
    driver.quit();
  }
}
