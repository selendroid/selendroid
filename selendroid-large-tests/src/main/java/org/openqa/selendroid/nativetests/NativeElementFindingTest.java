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

import static org.openqa.selendroid.webviewdrivertests.waiter.TestWaiter.waitFor;

import java.util.List;

import org.openqa.selendroid.tests.internal.BaseAndroidTest;
import org.openqa.selendroid.webviewdrivertests.waiter.WaitingConditions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Element finding test for the native view part of selendroid.
 * 
 * @author ddary
 */
public class NativeElementFindingTest extends BaseAndroidTest {
  public static final String ACTIVITY_CLASS = "org.openqa.selendroid.testapp."
      + "HomeScreenActivity";

  private void openStartActivity() {
    driver.switchTo().window(NATIVE_APP);
    driver.get("and-activity://" + ACTIVITY_CLASS);
    waitFor(WaitingConditions.driverUrlToBe(driver, "and-activity://HomeScreenActivity"));
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
      driver.findElement(By.linkText("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsByTextThatDoesNotExist() {
    openStartActivity();
    try {
      driver.findElements(By.linkText("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test
  public void testShouldBeAbleToFindButtonIdentifiedById() throws Exception {
    openStartActivity();
    String buttonText = "Show Progress Bar for a while";
    WebElement clickMe = driver.findElement(By.id("waitingButtonTest"));
    Assert.assertEquals(clickMe.getText(), buttonText);
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
      driver.findElement(By.id("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

  @Test()
  public void testShouldNotBeAbleToLocateMultipleElementsByIdThatDoesNotExist() {
    openStartActivity();
    try {
      driver.findElements(By.id("nonExistantButton"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
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
    Assert.assertEquals(elements.size(), 2);
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
    try {
      driver.findElements(By.className("de.dary.MyView"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
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
    try {
      driver.findElements(By.tagName("de.dary.MyView.l10nView"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
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
    try {
      driver.findElements(By.name("cdDoesNotExist"));
      Assert.fail("Should not have succeeded");
    } catch (NoSuchElementException e) {
      // this is expected
    }
  }

}
