/*
 * Copyright 2014 eBay Software Foundation and selendroid committers.
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

import io.selendroid.client.Configuration;
import io.selendroid.client.DriverCommand;
import io.selendroid.support.BaseAndroidTest;
import io.selendroid.webviewdrivertests.HtmlTestData;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


public class CommandConfigurationTest extends BaseAndroidTest {
  @Test
  public void shouldGetAndSetCommandConfiguration() throws InterruptedException {
    openStartActivity();
    Configuration configurable = (Configuration) driver();

    Map config = configurable.getConfiguration(DriverCommand.SEND_KEYS_TO_ELEMENT);
    assertNativeEventsEnabledForSendKeys(config);

    configurable.setConfiguration(DriverCommand.SEND_KEYS_TO_ELEMENT, "nativeEvents", false);
    config = configurable.getConfiguration(DriverCommand.SEND_KEYS_TO_ELEMENT);
    assertNativeEventsDisabledForSendKeys(config);
  }

  @Test
  public void shouldSetJapaneseTextIntoNativeTextField() {
    openStartActivity();
    Configuration configurable = (Configuration) driver();

    configurable.setConfiguration(DriverCommand.SEND_KEYS_TO_ELEMENT, "nativeEvents", false);
    String text = "ありがとう";
    WebElement input = driver().findElement(By.id("my_text_field"));
    input.sendKeys(text);
    Assert.assertEquals(text, input.getText());
  }

  @Test
  public void shouldSetJapaneseTextIntoWebInputField() {
    openWebdriverTestPage(HtmlTestData.SAY_HELLO_DEMO);
    Configuration configurable = (Configuration) driver();

    configurable.setConfiguration(DriverCommand.SEND_KEYS_TO_ELEMENT, "nativeEvents", false);
    String text = "ありがとう";
    WebElement inputField = driver().findElement(By.id("name_input"));
    Assert.assertNotNull(inputField);
    inputField.clear();
    inputField.sendKeys(text);
    
    Assert.assertEquals(text, inputField.getAttribute("value"));
  }

  private void assertNativeEventsEnabledForSendKeys(Map config) {
    Assert.assertTrue("configuration map should contain 'nativeEvents' key.",
        config.containsKey("nativeEvents"));
    Assert.assertTrue("'nativeEvents' should be true", (Boolean) config.get("nativeEvents"));
  }

  private void assertNativeEventsDisabledForSendKeys(Map config) {
    Assert.assertTrue("configuration map should contain 'nativeEvents' key.",
        config.containsKey("nativeEvents"));
    Assert.assertFalse("'nativeEvents' should be disabled", (Boolean) config.get("nativeEvents"));
  }
}
