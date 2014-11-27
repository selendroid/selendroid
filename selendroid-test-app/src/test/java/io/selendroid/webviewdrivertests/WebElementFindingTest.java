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

import static io.selendroid.client.waiter.TestWaiter.waitFor;
import static io.selendroid.client.waiter.WaitingConditions.pageTitleToBe;
import io.selendroid.client.waiter.TestWaiter;
import io.selendroid.support.BaseAndroidTest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Element finding test for the web view part of selendroid.
 * 
 * @author ddary
 */
public class WebElementFindingTest extends BaseAndroidTest {
  @Test()
  public void shouldNotFindElementById() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver().findElement(By.id("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindElementByCss() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver().findElement(By.cssSelector("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void shouldNotFindElementByXPath() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver().findElement(By.xpath("//*[@id='notThere']"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void shouldNotFindElementByTagName() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver().findElement(By.tagName("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void shouldNotFindElementByClass() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver().findElement(By.className("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void shouldNotFindElementByName() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver().findElement(By.name("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void shouldNotFindElementByText() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver().findElement(By.linkText("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void shouldNotFindElementByPartialText() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver().findElement(By.partialLinkText("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindElementsById() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver().findElements(By.id("nonExistantButton")));
  }

  @Test()
  public void shouldNotFindElementsByCss() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver().findElements(By.cssSelector("nonExistantButton")));
  }

  @Test
  public void shouldNotFindElementsByXPath() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver().findElements(By.xpath("//*[@id='notThere']")));
  }

  @Test
  public void shouldNotFindElementsByTagName() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver().findElements(By.tagName("notThere")));
  }

  @Test
  public void shouldNotFindElementsByClass() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver().findElements(By.className("notThere")));
  }

  private void assertListIsEmpty(List<WebElement> elements) {
    Assert.assertTrue("Expecting empty list when no elements are found.", elements.isEmpty());
  }

  @Test
  public void shouldNotFindElementsByName() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver().findElements(By.name("notThere")));
  }

  @Test
  public void shouldNotFindElementsByText() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver().findElements(By.linkText("notThere")));
  }

  @Test
  public void shouldNotFindElementsByPartialText() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver().findElements(By.partialLinkText("notThere")));
  }

  @Test
  public void shouldFindElementByText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement clickMe = driver().findElement(By.linkText("click me"));
    clickMe.click();

    waitFor(pageTitleToBe(driver(), "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementByPartialText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    TestWaiter.waitForElement(By.partialLinkText("click"), 10, driver()).click();

    waitFor(pageTitleToBe(driver(), "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementById() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver().findElement(By.id("linkId")).click();
    waitFor(pageTitleToBe(driver(), "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementByCssSelector() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver().findElement(By.cssSelector("a[id='linkId']")).click();
    waitFor(pageTitleToBe(driver(), "We Arrive Here"), 25, TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementByXPath() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver().findElement(By.xpath("//a[@id='linkId']")).click();
    waitFor(pageTitleToBe(driver(), "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementByTagName() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement element = driver().findElement(By.tagName("a"));
    Assert.assertEquals(element.getText(), "Open new window");
  }

  @Test
  public void shouldFindElementByClass() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement element = driver().findElement(By.className("myTestClass"));
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void shouldFindElementByName() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);

    WebElement element = driver().findElement(By.name("nameTest"));
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void shouldFindElementsByText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement clickMe = driver().findElements(By.linkText("click me")).get(0);
    clickMe.click();

    waitFor(pageTitleToBe(driver(), "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementsByPartialText()
      throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement clickMe = driver().findElements(By.partialLinkText("click m")).get(0);
    clickMe.click();

    waitFor(pageTitleToBe(driver(), "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }


  @Test
  public void shouldFindElementsById() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    Thread.sleep(500);
    driver().findElements(By.id("linkId")).get(0).click();
    waitFor(pageTitleToBe(driver(), "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementsByCssSelector() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver().findElements(By.cssSelector("a[id='linkId']")).get(0).click();
    waitFor(pageTitleToBe(driver(), "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementsByXPath() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver().findElements(By.xpath("//a[@id='linkId']")).get(0).click();
    waitFor(pageTitleToBe(driver(), "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver().getTitle(), "We Arrive Here");
  }

  @Test
  public void shouldFindElementsByTagName() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement element = driver().findElements(By.tagName("a")).get(0);
    Assert.assertEquals(element.getText(), "Open new window");
  }

  @Test
  public void shouldFindElementsByClass() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement element = driver().findElements(By.className("myTestClass")).get(0);
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void shouldFindElementsByName() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement element = driver().findElements(By.name("nameTest")).get(0);
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void testShouldfindAnElementBasedOnId() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    waitFor(pageTitleToBe(driver(), "We Leave From Here"), 10, TimeUnit.SECONDS);

    WebElement element = driver().findElement(By.id("checky"));
    Assert.assertEquals(element.isSelected(), false);
  }

  @Test
  public void shouldGetBodyDOMElementViaJavascript() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement bodyByJS = (WebElement) driver().executeScript("return document.body;");
    Assert.assertEquals("body", bodyByJS.getTagName());
  }
}
