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

import static io.selendroid.client.waiter.TestWaiter.waitFor;
import static io.selendroid.client.waiter.WaitingConditions.driverUrlToBe;
import static io.selendroid.client.waiter.WaitingConditions.elementTextToContain;
import io.selendroid.client.SelendroidKeys;
import io.selendroid.support.BaseAndroidTest;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import android.view.KeyEvent;

public class AdbConnectionTest extends BaseAndroidTest {
  @Test
  public void shouldTapViaAdb() {
    Point buttonLocation = driver().findElement(By.id("startUserRegistration")).getLocation();
    driver().getAdbConnection().tap(buttonLocation.x, buttonLocation.y);
    waitFor(driverUrlToBe(driver(), "and-activity://RegisterUserActivity"), 10, TimeUnit.SECONDS);
  }

  @Test
  public void shouldExecuteShellCommand() {
    Point buttonLocation = driver().findElement(By.id("startUserRegistration")).getLocation();
    String command = String.format("input tap %s %s", buttonLocation.x, buttonLocation.y);
    driver().getAdbConnection().executeShellCommand(command);
    waitFor(driverUrlToBe(driver(), "and-activity://RegisterUserActivity"), 10, TimeUnit.SECONDS);
  }

  @Test
  public void shouldSendTextViaAdb() {
    final WebElement input = driver().findElement(By.id("my_text_field"));
    input.click();
    final String text = "textViaAdb";
    driver().getAdbConnection().sendText(text);
    waitFor(elementTextToContain( input, text), 10, TimeUnit.SECONDS);
  }

  @Test
  public void shouldSendKeyEventViaAdb() {
    driver().get(HOMESCREEN_ACTIVITY);
    driver().findElement(By.id("startUserRegistration")).click();
    waitFor(driverUrlToBe(driver(), "and-activity://RegisterUserActivity"), 5, TimeUnit.SECONDS);

    // 4 is back key, will go back to the previous activity.
    driver().getAdbConnection().sendKeyEvent(KeyEvent.KEYCODE_BACK);
    
    waitFor(driverUrlToBe(driver(), "and-activity://HomeScreenActivity"), 10, TimeUnit.SECONDS);
  }

  @After
  public void quitDriver() {
    if (driver() != null) {
      driver().quit();
    }
  }
}
