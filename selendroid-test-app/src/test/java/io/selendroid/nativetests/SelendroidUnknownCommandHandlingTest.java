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

import io.selendroid.support.BaseAndroidTest;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.UnsupportedCommandException;
import org.testng.annotations.Test;


public class SelendroidUnknownCommandHandlingTest extends BaseAndroidTest {
  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGoForward() {
    driver.navigate().forward();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGoRefresh() {
    driver.navigate().refresh();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToActivateIMEEngine() {
    driver.manage().ime().activateEngine("selendroid");
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToDeactivateIME() {
    driver.manage().ime().deactivate();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetActiveEngine() {
    driver.manage().ime().getActiveEngine();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetAvailableEngines() {
    driver.manage().ime().getAvailableEngines();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetActivatedStateOfIME() {
    driver.manage().ime().isActivated();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetWindowPosition() {
    driver.manage().window().getPosition();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetWindowSize() {
    driver.manage().window().getSize();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetWindowsMaximizedState() {
    driver.manage().window().maximize();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToSetPosition() {
    Point targetPosition = new Point(1, 2);
    driver.manage().window().setPosition(targetPosition);
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToSetSize() {
    Dimension targetSize = new Dimension(320, 480);
    driver.manage().window().setSize(targetSize);
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToSetPageLoadTimeout() {
    driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToSetScriptTimeout() {
    driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
  }
}
