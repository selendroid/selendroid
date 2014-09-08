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
package io.selendroid.webviewdrivertests;

import io.selendroid.support.BaseAndroidTest;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class CookieHandlerTest extends BaseAndroidTest {

  private void setupWebView() {
    openWebdriverTestPage(HtmlTestData.ACTUAL_XHTML_PAGE);
    ((JavascriptExecutor) driver()).executeScript("window.location = 'http://www.google.com'");
    new WebDriverWait(driver(), 10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
        .cssSelector("input[name=q]")));
  }

  @Test
  public void shouldAddCookie() {
    setupWebView();
    Cookie cookie = new Cookie("name", "value", "/", null);
    Set<Cookie> cookies = new HashSet<Cookie>();
    cookies.add(cookie);
    driver().manage().addCookie(cookie);
    Assert.assertEquals(driver().manage().getCookies().containsAll(cookies), true);
  }

  @Test
  public void shouldDeleteNamedCookie() {
    setupWebView();
    driver().manage().deleteCookieNamed("name");
    Assert.assertEquals(driver().manage().getCookieNamed("name"), null);
  }

  @Test
  public void shouldDeleteAllCookies() {
    setupWebView();
    Cookie cookie = new Cookie("name", "value");
    driver().manage().addCookie(cookie);
    driver().manage().deleteAllCookies();
    Assert.assertEquals(driver().manage().getCookies().contains(cookie), false);
  }

  @Test
  public void shouldGetAllCookies() {
    setupWebView();
    Cookie cookie1 = new Cookie("name1", "value1");
    Cookie cookie2 = new Cookie("name2", "value2");
    Set<Cookie> cookies = new HashSet<Cookie>();
    cookies.add(cookie1);
    cookies.add(cookie2);
    driver().manage().addCookie(cookie1);
    driver().manage().addCookie(cookie2);
    Assert.assertEquals(driver().manage().getCookies().containsAll(cookies), true);
  }

  @Test
  public void shouldNotGetNonExistentCookies() {
    setupWebView();
    driver().manage().deleteAllCookies();
    Assert.assertEquals(driver().manage().getCookies().isEmpty(), true);

  }

  @Test
  public void shouldAddCookieAfterDeleting() {
    setupWebView();
    Cookie cookie1 = new Cookie("name1", "value1");
    Cookie cookie2 = new Cookie("name2", "value2");
    driver().manage().addCookie(cookie2);
    driver().manage().deleteAllCookies();
    driver().manage().addCookie(cookie1);
    Assert.assertEquals(driver().manage().getCookies().contains(cookie1), true);
    Assert.assertEquals(driver().manage().getCookies().contains(cookie2), false);
  }
  
  @Test
  public void shouldGetCookieValue() {
    setupWebView();
    String cookieName ="name1";
    String val = "value1=alsoWithEqualSign";
    Cookie cookie1 = new Cookie(cookieName,val);
    driver().manage().addCookie(cookie1);
    Assert.assertEquals(val, driver().manage().getCookieNamed(cookieName).getValue());
  }


}
