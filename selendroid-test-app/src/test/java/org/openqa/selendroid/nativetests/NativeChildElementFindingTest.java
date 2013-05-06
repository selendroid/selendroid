/*
 * Copyright 2013 selendroid committers.
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
package org.openqa.selendroid.nativetests;

import java.util.List;

import org.openqa.selendroid.TestGroups;
import org.openqa.selendroid.support.BaseAndroidTest;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Child element finding test for the native view part of selendroid.
 * 
 * @author ddary
 */
@Test(groups={TestGroups.NATIVE})
public class NativeChildElementFindingTest extends BaseAndroidTest {
  public static final String ACTIVITY_CLASS = "org.openqa.selendroid.testapp."
      + "HomeScreenActivity";

  private void assertListIsEmpty(List<WebElement> elements) {
    Assert.assertTrue(elements.isEmpty(), "Expecting empty list when no elements are found.");
  }
  
  
  @Test
  public void testShouldBeAbleToFindChildButtonIdentifiedByText() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    String buttonText = "EN Button";
    WebElement clickMe = rootElement.findElement(By.linkText(buttonText));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void testShouldBeAbleToFindChildButtonsIdentifiedByPartialText() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    String buttonText = "EN Butto";
    List<WebElement> elements = rootElement.findElements(By.partialLinkText(buttonText));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertTrue(elements.get(0).getText().contains(buttonText));
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleChildElementByPartialTextThatDoesNotExist() {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    try {
      rootElement.findElement(By.partialLinkText("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleChildElementsByPartialTextThatDoesNotExist() {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.linkText("nonExistantButton")));
  }
  
  @Test
  public void testShouldBeAbleToFindChildButtonIdentifiedByPartialText() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    String buttonText = "EN Butto";
    WebElement clickMe = rootElement.findElement(By.partialLinkText(buttonText));
    Assert.assertTrue(clickMe.getText().contains(buttonText));
  }

  @Test
  public void testShouldBeAbleToFindChildButtonsIdentifiedByText() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    String buttonText = "EN Button";
    List<WebElement> elements = rootElement.findElements(By.linkText(buttonText));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleChildElementByTextThatDoesNotExist() {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    try {
      rootElement.findElement(By.linkText("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleChildElementsByTextThatDoesNotExist() {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.linkText("nonExistantButton")));
  }

  @Test
  public void testShouldBeAbleToFindChildButtonIdentifiedById() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    String buttonText = "EN Button";
    WebElement clickMe = rootElement.findElement(By.id("buttonTest"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void testShouldBeAbleToFindChildButtonsIdentifiedById() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    String buttonText = "EN Button";
    List<WebElement> elements = rootElement.findElements(By.id("buttonTest"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleChildElementByIdThatDoesNotExist() {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));

    try {
      rootElement.findElement(By.id("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleChildElementsByIdThatDoesNotExist() {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.id("nonExistantButton")));

  }

  @Test
  public void testShouldBeAbleToFindChildButtonIdentifiedByClass() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    String buttonText = "EN Button";
    WebElement clickMe = rootElement.findElement(By.className("android.widget.Button"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void testShouldBeAbleToFindChildButtonsIdentifiedByClass() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    String buttonText = "EN Button";
    List<WebElement> elements = rootElement.findElements(By.className("android.widget.Button"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleChildElementByClassThatDoesNotExist() {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    try {
      rootElement.findElement(By.className("de.dary.MyView"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleChildElementsByClassThatDoesNotExist() {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.className("de.dary.MyView")));
  }

  @Test
  public void testShouldBeAbleToFindChildButtonIdentifiedByTagName() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    String buttonText = "EN Button";
    WebElement clickMe = rootElement.findElement(By.tagName("Button"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void testShouldBeAbleToFindChildButtonsIdentifiedByTagName() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    String buttonText = "EN Button";
    List<WebElement> elements = rootElement.findElements(By.tagName("Button"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleChildElementByTagNameThatDoesNotExist() {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    try {
      rootElement.findElement(By.tagName("de.dary.MyView.l10nView"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleChildElementsByTagNameThatDoesNotExist() {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.tagName("de.dary.MyView.l10nView")));

  }

  @Test
  public void testShouldBeAbleToFindChildButtonIdentifiedByContentDescription() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    String buttonText = "EN Button";
    WebElement clickMe = rootElement.findElement(By.name("buttonTestCD"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void testShouldBeAbleToFindChildButtonsIdentifiedContentDescription() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    String buttonText = "EN Button";
    List<WebElement> elements = rootElement.findElements(By.name("buttonTestCD"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleChildElementByContentDescriptionThatDoesNotExist() {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    try {
      rootElement.findElement(By.name("cdDoesNotExist"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleChildElementsByContentDescriptionThatDoesNotExist() {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.name("cdDoesNotExist")));

  }

  @Test
  public void testShouldNotBeAbleTofindChildElementByXpath() throws Exception {
    openStartActivity();
    try {
      driver.findElement(By.xpath("//a[@id='waitingButtonTest']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (WebDriverException e) {
      Assert.assertTrue(e.getMessage().contains("By locator ByXPath is curently not supported!"));
    }
  }

  @Test
  public void testShouldNotBeAbleTofindChildElementByCssSelector() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    try {
      rootElement.findElement(By.cssSelector("button[id='waitingButtonTest']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (WebDriverException e) {
      Assert.assertTrue(e.getMessage().contains(
          "By locator ByCssSelector is curently not supported!"));
    }
  }

  @Test
  public void testShouldNotBeAbleTofindChildElementsByXpath() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    try {
      rootElement.findElements(By.xpath("//button[id='waitingButtonTest']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (WebDriverException e) {
      Assert.assertTrue(e.getMessage().contains("By locator ByXPath is curently not supported!"));
    }
  }

  @Test
  public void testShouldNotBeAbleTofindChildElementsByCssSelector() throws Exception {
    openStartActivity();
    WebElement rootElement = driver.findElement(By.id("l10n"));
    try {
      rootElement.findElements(By.cssSelector("button[id='waitingButtonTest']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (WebDriverException e) {
      Assert.assertTrue(e.getMessage().contains(
          "By locator ByCssSelector is curently not supported!"));
    }
  }

}
