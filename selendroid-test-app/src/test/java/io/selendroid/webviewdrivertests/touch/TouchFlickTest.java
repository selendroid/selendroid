/*
 * Copyright 2012 Software Freedom Conservancy Copyright 2007-2012 Selenium committers
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

package io.selendroid.webviewdrivertests.touch;

import static org.junit.Assert.assertTrue;
import io.selendroid.webviewdrivertests.HtmlTestData;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.touch.FlickAction;
import org.openqa.selenium.interactions.touch.TouchActions;

/**
 * Tests the basic flick operations on touch enabled devices.
 */
public class TouchFlickTest extends TouchTestBase {

  private TouchActions getBuilder(WebDriver driver) {
    return new TouchActions(driver);
  }

  @Test
  public void testCanFlickVerticallyFromWebElement() {
    openWebdriverTestPage(HtmlTestData.LONG_CONTENT_PAGE);

    WebElement link = driver().findElement(By.id("link3"));
    int y = link.getLocation().y;
    // The element is located at the bottom of the page,
    // so it is not initially visible on the screen.
    assertTrue(y > 4200);

    WebElement toFlick = driver().findElement(By.id("imagestart"));
    Action flick = getBuilder(driver()).flick(toFlick, 0, -600, FlickAction.SPEED_NORMAL).build();
    flick.perform();
    y = link.getLocation().y;
    // After flicking, the element should now be visible on the screen.
    assertTrue("Expected y < 4000, but got: " + y, y < 4000);
  }


  @Test
  public void testCanFlickVerticallyFastFromWebElement() {
    openWebdriverTestPage(HtmlTestData.LONG_CONTENT_PAGE);

    WebElement link = driver().findElement(By.id("link4"));
    int y = link.getLocation().y;
    // The element is located at the bottom of the page,
    // so it is not initially visible on the screen.
    assertTrue(y > 8700);

    WebElement toFlick = driver().findElement(By.id("imagestart"));
    Action flick = getBuilder(driver()).flick(toFlick, 0, -600, FlickAction.SPEED_FAST).build();
    flick.perform();
    y = link.getLocation().y;
    // After flicking, the element should now be visible on the screen.
    assertTrue("Expected y < 8700, but got: " + y, y < 8700);
  }


  @Test
  public void testCanFlickVertically() {
    openWebdriverTestPage(HtmlTestData.LONG_CONTENT_PAGE);

    WebElement link = driver().findElement(By.id("link3"));
    int y = link.getLocation().y;
    // The element is located at the bottom of the page,
    // so it is not initially visible on the screen.
    assertTrue(y > 4200);

    Action flick = getBuilder(driver()).flick(0, 750).build();
    flick.perform();
    y = link.getLocation().y;

    // After flicking, the element should now be visible on the screen.
    assertTrue("Got: " + y, y < 4200);
  }


  @Test
  public void testCanFlickVerticallyFast() {
    openWebdriverTestPage(HtmlTestData.LONG_CONTENT_PAGE);

    WebElement link = driver().findElement(By.id("link4"));
    int y = link.getLocation().y;
    // The element is located at the bottom of the page,
    // so it is not initially visible on the screen.
    assertTrue(y > 8700);

    Action flick = getBuilder(driver()).flick(0, 1500).build();
    flick.perform();
    y = link.getLocation().y;
    // After flicking, the element should now be visible on the screen.
    assertTrue("Got: " + y, y < 4000);
  }

}
