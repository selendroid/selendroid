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
package io.selendroid.webviewdrivertests;

import io.selendroid.support.BaseAndroidTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

public class FrameSwitchingTest extends BaseAndroidTest {

  @Before
  public void setupIframePage() {
    openWebdriverTestPage(HtmlTestData.IFRAME_PAGE);
  }

  @Test
  public void switchToFrameByWebElement() {
    verifyInMainPage();
    driver().switchTo().frame(driver().findElement(By.tagName("iframe")));
    verifyInsideFrame();
    driver().switchTo().defaultContent();
    verifyInMainPage();
  }

  @Test
  public void switchToFrameByIndex() {
    verifyInMainPage();
    driver().switchTo().frame(0);
    verifyInsideFrame();
    driver().switchTo().defaultContent();
    verifyInMainPage();
  }

  @Test
  public void switchToFrameByName() {
    verifyInMainPage();
    driver().switchTo().frame("iframe1-name");
    verifyInsideFrame();
    driver().switchTo().defaultContent();
    verifyInMainPage();
  }

  @Test
  public void switchToFrameById() {
    verifyInMainPage();
    driver().switchTo().frame("iframe1");
    verifyInsideFrame();
    driver().switchTo().defaultContent();
    verifyInMainPage();
  }

  @Test
  public void switchToFrameGetCurrentUrlAlwaysReturnsTop() {
    driver().switchTo().frame(0);
    Assert.assertEquals(HtmlTestData.IFRAME_PAGE, driver().getCurrentUrl());
  }

  @Test
  public void canClickOnElementInsideFrame() {
    driver().switchTo().frame(0);
    WebElement el = driver().findElement(By.tagName("a"));
    el.click();
    try {
      el.getText();
      throw new RuntimeException("should have gotten a stale element");
    } catch (StaleElementReferenceException sre) {
      // pass
    }
  }

  private void verifyInsideFrame() {
    Assert.assertEquals("Foo", driver().findElement(By.tagName("a")).getText());
  }

  private void verifyInMainPage() {
    Assert.assertEquals("This is the heading for the main page which contains an iframe",
        driver().findElement(By.id("iframe_page_heading")).getText());
  }
}
