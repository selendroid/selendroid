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

import static org.openqa.selendroid.webviewdrivertests.waiter.TestWaiter.waitFor;
import static org.openqa.selendroid.webviewdrivertests.waiter.WaitingConditions.pageTitleToBe;

import java.util.concurrent.TimeUnit;

import org.openqa.selendroid.tests.internal.BaseAndroidTest;
import org.openqa.selendroid.webviewdrivertests.waiter.WaitingConditions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Element finding test for the web view part of selendroid.
 * 
 * @author ddary
 */
public class WebElementFindingTest extends BaseAndroidTest {
  public static final String ACTIVITY_CLASS = "org.openqa.selendroid.testapp." + "WebViewActivity";

  private void openWebdriverTestPage(String page) {
    driver.switchTo().window(NATIVE_APP);
    driver.get("and-activity://" + ACTIVITY_CLASS);
    waitFor(WaitingConditions.driverUrlToBe(driver, "and-activity://WebViewActivity"));
    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Go to home screen")));
    driver.findElement(By.id("spinner_webdriver_test_data")).click();
    driver.findElement(By.linkText(page)).click();
    driver.switchTo().window(WEBVIEW);
  }


  @Test()
  public void testShouldNotBeAbleToLocateASingleElementThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver.findElement(By.id("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldBeAbleToClickOnLinkIdentifiedByText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement clickMe = driver.findElement(By.linkText("click me"));
    clickMe.click();

    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testshouldBeAbleToClickOnLinkIdentifiedById() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver.findElement(By.id("linkId")).click();

    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldThrowAnExceptionWhenThereIsNoLinkToClickAndItIsFoundWithLinkText() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);

    try {
      driver.findElement(By.linkText("Not here either"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldfindAnElementBasedOnId() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    waitFor(pageTitleToBe(driver, "We Leave From Here"), 10, TimeUnit.SECONDS);

    WebElement element = driver.findElement(By.id("checky"));
    Assert.assertEquals(element.isSelected(), false);
  }

  @Test
  public void testShouldNotBeAbleTofindElementsBasedOnIdIfTheElementIsNotThere() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver.findElement(By.id("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }
}
