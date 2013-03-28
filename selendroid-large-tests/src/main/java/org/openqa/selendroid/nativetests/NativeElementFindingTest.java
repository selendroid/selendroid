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

import org.openqa.selendroid.tests.internal.BaseAndroidTest;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Element finding test for the native view part of selendroid.
 * 
 * @author ddary
 */
public class NativeElementFindingTest extends BaseAndroidTest {

  @Test
  public void testShouldBeAbleToFindButtonIdentifiedByText() throws Exception {
    openStartActivity();
    String buttonText = "EN Button";
    WebElement clickMe = driver.findElement(By.linkText(buttonText));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void testShouldBeAbleToFindButtonsIdentifiedByText() throws Exception {
    openStartActivity();
    String buttonText = "EN Button";
    List<WebElement> elements = driver.findElements(By.linkText(buttonText));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleElementByTextThatDoesNotExist() {
    openStartActivity();
    try {
      driver.findElement(By.linkText("nonExistentButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsByTextThatDoesNotExist() {
    openStartActivity();
    assertListIsEmpty(driver.findElements(By.linkText("nonExistentButton")));
  }

  @Test
  public void testShouldBeAbleToFindButtonIdentifiedById() throws Exception {
    openStartActivity();
    String buttonText = "Show Progress Bar for a while";
    WebElement clickMe = driver.findElement(By.id("waitingButtonTest"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void testShouldNotBeAbleTofindElementByXpath() throws Exception {
    openStartActivity();
    try {
      driver.findElement(By.xpath("//a[@id='waitingButtonTest']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (WebDriverException e) {
      Assert.assertTrue(e.getMessage().contains("By locator ByXPath is curently not supported!"));
    }
  }

  @Test
  public void testShouldNotBeAbleTofindElementByCssSelector() throws Exception {
    openStartActivity();
    try {
      driver.findElement(By.cssSelector("button[id='linkId']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (WebDriverException e) {
      Assert.assertTrue(e.getMessage().contains(
          "By locator ByCssSelector is curently not supported!"));
    }
  }

  @Test
  public void testShouldNotBeAbleTofindElementsByXpath() throws Exception {
    openStartActivity();
    try {
      driver.findElements(By.xpath("//a[@id='waitingButtonTest']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (WebDriverException e) {
      Assert.assertTrue(e.getMessage().contains("By locator ByXPath is curently not supported!"));
    }
  }

  @Test
  public void testShouldNotBeAbleTofindElementsByCssSelector() throws Exception {
    openStartActivity();
    try {
      driver.findElements(By.cssSelector("button[id='linkId']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (WebDriverException e) {
      Assert.assertTrue(e.getMessage().contains(
          "By locator ByCssSelector is curently not supported!"));
    }
  }

  @Test
  public void testShouldBeAbleToFindButtonsIdentifiedById() throws Exception {
    openStartActivity();
    String buttonText = "Show Progress Bar for a while";
    List<WebElement> elements = driver.findElements(By.id("waitingButtonTest"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleElementByIdThatDoesNotExist() {
    openStartActivity();
    try {
      driver.findElement(By.id("nonExistentButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsByIdThatDoesNotExist() {
    openStartActivity();
    assertListIsEmpty(driver.findElements(By.id("nonExistentButton")));

  }

  @Test
  public void testShouldBeAbleToFindButtonIdentifiedByClass() throws Exception {
    openStartActivity();
    String buttonText = "EN Button";
    WebElement clickMe = driver.findElement(By.className("android.widget.Button"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void testShouldBeAbleToFindButtonsIdentifiedByClass() throws Exception {
    openStartActivity();
    String buttonText = "Show Progress Bar for a while";
    List<WebElement> elements = driver.findElements(By.className("android.widget.Button"));
    Assert.assertEquals(elements.size(), 3);
    Assert.assertEquals(elements.get(1).getText(), buttonText);
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleElementByClassThatDoesNotExist() {
    openStartActivity();
    try {
      driver.findElement(By.className("de.dary.MyView"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsByClassThatDoesNotExist() {
    openStartActivity();
    assertListIsEmpty(driver.findElements(By.className("de.dary.MyView")));
  }

  private void assertListIsEmpty(List<WebElement> elements) {
    Assert.assertTrue(elements.isEmpty(), "Expecting empty list when no elements are found.");
  }

  @Test
  public void testShouldBeAbleToFindButtonIdentifiedByL10nKey() throws Exception {
    openStartActivity();
    String buttonText = "EN Button";
    WebElement clickMe = driver.findElement(By.tagName("button"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void testShouldBeAbleToFindButtonsIdentifiedByL10nKey() throws Exception {
    openStartActivity();
    String buttonText = "EN Button";
    List<WebElement> elements = driver.findElements(By.tagName("button"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleElementByL10nKeyThatDoesNotExist() {
    openStartActivity();
    try {
      driver.findElement(By.tagName("de.dary.MyView.l10nView"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsByL10nKeyThatDoesNotExist() {
    openStartActivity();
    assertListIsEmpty(driver.findElements(By.tagName("de.dary.MyView.l10nView")));

  }

  @Test
  public void testShouldBeAbleToFindButtonIdentifiedByContentDescription() throws Exception {
    openStartActivity();
    String buttonText = "Show Progress Bar for a while";
    WebElement clickMe = driver.findElement(By.name("waitingButtonTestCD"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void testShouldBeAbleToFindButtonsIdentifiedContentDescription() throws Exception {
    openStartActivity();
    String buttonText = "Show Progress Bar for a while";
    List<WebElement> elements = driver.findElements(By.name("waitingButtonTestCD"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleElementByContentDescriptionThatDoesNotExist() {
    openStartActivity();
    try {
      driver.findElement(By.name("cdDoesNotExist"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsByContentDescriptionThatDoesNotExist() {
    openStartActivity();

    assertListIsEmpty(driver.findElements(By.name("cdDoesNotExist")));

  }

}
