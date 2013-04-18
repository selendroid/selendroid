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

import static org.openqa.selendroid.waiter.TestWaiter.waitFor;
import static org.openqa.selendroid.waiter.WaitingConditions.pageTitleToBe;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selendroid.TestGroups;
import org.openqa.selendroid.support.BaseAndroidTest;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Child element finding test for the web view part of selendroid.
 * 
 * @author ddary
 */
@Test(groups={TestGroups.WEBVIEW})
public class WebChildElementFindingTest extends BaseAndroidTest {
  public static final String ACTIVITY_CLASS = "org.openqa.selendroid.testapp." + "WebViewActivity";

  private void assertListIsEmpty(List<WebElement> elements) {
    Assert.assertTrue(elements.isEmpty(), "Expecting empty list when no elements are found.");
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleElementByIdThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    try {
      rootElement.findElement(By.id("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleElementByCssSelectorThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    try {
      rootElement.findElement(By.cssSelector("a[id='nonExistantButton']"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldNotBeAbleToLocateASingleElementByXPathThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    try {
      rootElement.findElement(By.xpath("//*[@id='notThere']"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldNotBeAbleToLocateASingleElementByTagNameThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    try {
      rootElement.findElement(By.tagName("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldNotBeAbleToLocateASingleElementByClassThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    try {
      rootElement.findElement(By.className("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldNotBeAbleToLocateASingleElementByNameThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    try {
      rootElement.findElement(By.name("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldNotBeAbleToLocateASingleElementByTextThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    try {
      rootElement.findElement(By.linkText("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }
  
  @Test
  public void testShouldNotBeAbleToLocateASingleElementByPartialTextThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    try {
      rootElement.findElement(By.partialLinkText("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsByIdThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.id("nonExistantButton")));
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsCssSelectorIdThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.cssSelector("a[id='nonExistantButton']")));
  }

  @Test
  public void testShouldNotBeAbleToLocateMultipleElementsByXPathThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.xpath("//*[@id='notThere']")));
  }

  @Test
  public void testShouldNotBeAbleToLocateAMultipleElementsByTagNameThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.tagName("notThere")));
  }

  @Test
  public void testShouldNotBeAbleToLocateMultipleElementsByClassThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.className("notThere")));
  }

  @Test
  public void testShouldNotBeAbleToLocateMultipleElementsByNameThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.name("notThere")));
  }

  @Test
  public void testShouldNotBeAbleToLocateMultipleElementsByTextThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.linkText("notThere")));
  }
  
  @Test
  public void testShouldNotBeAbleToLocateMultipleElementsByPartialTextThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement rootElement = driver.findElement(By.id("multi"));
    assertListIsEmpty(rootElement.findElements(By.partialLinkText("notThere")));
  }

  @Test
  public void testShouldBeAbleToFindElementAndClickOnLinkIdentifiedByText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    WebElement clickMe = rootElement.findElement(By.linkText("click me"));
    clickMe.click();

    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }
  
  @Test
  public void testShouldBeAbleToFindElementAndClickOnLinkIdentifiedByPartialText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    WebElement clickMe = rootElement.findElement(By.partialLinkText("click m"));
    clickMe.click();

    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }


  @Test
  public void testShouldBeAbleToFindElementAndClickOnLinkIdentifiedById() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    rootElement.findElement(By.id("linkId")).click();
    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementAndClickOnLinkIdentifiedByCssSelector() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    rootElement.findElement(By.cssSelector("a[id='linkId']")).click();
    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementAndClickOnLinkIdentifiedByXPath() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    rootElement.findElement(By.xpath("//a[@id='linkId']")).click();
    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementAndGetTextOnLinkIdentifiedByTagName() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    WebElement element = rootElement.findElement(By.tagName("a"));
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void testShouldBeAbleToFindElementAndGetTextOnLinkIdentifiedByClass() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    WebElement element = rootElement.findElement(By.className("myTestClass"));
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void testShouldBeAbleToFindElementAndGetTextOnLinkIdentifiedByName() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    WebElement element = rootElement.findElement(By.name("nameTest"));
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void testShouldBeAbleToFindElementsAndClickOnLinkIdentifiedByText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    WebElement clickMe = rootElement.findElements(By.linkText("click me")).get(0);
    clickMe.click();

    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }
  
  @Test
  public void testShouldBeAbleToFindElementsAndClickOnLinkIdentifiedByPartialText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    WebElement clickMe = rootElement.findElements(By.partialLinkText("click m")).get(0);
    clickMe.click();

    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }


  @Test
  public void testShouldBeAbleToFindElementsAndClickOnLinkIdentifiedById() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    rootElement.findElements(By.id("linkId")).get(0).click();
    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementsAndClickOnLinkIdentifiedByCssSelector() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    rootElement.findElements(By.cssSelector("a[id='linkId']")).get(0).click();
    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementsAndClickOnLinkIdentifiedByXPath() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    rootElement.findElements(By.xpath("//a[@id='linkId']")).get(0).click();
    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementsAndGetTextOnLinkIdentifiedByTagName() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    WebElement element = rootElement.findElements(By.tagName("a")).get(0);
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void testShouldBeAbleToFindElementsAndGetTextOnLinkIdentifiedByClass() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement rootElement = driver.findElement(By.className("content"));
    WebElement element = rootElement.findElements(By.className("myTestClass")).get(0);
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void testShouldBeAbleToFindElementsAndGetTextOnLinkIdentifiedByName() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver.manage().timeouts().implicitlyWait(9, TimeUnit.SECONDS);
    WebElement rootElement = driver.findElement(By.className("content"));
    WebElement element = rootElement.findElements(By.name("nameTest")).get(0);
    Assert.assertEquals(element.getText(), "click me");
  }
}
