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
package io.selendroid.driver;

import io.selendroid.support.BaseAndroidTest;
import io.selendroid.webviewdrivertests.HtmlTestData;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebElement;

public class NavigationTests extends BaseAndroidTest {
  @Test(expected = UnsupportedCommandException.class)
  public void shouldNotGoForwardInNativeMode() {
    openStartActivity();
    driver().navigate().forward();
  }

  @Test(expected = UnsupportedCommandException.class)
  public void shouldNotDoRefreshInNativeMode() {
    openStartActivity();
    driver().navigate().refresh();
  }

  @Test()
  public void shouldGoBackInNativeMode() {
    openStartActivity();
    driver().findElement(By.id("startUserRegistration")).click();
    Assert.assertEquals(driver().getCurrentUrl(), "and-activity://RegisterUserActivity");
    driver().navigate().back();
    Assert.assertEquals(driver().getCurrentUrl(), "and-activity://HomeScreenActivity");
  }

  @Test()
  public void shouldNotGoForwardInWebviewMode() {
    openWebdriverTestPage(HtmlTestData.SAY_HELLO_DEMO);

    openWebdriverTestPage(HtmlTestData.TEST_CLICK_PAGE_1);
    driver().findElement(By.id("link")).click();
    Assert.assertEquals(driver().getCurrentUrl(), HtmlTestData.TEST_CLICK_PAGE_2);
    driver().navigate().back();
    Assert.assertEquals(driver().getCurrentUrl(), HtmlTestData.TEST_CLICK_PAGE_1);
    driver().navigate().forward();
    Assert.assertEquals(driver().getCurrentUrl(), HtmlTestData.TEST_CLICK_PAGE_2);
  }

  @Test()
  public void shouldNotDoRefreshInWebviewMode() throws Exception{
    openWebdriverTestPage(HtmlTestData.SAY_HELLO_DEMO);
    WebElement inputField = driver().findElement(By.id("name_input"));
    inputField.clear();
    inputField.sendKeys("a text");
    Assert.assertEquals("a text", inputField.getAttribute("value"));
    driver().navigate().refresh();
    inputField = driver().findElement(By.id("name_input"));
    Assert.assertEquals("Enter your name here!", inputField.getAttribute("value"));
  }

  @Test()
  public void shouldGoBackInWebviewMode() {
    openWebdriverTestPage(HtmlTestData.TEST_CLICK_PAGE_1);
    driver().findElement(By.id("link")).click();
    Assert.assertEquals(driver().getCurrentUrl(), HtmlTestData.TEST_CLICK_PAGE_2);
    driver().navigate().back();
    Assert.assertEquals(driver().getCurrentUrl(), HtmlTestData.TEST_CLICK_PAGE_1);
  }
}
