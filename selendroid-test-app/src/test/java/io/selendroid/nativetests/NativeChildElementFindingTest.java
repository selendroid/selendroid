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
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

/**
 * Child element finding test for the native view part of selendroid.
 * 
 * @author ddary
 */
public class NativeChildElementFindingTest extends BaseAndroidTest {
  public static final String ACTIVITY_CLASS = "io.testapp." + "HomeScreenActivity";

  private void assertListIsEmpty(List<WebElement> elements) {
    Assert.assertTrue("Expecting empty list when no elements are found.",elements.isEmpty());
  }


  @Test
  public void shouldFindChildButtonByText() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    String buttonText = "EN Button";
    WebElement clickMe = rootElement.findElement(By.linkText(buttonText));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void shouldFindChildButtonsByPartialText() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    String buttonText = "EN Butto";
    List<WebElement> elements = rootElement.findElements(By.partialLinkText(buttonText));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertTrue(elements.get(0).getText().contains(buttonText));
  }

  @Test()
  public void shouldNotFindChildElementByPartialText() {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    try {
      rootElement.findElement(By.partialLinkText("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindChildElementsByPartialText() {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.linkText("nonExistantButton")));
  }

  @Test
  public void shouldFindChildButtonByPartialText() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    String buttonText = "EN Butto";
    WebElement clickMe = rootElement.findElement(By.partialLinkText(buttonText));
    Assert.assertTrue(clickMe.getText().contains(buttonText));
  }

  @Test
  public void shouldFindChildButtonsIdentifiedByText() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    String buttonText = "EN Button";
    List<WebElement> elements = rootElement.findElements(By.linkText(buttonText));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void shouldNotFindChildElementByText() {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    try {
      rootElement.findElement(By.linkText("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindChildElementsByText() {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.linkText("nonExistantButton")));
  }

  @Test
  public void shouldFindChildButtonById() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    String buttonText = "EN Button";
    WebElement clickMe = rootElement.findElement(By.id("buttonTest"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void shouldFindChildButtonsById() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    String buttonText = "EN Button";
    List<WebElement> elements = rootElement.findElements(By.id("buttonTest"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void shouldNotFindChildElementById() {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));

    try {
      rootElement.findElement(By.id("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindChildElementsById() {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.id("nonExistantButton")));

  }

  @Test
  public void shouldFindChildButtonByClass() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    String buttonText = "EN Button";
    WebElement clickMe = rootElement.findElement(By.className("android.widget.Button"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void shouldFindChildButtonsByClass() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    String buttonText = "EN Button";
    List<WebElement> elements = rootElement.findElements(By.className("android.widget.Button"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void shouldNotFindChildElementByClass() {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    try {
      rootElement.findElement(By.className("de.dary.MyView"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindChildElementsByClass() {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.className("de.dary.MyView")));
  }

  @Test
  public void shouldFindChildButtonByTagName() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    String buttonText = "EN Button";
    WebElement clickMe = rootElement.findElement(By.tagName("Button"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void shouldFindChildButtonsByTagName() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    String buttonText = "EN Button";
    List<WebElement> elements = rootElement.findElements(By.tagName("Button"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void shouldNotFindChildElementByTagName() {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    try {
      rootElement.findElement(By.tagName("de.dary.MyView.l10nView"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindChildElementsByTagName() {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.tagName("de.dary.MyView.l10nView")));

  }

  @Test
  public void shouldFindChildButtonByName() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    String buttonText = "EN Button";
    WebElement clickMe = rootElement.findElement(By.name("buttonTestCD"));
    Assert.assertEquals(clickMe.getText(), buttonText);
  }

  @Test
  public void shouldFindChildButtonsByName() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    String buttonText = "EN Button";
    List<WebElement> elements = rootElement.findElements(By.name("buttonTestCD"));
    Assert.assertEquals(elements.size(), 1);
    Assert.assertEquals(elements.get(0).getText(), buttonText);
  }

  @Test()
  public void shouldNotFindChildElementByName() {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    try {
      rootElement.findElement(By.name("cdDoesNotExist"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void shouldNotFindChildElementsByName() {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.name("cdDoesNotExist")));

  }

  @Test
  public void shouldNotFindChildElementByXpath() throws Exception {
    openStartActivity();
    try {
      driver().findElement(By.xpath("//a[@id='blabla']"));
      Assert.fail("not existent element cannot be found.");
    } catch (NoSuchElementException e) {
      // expected
    }
  }

  @Test
  public void shouldNotFindChildElementByCss() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    try {
      rootElement.findElement(By.cssSelector("button[id='waitingButtonTest']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (WebDriverException e) {
      Assert.assertTrue(e.getMessage().contains(
          "By locator ByCssSelector is curently not supported!"));
    }
  }

  @Test
  public void shouldNotFindChildElementsByXpath() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    assertListIsEmpty(rootElement.findElements(By.xpath("//button[id='blabla']")));
  }

  @Test
  public void shouldNotFindChildElementsByCss() throws Exception {
    openStartActivity();
    WebElement rootElement = driver().findElement(By.id("l10n"));
    try {
      rootElement.findElements(By.cssSelector("button[id='waitingButtonTest']"));
      Assert.fail("Finding Native elements by css selector is not supported.");
    } catch (WebDriverException e) {
      Assert.assertTrue(e.getMessage().contains(
          "By locator ByCssSelector is curently not supported!"));
    }
  }

}
