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
package org.openqa.selendroid.webviewdrivertests;

import org.openqa.selendroid.tests.internal.BaseAndroidTest;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Element finding test for the web view part of selendroid.
 * 
 * @author ddary
 */
public class ElementFindingTest extends BaseAndroidTest {

  protected void openWebView() {
    driver.findElement(By.linkText("Start Webview")).click();
    WebDriverWait wait = new WebDriverWait(driver, 5);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Go to home screen")));
  }

  protected void openWebdriverTestPage(String page) {
    driver.findElement(By.id("spinner_webdriver_test_data")).click();
    driver.findElement(By.linkText(page)).click();
    driver.switchTo().window("WEBVIEW");
  }

  @Test
  public void testShouldNotBeAbleToLocateASingleElementThatDoesNotExist() {
    openWebView();
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver.findElement(By.id("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }
}
