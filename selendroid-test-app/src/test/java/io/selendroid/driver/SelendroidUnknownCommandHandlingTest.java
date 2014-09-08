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
package io.selendroid.driver;

import io.selendroid.support.BaseAndroidTest;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriverException;


public class SelendroidUnknownCommandHandlingTest extends BaseAndroidTest {
  @Test(expected = UnsupportedCommandException.class)
  public void testShouldNotBeAbleToGoForward() {
    driver().navigate().forward();
  }

  @Test(expected = UnsupportedCommandException.class)
  public void shouldNotRefresh() {
    driver().navigate().refresh();
  }

  @Test(expected = UnsupportedCommandException.class)
  public void shouldNotActivateIMEEngine() {
    driver().manage().ime().activateEngine("selendroid");
  }

  @Test(expected = UnsupportedCommandException.class)
  public void shouldNotDeactivateIME() {
    driver().manage().ime().deactivate();
  }

  @Test(expected = UnsupportedCommandException.class)
  public void shouldNotGetActiveEngine() {
    driver().manage().ime().getActiveEngine();
  }

  @Test(expected = UnsupportedCommandException.class)
  public void shouldNotGetAvailableEngines() {
    driver().manage().ime().getAvailableEngines();
  }

  @Test(expected = UnsupportedCommandException.class)
  public void shouldNotGetActivatedStateOfIME() {
    driver().manage().ime().isActivated();
  }

  @Test(expected = UnsupportedCommandException.class)
  public void shouldNotGetWindowPosition() {
    driver().manage().window().getPosition();
  }

  @Test(expected = UnsupportedCommandException.class)
  public void shouldNotGetWindowsMaximizedState() {
    driver().manage().window().maximize();
  }

  @Test(expected = UnsupportedCommandException.class)
  public void testShouldNotSetPosition() {
    Point targetPosition = new Point(1, 2);
    driver().manage().window().setPosition(targetPosition);
  }

  @Test(expected = UnsupportedCommandException.class)
  public void testShouldNotSetSize() {
    Dimension targetSize = new Dimension(320, 480);
    driver().manage().window().setSize(targetSize);
  }

  @Test(expected = WebDriverException.class)
  public void testShouldNotSetPageLoadTimeout() {
    driver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
  }
}
