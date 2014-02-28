/*
 * Copyright 2014 eBay Software Foundation and selendroid committers.
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

import static io.selendroid.waiter.TestWaiter.waitFor;
import static io.selendroid.waiter.WaitingConditions.driverUrlToBe;
import io.selendroid.SelendroidCapabilities;
import io.selendroid.SelendroidDriver;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

public class AdbConnectionTest {
  private SelendroidDriver driver;

  @BeforeClass
  public static void startStandalone() {
    // start selendroid standalone manually
  }

  @Before
  public void startSelendroidDriver() throws Exception {
    driver =
        new SelendroidDriver(new SelendroidCapabilities("io.selendroid.testapp:0.9.0-SNAPSHOT"));
  }

  @Test
  public void shouldTapViaAdb() {
    Point buttonLocation = driver.findElement(By.id("startUserRegistration")).getLocation();
    driver.getAdbConnection().tap(buttonLocation.x, buttonLocation.y);
    Assert.assertEquals(driver.getCurrentUrl(), "and-activity://RegisterUserActivity");
  }

  @Test
  public void shouldSendTextViaAdb() {
    WebElement input = driver.findElement(By.id("my_text_field"));
    input.click();
    String text = "textViaAdb";
    driver.getAdbConnection().sendText(text);
    Assert.assertEquals(text, input.getText());
  }

  @Test
  public void shouldSendKeyEventViaAdb() {
    driver.findElement(By.id("startUserRegistration")).click();
    Assert.assertEquals(driver.getCurrentUrl(), "and-activity://RegisterUserActivity");
    // 4 is back key, will go back to the previous activity.
    driver.getAdbConnection().sendKeyEvent(4);
    waitFor(driverUrlToBe(driver, "and-activity://HomeScreenActivity"), 5, TimeUnit.SECONDS);
  }

  @After
  public void quitDriver() {
    driver.quit();
  }
}
