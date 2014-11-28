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
package io.selendroid.tests;

import io.selendroid.client.waiter.WaitingConditions;
import io.selendroid.support.BaseAndroidTest;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base Test to demonstrate how to test webviews with Selendroid.
 * 
 * @author ddary
 */
public class SayHelloWebviewTest extends BaseAndroidTest {
  @Test
  public void assertThatWebviewSaysHello() throws Exception {
    WebElement button = driver().findElement(By.id("buttonStartWebview"));

    button.click();
    WebDriverWait wait = new WebDriverWait(driver(), 10);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Go to home screen")));
    driver().switchTo().window("WEBVIEW");

    WebElement inputField = driver().findElement(By.id("name_input"));
    Assert.assertNotNull(inputField);
    inputField.clear();
    inputField.sendKeys("Dominik");

    WebElement car = driver().findElement(By.name("car"));
    Select preferedCar = new Select(car);
    preferedCar.selectByValue("audi");
    inputField.submit();

    WaitingConditions.pageTitleToBe(driver(), "Hello: Dominik");
  }
}
