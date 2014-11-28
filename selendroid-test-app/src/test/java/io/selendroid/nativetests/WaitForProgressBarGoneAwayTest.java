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

import static io.selendroid.client.waiter.TestWaiter.waitFor;
import static io.selendroid.client.waiter.TestWaiter.waitForElement;
import io.selendroid.client.waiter.WaitingConditions;
import io.selendroid.support.BaseAndroidTest;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


public class WaitForProgressBarGoneAwayTest extends BaseAndroidTest {
  public static final String ACTIVITY_CLASS = "io.selendroid.testapp." + "HomeScreenActivity";
  public static final By byIdUsernameLocator = By.id("label_username");
  public static final By byNameUsernameLocator = By.name("label_usernameCD");
  public static final By byLinkTextUsernameLocator = By.linkText("Username");
  private final int timeout = 12;

  protected void precondition() {
    driver().switchTo().window(NATIVE_APP);
    driver().get("and-activity://" + ACTIVITY_CLASS);
    waitFor(WaitingConditions.driverUrlToBe(driver(), "and-activity://HomeScreenActivity"));
  }

  @Test
  public void shouldPassWithRightTimeoutUsingIdLocator() {
    precondition();

    driver().manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    startTimeOutTest(timeout, byIdUsernameLocator);
  }

  @Test
  public void shouldPassWithRightTimeoutUsingNameLocator() {
    precondition();
    driver().manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    startTimeOutTest(timeout, byNameUsernameLocator);
  }

  @Test
  public void shouldPassWithRightTimeoutUsingLinkTextLocator() {
    precondition();
    driver().manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    startTimeOutTest(timeout, byIdUsernameLocator);
  }

  @Test
  public void shouldNotPassWithTooShortTimeout() {
    precondition();
    int timeout = 5;
    driver().manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    try {
      startTimeOutTest(timeout, byIdUsernameLocator);
      Assert.fail("This should not happen.");
    } catch (Exception e) {
      // expected
    }
  }

  private void startTimeOutTest(int timeout, By locatorUsernameLabel) {
    driver().findElement(By.id("waitingButtonTest")).click();
    // the popup dialog wait for some time until then the user registration page is opened
    Assert.assertEquals(driver().findElement(locatorUsernameLabel).getText(), "Username");
  }

  @Test
  public void shouldWaitUntilToastIsDisplayed() throws Exception {
    precondition();
    driver().findElement(By.id("showToastButton")).click();
    WebElement toast = waitForElement(By.linkText("Hello selendroid toast!"), 4, driver());
    Assert.assertNotNull(toast);
  }
}
