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
package io.selendroid.webviewdrivertests;

import io.selendroid.support.BaseAndroidTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AlertHandlingTest extends BaseAndroidTest {

  @Before
  public void setupWebView() {
    openWebdriverTestPage(HtmlTestData.ACTUAL_XHTML_PAGE);
  }

  @Test
  public void canHandleChainOfAlerts() {
    driver().executeScript("setTimeout(function(){alert(confirm('really? ' + prompt('testin alerts')));}, 100)");
    Alert a = new WebDriverWait(driver(), 2).until(ExpectedConditions.alertIsPresent());
    Assert.assertEquals("testin alerts", a.getText());
    a.sendKeys("WAT");
    a.accept();
    a = driver().switchTo().alert();
    Assert.assertEquals("really? WAT", a.getText());
    a.dismiss();
    a = driver().switchTo().alert();
    Assert.assertEquals("false", a.getText());
    a.dismiss();
  }

  @Test
  public void blocksOtherCallsWhenAlertPresent() {
    driver().executeScript("setTimeout(function(){alert('alert present');}, 100)");
    Alert a = new WebDriverWait(driver(), 2).until(ExpectedConditions.alertIsPresent());
    Assert.assertEquals("alert present", a.getText());
    try {
      driver().findElement(By.linkText("Foo"));
      throw new RuntimeException("should have gotten an UnhandledAlertException");
    } catch (UnhandledAlertException uae) {
      // pass
    } finally {
      a.accept();
    }
  }

  @Test
  public void alertNotPresentErrorOccurs() {
    try {
      driver().switchTo().alert();
      throw new RuntimeException("should have gotten an NoAlertPresentException");
    } catch (NoAlertPresentException nape) {
    }
  }
}
