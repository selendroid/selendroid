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
package io.selendroid.webviewdrivertests;

import io.selendroid.support.BaseAndroidTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.TimeUnit;

import static io.selendroid.client.waiter.TestWaiter.waitFor;
import static io.selendroid.client.waiter.WaitingConditions.pageTitleToBe;

public class WebElementInteractionTest extends BaseAndroidTest {
  @Test
  public void shouldGetTextOfElement() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement element = driver().findElements(By.name("nameTest")).get(0);
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test()
  public void shouldClickOnElement() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement ckeckbox = driver().findElement(By.id("checkedchecky"));
    Assert.assertEquals(ckeckbox.isSelected(), true);
    ckeckbox.click();
    Assert.assertEquals(ckeckbox.isSelected(), false);
  }

  @Test
  public void shouldGetAttributeOfTextField() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement button = driver().findElement(By.cssSelector("input[id='inputWithText']"));
    Assert.assertEquals(button.getAttribute("value"), "Example text");
    Assert.assertEquals(button.getAttribute("type"), "text");
  }

  @Test
  public void shouldGetTagNameOfElement() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement button = driver().findElement(By.cssSelector("input[id='inputWithText']"));

    Assert.assertEquals(button.getTagName(), "input"); //< Specs "dictate" the tag names to be lower-case
  }

  @Test
  public void shouldGetAttributeOfButton() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement button = driver().findElement(By.cssSelector("input[id='submitButton']"));
    Assert.assertEquals(button.getAttribute("value"), "Hello there");
  }

  @Test
  public void shouldSendKeysAndClearAnElement() {
    givenWebViewWithFormPageLoaded();

    WebElement inputField = driver().findElement(By.id("email"));
    String text = "a.anyString@not.existent%.1.de";
    inputField.sendKeys(text);
    Assert.assertEquals(inputField.getAttribute("value"), text);
    inputField.clear();
    Assert.assertEquals(inputField.getAttribute("text"), null);
  }

  @Test
  public void shouldTriggerInputEventWhenSendTextWithNativeKeyboard() throws Exception {
    // ensure native keyboard is used
    // according to io.selendroid.server.handler.SendKeysToElement.safeHandle()
    Assert.assertTrue("Should use native keyboard", !hasNativeEventsDisabled());

    givenWebViewWithFormPageLoaded();
    whenSendingKeysToInputElement();
    thenInputEventShouldBeTriggeredOnInputElement();
  }

  @Test
  public void shouldTriggerInputEventWhenSendTextWithoutNativeKeyboard() throws Exception {
    givenWebDriverWithNativeEventsDisabled();
    givenWebViewWithFormPageLoaded();
    whenSendingKeysToInputElement();
    thenInputEventShouldBeTriggeredOnInputElement();
  }

  @Test
  public void shouldGetSelectedStateOfElement() {
    givenWebViewWithFormPageLoaded();

    WebElement element = driver().findElement(By.id("checky"));
    Assert.assertEquals(element.isSelected(), false);
    element.click();
    Assert.assertEquals(element.isSelected(), true);
  }

  /**
   * Based on the default test emulator used to verify build that has a resolution of 320x480
   * pixels.
   */
  @Test
  public void shouldGetSizeOfElement() {
    givenWebViewWithFormPageLoaded();

    WebElement element = driver().findElement(By.id("checky"));
    Dimension size = element.getSize();
    Assert.assertEquals(size.width, 19);
    Assert.assertEquals(size.height, 19);
  }

  /**
   * Based on the default test emulator used to verify build that has a resolution of 320x480
   * pixels.
   */
  @Test
  public void shouldGetLocationOfElement() {
    givenWebViewWithFormPageLoaded();

    WebElement element = driver().findElement(By.id("checky"));
    Point location = element.getLocation();
    Assert.assertTrue(location.x >= 120);
    Assert.assertTrue(location.y >= 100);
  }

  @Test
  public void shouldExecuteSimpleJavaScript() {
    givenWebViewWithFormPageLoaded();

    String name = (String) executeJavaScript("return document.title");
    Assert.assertEquals(name, "We Leave From Here");
  }

  @Test
  public void shouldGetDisplayedStateOfElement() {
    givenWebViewWithFormPageLoaded();

    WebElement element = driver().findElement(By.id("checky"));
    Assert.assertEquals(element.isDisplayed(), true);
  }

  @Test
  public void shouldGetEnbledStateOfElement() {
    givenWebViewWithFormPageLoaded();

    WebElement element = driver().findElement(By.id("checky"));
    Assert.assertEquals(element.isEnabled(), true);
  }

  @Test()
  public void shouldSubmitAnElement() {
    openWebdriverTestPage(HtmlTestData.SAY_HELLO_DEMO);

    WebElement inputField = driver().findElement(By.id("name_input"));
    Assert.assertNotNull(inputField);
    inputField.clear();
    inputField.sendKeys("Selendroid");

    inputField.submit();
    String name = (String) executeJavaScript("return document.title");
    Assert.assertEquals(name, "Hello: Selendroid");
  }

  private void givenWebDriverWithNativeEventsDisabled() throws Exception {
    closeDriver();

    final DesiredCapabilities caps = getDefaultCapabilities();
    caps.setCapability(CapabilityType.HAS_NATIVE_EVENTS, false);

    createDriver(caps);

    // ensure native keyboard is NOT used
    // according to io.selendroid.server.handler.SendKeysToElement.safeHandle()
    Assert.assertTrue("Should NOT use native keyboard", hasNativeEventsDisabled());
  }

  protected void givenWebViewWithFormPageLoaded() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    waitFor(pageTitleToBe(driver(), "We Leave From Here"), 10, TimeUnit.SECONDS);
  }

  protected Object executeJavaScript(String script) {
    return ((JavascriptExecutor) driver()).executeScript(script);
  }

  private boolean hasNativeEventsDisabled() {
    final Object capability = driver().getCapabilities().getCapability(CapabilityType.HAS_NATIVE_EVENTS);
    return Boolean.FALSE.equals(capability);
  }

  private void whenSendingKeysToInputElement() {
    executeJavaScript("window._input_event_triggered = false;   " +
            "document.getElementById('email').addEventListener( " +
            "  'input', function(event){                        " +
            "    window._input_event_triggered = true;          " +
            "  }                                                " +
            ");                                                 ");
    driver().findElement(By.id("email")).sendKeys("test");
  }

  private void thenInputEventShouldBeTriggeredOnInputElement() {
    final Boolean isInputEventTriggered = (Boolean) executeJavaScript("return window._input_event_triggered;");
    Assert.assertTrue("Input event must be triggered on sendKeys", isInputEventTriggered);
  }
}
