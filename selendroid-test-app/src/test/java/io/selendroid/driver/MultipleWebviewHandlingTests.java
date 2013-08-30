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
package io.selendroid.driver;

import static io.selendroid.waiter.TestWaiter.waitFor;
import io.selendroid.support.BaseAndroidTest;
import io.selendroid.waiter.WaitingConditions;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MultipleWebviewHandlingTests extends BaseAndroidTest {
  @Test
  public void testShouldBeAbleToReadWindowTitleOfEachWebView() {
    openMultipleWebViewActivity();

    driver.switchTo().window("WEBVIEW_0");
    Assert.assertEquals(driver.getTitle(), "content 1");
    driver.switchTo().window("WEBVIEW_1");
    Assert.assertEquals(driver.getTitle(), "content 2");
  }


  @Test
  public void assertsThatDriverIsAbleToGetWindowHandlesOnWebViewActivity() {
    openMultipleWebViewActivity();
    Set<String> windowHandles = driver.getWindowHandles();
    Assert.assertEquals(windowHandles.size(), 3);
    Assert.assertTrue(windowHandles.contains(NATIVE_APP), "Should be able to find native context");
    Assert.assertTrue(windowHandles.contains("WEBVIEW_1"),
        "Should be able to find webview context 1");
    Assert.assertTrue(windowHandles.contains("WEBVIEW_0"),
        "Should be able to find webview context 1");
  }

  private void openMultipleWebViewActivity() {
    String activityClass = "io.selendroid.testapp." + "MultipleWebViewsActivity";
    driver.switchTo().window(NATIVE_APP);
    driver.get("and-activity://" + activityClass);
    waitFor(WaitingConditions.driverUrlToBe(driver, "and-activity://MultipleWebViewsActivity"));
  }
}
