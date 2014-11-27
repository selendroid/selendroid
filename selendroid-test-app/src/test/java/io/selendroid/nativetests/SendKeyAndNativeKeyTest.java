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

import io.selendroid.client.SelendroidKeys;
import io.selendroid.client.waiter.WaitingConditions;
import io.selendroid.support.BaseAndroidTest;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import static io.selendroid.client.waiter.TestWaiter.waitFor;


public class SendKeyAndNativeKeyTest extends BaseAndroidTest {
  @Test
  public void shouldTriggerNativeSearch() throws Exception {
    openStartActivity();

    driver().getKeyboard().sendKeys(SelendroidKeys.SEARCH);
    driver().getKeyboard().sendKeys("cars");

    driver().getKeyboard().sendKeys(SelendroidKeys.ENTER);
    waitFor(WaitingConditions.driverUrlToBe(driver(), "and-activity://SearchUsersActivity"));

    Assert.assertNotNull(driver().findElement(By.linkText("Mercedes Benz")));
    Assert.assertTrue(driver().getPageSource().contains("Mercedes Benz"));
  }
}
