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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.touch.TouchActions;

import io.selendroid.support.BaseAndroidTest;

/**
 * Checks if we are able to fetch and propagate unhandled exception, occurring
 * in application under test and causing a crash, to the local end.
 */
@Ignore("These tests no longer work as the standalone server (which checks for crashes) is bypassed.")
public class UnhandledExceptionPropagatingTest extends BaseAndroidTest {

  @Before
  public void openApp() throws Exception {
    openStartActivity();
  }

  @Test
  public void shouldBeAbleToFetchExceptionAfterKeyEvent() {
    try {
      WebElement exceptionTestField = driver().findElement(By.id("exceptionTestField"));
      Assert.assertNotNull("TextField should be found.", exceptionTestField);
      exceptionTestField.sendKeys("a");
    } catch (Exception e) {
      String message = e.getMessage();
      Assert.assertTrue(message.contains("Unhandled exception from application under test."));
      try {
        // Recover from the application crash which was made by intention.
        startSelendroidServer();
      } catch (Exception e1) {
        Assert.fail("Unable to restart selendroid server");
      }
      return;
    }
    Assert.fail("Should catch the exception.");
  }

  @Test
  public void shouldBeAbleToFetchExceptionAfterMotionEvent() {
    try {
      WebElement exceptionTestButton = driver().findElement(By.id("exceptionTestButton"));
      Assert.assertNotNull("Button should be found.", exceptionTestButton);
      TouchActions tapAction = new TouchActions(driver()).singleTap(exceptionTestButton);
      tapAction.perform();
    } catch (Exception e) {
      String message = e.getMessage();
      Assert.assertTrue(message.contains("Unhandled exception from application under test."));
      try {
        // Recover from the application crash which was made by intention.
        startSelendroidServer();
      } catch (Exception e1) {
        Assert.fail("Unable to restart selendroid server");
      }
      return;
    }
    Assert.fail("Should catch the exception.");
  }

}
