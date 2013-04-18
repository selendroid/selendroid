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
import org.openqa.selendroid.waiter.TestWaiter;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Element finding test for the web view part of selendroid.
 * 
 * @author ddary
 */
@Test(groups={TestGroups.WEBVIEW})
public class WebElementFindingTest extends BaseAndroidTest {
  @Test()
  public void testShouldNotBeAbleToLocateASingleElementByIdThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver.findElement(By.id("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleElementByCssSelctorThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver.findElement(By.cssSelector("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldNotBeAbleToLocateASingleElementByXPathThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver.findElement(By.xpath("//*[@id='notThere']"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldNotBeAbleToLocateASingleElementByTagNameThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver.findElement(By.tagName("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldNotBeAbleToLocateASingleElementByClassThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver.findElement(By.className("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldNotBeAbleToLocateASingleElementByNameThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver.findElement(By.name("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldNotBeAbleToLocateASingleElementByTextThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver.findElement(By.linkText("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }
  
  @Test
  public void testShouldNotBeAbleToLocateASingleElementByPartialTextThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    try {
      driver.findElement(By.partialLinkText("notThere"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsByIdThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver.findElements(By.id("nonExistantButton")));
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsByCssSelectorThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver.findElements(By.cssSelector("nonExistantButton")));
  }

  @Test
  public void testShouldNotBeAbleToLocateMultipleElementsByXPathThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver.findElements(By.xpath("//*[@id='notThere']")));
  }

  @Test
  public void testShouldNotBeAbleToLocateAMultipleElementsByTagNameThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver.findElements(By.tagName("notThere")));
  }

  @Test
  public void testShouldNotBeAbleToLocateMultipleElementsByClassThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver.findElements(By.className("notThere")));
  }

  private void assertListIsEmpty(List<WebElement> elements) {
    Assert.assertTrue(elements.isEmpty(), "Expecting empty list when no elements are found.");
  }

  @Test
  public void testShouldNotBeAbleToLocateMultipleElementsByNameThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver.findElements(By.name("notThere")));
  }

  @Test
  public void testShouldNotBeAbleToLocateMultipleElementsByTextThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver.findElements(By.linkText("notThere")));
  }
  
  @Test
  public void testShouldNotBeAbleToLocateMultipleElementsByPartialTextThatDoesNotExist() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);

    assertListIsEmpty(driver.findElements(By.partialLinkText("notThere")));
  }

  @Test
  public void testShouldBeAbleToFindElementAndClickOnLinkIdentifiedByText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement clickMe = driver.findElement(By.linkText("click me"));
    clickMe.click();

    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementAndClickOnLinkIdentifiedByPartialText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    TestWaiter.waitForElement(By.partialLinkText("click"),10,driver).click();

    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementAndClickOnLinkIdentifiedById() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver.findElement(By.id("linkId")).click();
    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementAndClickOnLinkIdentifiedByCssSelector() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver.findElement(By.cssSelector("a[id='linkId']")).click();
    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementAndClickOnLinkIdentifiedByXPath() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver.findElement(By.xpath("//a[@id='linkId']")).click();
    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementAndGetTextOnLinkIdentifiedByTagName() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement element = driver.findElement(By.tagName("a"));
    Assert.assertEquals(element.getText(), "Open new window");
  }

  @Test
  public void testShouldBeAbleToFindElementAndGetTextOnLinkIdentifiedByClass() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement element = driver.findElement(By.className("myTestClass"));
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void testShouldBeAbleToFindElementAndGetTextOnLinkIdentifiedByName() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    
    WebElement element = driver.findElement(By.name("nameTest"));
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void testShouldBeAbleToFindElementsAndClickOnLinkIdentifiedByText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement clickMe = driver.findElements(By.linkText("click me")).get(0);
    clickMe.click();

    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }
  
  @Test
  public void testShouldBeAbleToFindElementsAndClickOnLinkIdentifiedByPartialText() throws Exception {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement clickMe = driver.findElements(By.partialLinkText("click m")).get(0);
    clickMe.click();

    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }


  @Test
  public void testShouldBeAbleToFindElementsAndClickOnLinkIdentifiedById() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver.findElements(By.id("linkId")).get(0).click();
    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementsAndClickOnLinkIdentifiedByCssSelector() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver.findElements(By.cssSelector("a[id='linkId']")).get(0).click();
    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementsAndClickOnLinkIdentifiedByXPath() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    driver.findElements(By.xpath("//a[@id='linkId']")).get(0).click();
    waitFor(pageTitleToBe(driver, "We Arrive Here"), 15, TimeUnit.SECONDS);
    Assert.assertEquals(driver.getTitle(), "We Arrive Here");
  }

  @Test
  public void testShouldBeAbleToFindElementsAndGetTextOnLinkIdentifiedByTagName() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement element = driver.findElements(By.tagName("a")).get(0);
    Assert.assertEquals(element.getText(), "Open new window");
  }

  @Test
  public void testShouldBeAbleToFindElementsAndGetTextOnLinkIdentifiedByClass() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement element = driver.findElements(By.className("myTestClass")).get(0);
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void testShouldBeAbleToFindElementsAndGetTextOnLinkIdentifiedByName() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement element = driver.findElements(By.name("nameTest")).get(0);
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test
  public void testShouldfindAnElementBasedOnId() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    waitFor(pageTitleToBe(driver, "We Leave From Here"), 10, TimeUnit.SECONDS);

    WebElement element = driver.findElement(By.id("checky"));
    Assert.assertEquals(element.isSelected(), false);
  }
}
