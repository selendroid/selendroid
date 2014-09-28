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
package io.selendroid.nativetests;

import io.selendroid.support.BaseAndroidTest;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * Element finding test for the native view part of selendroid.
 * 
 * @author ddary
 */
public class NativeElementFindingTest extends BaseAndroidTest {
  @Test
  public void shouldFindButtonByPartialText() throws Exception {
    openStartActivity();
    String buttonText = "EN Butto";
    WebElement clickMe = driver().findElement(By.partialLinkText(buttonText));
    Assert.assertTrue(clickMe.getText().contains(buttonText));
  }

  @Test
  public void shouldFindButtonsByPartialText() throws Exception {
    openStartActivity();
    String buttonText = "EN Butto";
    List<WebElement> elements = driver().findElements(By.partialLinkText(buttonText));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertTrue(elements.get(0).getText().contains(buttonText));
  }

  @Test()
  public void shouldNotFindElementByPartialText() {
    openStartActivity();
    try {
      driver().findElement(By.partialLinkText("nonExistentButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindElementsByPartialText() {
    openStartActivity();
    assertListIsEmpty(driver().findElements(By.partialLinkText("nonExistentButton")));
  }

  @Test
  public void shouldFindButtonByText() throws Exception {
    openStartActivity();
    String buttonText = "EN Button";
    WebElement clickMe = driver().findElement(By.linkText(buttonText));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void shouldFindButtonsByText() throws Exception {
    openStartActivity();
    String buttonText = "EN Button";
    List<WebElement> elements = driver().findElements(By.linkText(buttonText));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void shouldNotFindElementByText() {
    openStartActivity();
    try {
      driver().findElement(By.linkText("nonExistentButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindElementsByText() {
    openStartActivity();
    assertListIsEmpty(driver().findElements(By.linkText("nonExistentButton")));
  }

  @Test
  public void shouldButtonById() throws Exception {
    openStartActivity();
    String buttonText = "Show Progress Bar for a while";
    WebElement clickMe = driver().findElement(By.id("waitingButtonTest"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void shouldNotFindElementByXpath() throws Exception {
    openStartActivity();
    try {
      driver().findElement(By.xpath("//a[@id='waitingButtonTest']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (NoSuchElementException e) {
      // expected
    }
  }

  @Test
  public void shouldNotFindElementByCssSelector() throws Exception {
    openStartActivity();
    try {
      driver().findElement(By.cssSelector("button[id='linkId']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (WebDriverException e) {
      Assert.assertTrue(e.getMessage().contains(
          "By locator ByCssSelector is curently not supported!"));
    }
  }

  @Test
  public void shouldNotFindElementsByXpath() throws Exception {
    openStartActivity();
    assertListIsEmpty(driver().findElements(By.xpath("//a[@id='blaBal']")));
  }

  @Test
  public void shouldNotFindElementsByCss() throws Exception {
    openStartActivity();
    try {
      driver().findElements(By.cssSelector("button[id='linkId']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (WebDriverException e) {
      Assert.assertTrue(e.getMessage().contains(
          "By locator ByCssSelector is curently not supported!"));
    }
  }

  @Test
  public void shouldFindButtonsById() throws Exception {
    openStartActivity();
    String buttonText = "Show Progress Bar for a while";
    List<WebElement> elements = driver().findElements(By.id("waitingButtonTest"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void shouldNotFindElementById() {
    openStartActivity();
    try {
      driver().findElement(By.id("nonExistentButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindElementsById() {
    openStartActivity();
    assertListIsEmpty(driver().findElements(By.id("nonExistentButton")));

  }

  @Test
  public void shouldFindButtonByClass() throws Exception {
    openStartActivity();
    String buttonText = "Display and focus on layout";
    WebElement clickMe = driver().findElement(By.className("android.widget.Button"));
    Assert.assertEquals(buttonText, clickMe.getText());
  }

  @Test
  public void shouldFindButtonsByClass() throws Exception {
    openStartActivity();
    String buttonText = "Show Progress Bar for a while";
    List<WebElement> elements = driver().findElements(By.className("android.widget.Button"));
    Assert.assertEquals(8, elements.size());
    Assert.assertEquals(buttonText, elements.get(2).getText());
  }

  @Test()
  public void shouldNotFindElementByClass() {
    openStartActivity();
    try {
      driver().findElement(By.className("de.dary.MyView"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindElementsByClass() {
    openStartActivity();
    assertListIsEmpty(driver().findElements(By.className("de.dary.MyView")));
  }

  private void assertListIsEmpty(List<WebElement> elements) {
    Assert.assertTrue("Expecting empty list when no elements are found.", elements.isEmpty());
  }

  @Test
  public void shouldFindButtonByTagName() throws Exception {
    openStartActivity();
    String buttonText = "Display and focus on layout";
    WebElement clickMe = driver().findElement(By.tagName("Button"));

    Assert.assertEquals(buttonText, clickMe.getText());
  }

  @Test
  public void shouldFindButtonsByTagName() throws Exception {
    openStartActivity();
    String buttonText = "EN Button";
    List<WebElement> elements = driver().findElements(By.tagName("Button"));
    Assert.assertEquals(7, elements.size());
    Assert.assertEquals(buttonText, elements.get(1).getText());
  }

  @Test()
  public void shouldNotFindElementByTagName() {
    openStartActivity();
    try {
      driver().findElement(By.tagName("de.dary.MyView.l10nView"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindElementsByTagName() {
    openStartActivity();
    assertListIsEmpty(driver().findElements(By.tagName("de.dary.MyView.l10nView")));
  }

  @Test
  public void shouldFindButtonByName() throws Exception {
    openStartActivity();
    String buttonText = "Show Progress Bar for a while";
    WebElement clickMe = driver().findElement(By.name("waitingButtonTestCD"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void shouldFindButtonsByName() throws Exception {
    openStartActivity();
    String buttonText = "Show Progress Bar for a while";
    List<WebElement> elements = driver().findElements(By.name("waitingButtonTestCD"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void shouldNotFindElementByName() {
    openStartActivity();
    try {
      driver().findElement(By.name("cdDoesNotExist"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindElementsByName() {
    openStartActivity();

    assertListIsEmpty(driver().findElements(By.name("cdDoesNotExist")));

  }


  @Test()
  public void shouldNotFindElementByIdFromPreviousActivity() {
    openStartActivity();
    driver().findElement(By.id("startUserRegistration")).click();
    new WebDriverWait(driver(), 5).until(ExpectedConditions.presenceOfElementLocated(By
        .id("inputUsername")));

    try {
      driver().findElement(By.id("buttonStartWebview"));
      Assert.fail("The element from previous screen should not be found.");
    } catch (NoSuchElementException e) {
      // // this element is located on previous activity and should not be found
    }
  }

  @Test()
  public void shouldThrowStaleElementReferenceError() {
    openStartActivity();
    WebElement textfield = driver().findElement(By.id("my_text_field"));
    Assert.assertNotNull("textfield should be found.", textfield);
    driver().findElement(By.id("startUserRegistration")).click();
    new WebDriverWait(driver(), 5).until(ExpectedConditions.presenceOfElementLocated(By
        .id("inputUsername")));

    try {
      textfield.sendKeys("Hello");
      Assert.fail("The element from previous screen should not be found.");
    } catch (StaleElementReferenceException e) {
      // // this element is located on previous activity and should not be found
    }
  }

  @Test()
  public void shouldFindInvisibleElemenById() throws Exception {
    openStartActivity();
    WebElement textview = driver().findElement(By.id("visibleTextView"));
    boolean isTextViewDisplayed = textview.isDisplayed();
    driver().findElement(By.id("visibleButtonTest")).click();
    Assert.assertEquals(textview.isDisplayed(), !isTextViewDisplayed);
    Assert.assertEquals(textview.getAttribute("shown"), String.valueOf(!isTextViewDisplayed));
  }

  @Test()
  public void shouldFindInvisibleElemenByText() throws Exception {
    openStartActivity();
    WebElement textview = driver().findElement(By.linkText("Text is sometimes displayed"));
    Assert.assertEquals(textview.isDisplayed(), false);
    driver().findElement(By.id("visibleButtonTest")).click();
    Thread.sleep(1000);
    Assert.assertEquals(textview.isDisplayed(), true);
  }
  
  @Test()
  public void shouldNotFindDuplicateElements() throws Exception {
    openStartActivity();
    driver().findElement(By.id("topLevelElementTest")).click();
    List<WebElement> elements = driver().findElements(By.id("focusedText"));
    Assert.assertEquals(elements.size(), 1);
  }
}
