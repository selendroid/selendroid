/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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
package io.selendroid.nativetests;

import io.selendroid.TestGroups;
import io.selendroid.support.BaseAndroidTest;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Element finding test for the native view part of selendroid.
 * 
 * @author ddary
 */
@Test(groups = {TestGroups.NATIVE})
public class NativeElementFindingTest extends BaseAndroidTest {
  @Test
  public void testShouldBeAbleToFindButtonIdentifiedByPartialText() throws Exception {
    openStartActivity();
    String buttonText = "EN Butto";
    WebElement clickMe = driver.findElement(By.partialLinkText(buttonText));
    Assert.assertTrue(clickMe.getText().contains(buttonText));
  }

  @Test
  public void testShouldBeAbleToFindButtonsIdentifiedByPartialText() throws Exception {
    openStartActivity();
    String buttonText = "EN Butto";
    List<WebElement> elements = driver.findElements(By.partialLinkText(buttonText));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertTrue(elements.get(0).getText().contains(buttonText));
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleElementByPartialTextThatDoesNotExist() {
    openStartActivity();
    try {
      driver.findElement(By.partialLinkText("nonExistentButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsByPartialTextThatDoesNotExist() {
    openStartActivity();
    assertListIsEmpty(driver.findElements(By.partialLinkText("nonExistentButton")));
  }

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
    } catch (NoSuchElementException e) {
      // expected
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
    assertListIsEmpty(driver.findElements(By.xpath("//a[@id='blaBal']")));
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
    Assert.assertEquals(elements.size(), 5);
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
  public void testShouldBeAbleToFindButtonIdentifiedByTagName() throws Exception {
    openStartActivity();
    String buttonText = "EN Button";
    WebElement clickMe = driver.findElement(By.tagName("Button"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void testShouldBeAbleToFindButtonsIdentifiedByTagName() throws Exception {
    openStartActivity();
    String buttonText = "EN Button";
    List<WebElement> elements = driver.findElements(By.tagName("Button"));
    Assert.assertEquals(elements.size(), 4);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void testShouldNotBeAbleToLocateASingleElementByTagNameThatDoesNotExist() {
    openStartActivity();
    try {
      driver.findElement(By.tagName("de.dary.MyView.l10nView"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsTagNameThatDoesNotExist() {
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


  @Test
  public void testShouldNotBeAbleToFindElementByIdFromPreviousActivity() {
    openStartActivity();
    driver.findElement(By.id("startUserRegistration")).click();
    new WebDriverWait(driver, 5).until(ExpectedConditions.presenceOfElementLocated(By
        .id("inputUsername")));

    try {
      driver.findElement(By.id("buttonStartWebview"));
      Assert.fail("The element from previous screen should not be found.");
    } catch (NoSuchElementException e) {
      // // this element is located on previous activity and should not be found
    }
  }

  @Test
  public void testShouldNotBeAbleToInteractWithElementFromPreviousActivity() {
    openStartActivity();
    WebElement textfield = driver.findElement(By.id("my_text_field"));
    Assert.assertNotNull(textfield, "textfield should be found.");
    driver.findElement(By.id("startUserRegistration")).click();
    new WebDriverWait(driver, 5).until(ExpectedConditions.presenceOfElementLocated(By
        .id("inputUsername")));

    try {
      textfield.sendKeys("Hello");
      Assert.fail("The element from previous screen should not be found.");
    } catch (StaleElementReferenceException e) {
      // // this element is located on previous activity and should not be found
    }
  }

  @Test
  public void testShouldBeAbleToFindElementAndEnterTextWhereScrollingMustBeDone() {
    openStartActivity();

    driver.findElement(By.id("startUserRegistration")).click();
    new WebDriverWait(driver, 5).until(ExpectedConditions.presenceOfElementLocated(By
        .id("inputUsername")));
    WebElement acceptAddsCheckbox = driver.findElement(By.id("input_adds"));
    Assert.assertEquals(acceptAddsCheckbox.isSelected(), false);
    acceptAddsCheckbox.click();
    Assert.assertEquals(acceptAddsCheckbox.isSelected(), true);
  }

  @Test
  public void testShouldBeAbleToFindInvisibleElemenById() throws Exception {
    openStartActivity();
    WebElement textview = driver.findElement(By.id("visibleTextView"));
    Assert.assertEquals(textview.isDisplayed(), false);
    driver.findElement(By.id("visibleButtonTest")).click();
    Thread.sleep(1000);
    Assert.assertEquals(textview.getAttribute("shown"), "true");
    Assert.assertEquals(textview.isDisplayed(), true);
  }

  @Test
  public void testShouldBeAbleToFindInvisibleElemenByText() throws Exception {
    openStartActivity();
    WebElement textview = driver.findElement(By.linkText("Text is sometimes displayed"));
    Assert.assertEquals(textview.isDisplayed(), false);
    driver.findElement(By.id("visibleButtonTest")).click();
    Thread.sleep(1000);
    Assert.assertEquals(textview.isDisplayed(), true);
  }
}
