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

import io.selendroid.client.waiter.TestWaiter;
import io.selendroid.client.waiter.WaitingConditions;
import io.selendroid.support.BaseAndroidTest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Child element finding test for the web view part of selendroid.
 * 
 * @author ddary
 */
public class WebChildElementFindingTest extends BaseAndroidTest {
  public static final String ACTIVITY_CLASS = "io.selendroid.testapp." + "WebViewActivity";

  private void assertListIsEmpty(List<WebElement> elements) {
    Assert.assertTrue("Expecting empty list when no elements are found.", elements.isEmpty());
  }

  @Test()
  public void shouldNotFindElementById() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    try {
      rootElement.findElement(By.id("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindElementByCss() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    try {
      rootElement.findElement(By.cssSelector("a[id='nonExistantButton']"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void shouldNotFindElementByXPath() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    try {
      rootElement.findElement(By.xpath("//*[@id='notThere']"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void shouldNotFindElementByTagName() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    try {
      rootElement.findElement(By.tagName("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void shouldNotFindElementByClass() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    try {
      rootElement.findElement(By.className("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void shouldNotFindElementByName() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    try {
      rootElement.findElement(By.name("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void shouldNotFindElementByText() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    try {
      rootElement.findElement(By.linkText("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void shouldNotFindElementByPartialText() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    try {
      rootElement.findElement(By.partialLinkText("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindElementsById() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.cssSelector("a[id='nonExistantButton']")));
  }

  @Test
  public void shouldNotFindElementsByXPath() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.xpath("//*[@id='notThere']")));
  }

  @Test
  public void shouldNotFindElementsByTagName() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.tagName("notThere")));
  }

  @Test
  public void shouldNotFindElementsByClass() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.className("notThere")));
  }

  @Test
  public void shouldNotFindElementsByName() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.name("notThere")));
  }

  @Test
  public void shouldNotFindElementsByText() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.linkText("notThere")));
  }

  @Test
  public void shouldNotFindElementsByPartialText() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver().findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.partialLinkText("notThere")));
  }

  @Test
  public void shouldFindElementByText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    WebElement clickMe = rootElement.findElement(By.linkText("click me"));
    clickMe.click();

    TestWaiter.waitFor(WaitingConditions.pageTitleToBe(driver(), "We Arrive Here"), 15,
        TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementByPartialText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    WebElement clickMe = rootElement.findElement(By.partialLinkText("click m"));
    clickMe.click();

    TestWaiter.waitFor(WaitingConditions.pageTitleToBe(driver(), "We Arrive Here"), 15,
        TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }


  @Test
  public void shouldFindElementById() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    rootElement.findElement(By.id("linkId")).click();
    TestWaiter.waitFor(WaitingConditions.pageTitleToBe(driver(), "We Arrive Here"), 20,
        TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementByCss() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    WebElement e = rootElement.findElement(By.cssSelector("a[id='linkId']"));
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    e.click();
    TestWaiter.waitFor(WaitingConditions.pageTitleToBe(driver(), "We Arrive Here"), 15,
        TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementByXPath() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    rootElement.findElement(By.xpath("//a[@id='linkId']")).click();
    TestWaiter.waitFor(WaitingConditions.pageTitleToBe(driver(), "We Arrive Here"), 15,
        TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementByTagNameAndGetText() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    WebElement element = rootElement.findElement(By.tagName("a"));
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void shouldFindElementByClassAndGetText() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    WebElement element = rootElement.findElement(By.className("myTestClass"));
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void shouldFindElementByNameAndGetText() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    WebElement element = rootElement.findElement(By.name("nameTest"));
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void shouldFindElementsByTextAndClick() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    WebElement clickMe = rootElement.findElements(By.linkText("click me")).get(0);
    clickMe.click();

    TestWaiter.waitFor(WaitingConditions.pageTitleToBe(driver(), "We Arrive Here"), 15,
        TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementsByPartialTextAndClick()
      throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    WebElement clickMe = rootElement.findElements(By.partialLinkText("click m")).get(0);
    clickMe.click();

    TestWaiter.waitFor(WaitingConditions.pageTitleToBe(driver(), "We Arrive Here"), 15,
        TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }


  @Test
  public void shouldFindElementsByIdAndClick() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    rootElement.findElements(By.id("linkId")).get(0).click();
    TestWaiter.waitFor(WaitingConditions.pageTitleToBe(driver(), "We Arrive Here"), 15,
        TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementsByCssAndClick() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    rootElement.findElements(By.cssSelector("a[id='linkId']")).get(0).click();
    TestWaiter.waitFor(WaitingConditions.pageTitleToBe(driver(), "We Arrive Here"), 15,
        TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementsByXPathAndClick() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    rootElement.findElements(By.xpath("//a[@id='linkId']")).get(0).click();
    TestWaiter.waitFor(WaitingConditions.pageTitleToBe(driver(), "We Arrive Here"), 15,
        TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementsyTagNameAndGetText() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    WebElement element = rootElement.findElements(By.tagName("a")).get(0);
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void shouldFindElementsByClassAndGetText() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver().findElement(By.className("content"));
    WebElement element = rootElement.findElements(By.className("myTestClass")).get(0);
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void shouldFindElementsByNameAndGetText() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver().manage().timeouts().implicitlyWait(9, TimeUnit.SECONDS);
    WebElement rootElement = driver().findElement(By.className("content"));
    WebElement element = rootElement.findElements(By.name("nameTest")).get(0);
    Assert.assertEquals(element.getText(), "click me");
  }
}
