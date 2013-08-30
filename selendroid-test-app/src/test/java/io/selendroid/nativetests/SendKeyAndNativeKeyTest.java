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

import static io.selendroid.waiter.TestWaiter.waitFor;
import io.selendroid.SelendroidKeys;
import io.selendroid.TestGroups;
import io.selendroid.support.BaseAndroidTest;
import io.selendroid.waiter.WaitingConditions;

import org.openqa.selenium.By;
import org.openqa.selenium.interactions.HasInputDevices;
import org.testng.Assert;
import org.testng.annotations.Test;
@Test(groups={TestGroups.NATIVE})
public class SendKeyAndNativeKeyTest extends BaseAndroidTest {
  public static final String ACTIVITY_CLASS = "io.selendroid.testapp."
      + "HomeScreenActivity";

  protected void precondition() {
    driver.switchTo().window(NATIVE_APP);
    driver.get("and-activity://" + ACTIVITY_CLASS);
    waitFor(WaitingConditions.driverUrlToBe(driver, "and-activity://HomeScreenActivity"));
  }

  @Test
  public void nativeSearchCanBeTriggered() throws Exception {
    precondition();

    ((HasInputDevices) driver).getKeyboard().sendKeys(SelendroidKeys.SEARCH);


    ((HasInputDevices) driver).getKeyboard().sendKeys("cars");

    ((HasInputDevices) driver).getKeyboard().sendKeys(SelendroidKeys.ENTER);
    waitFor(WaitingConditions.driverUrlToBe(driver, "and-activity://SearchUsersActivity"));
    
    Assert.assertNotNull(driver.findElement(By.linkText("Mercedes Benz")));
    Assert.assertTrue(driver.getPageSource().contains("Mercedes Benz"));
  }
}
