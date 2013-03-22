package org.openqa.selendroid.webviewdrivertests;

import static org.openqa.selendroid.waiter.TestWaiter.waitFor;
import static org.openqa.selendroid.waiter.WaitingConditions.pageTitleToBe;

import java.util.concurrent.TimeUnit;

import org.openqa.selendroid.tests.internal.BaseAndroidTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WebElementInteractionTests extends BaseAndroidTest {
  @Test
  public void testShouldBeAbleToGetTextOfElement() {
    openWebdriverTestPage(HtmlTestData.XHTML_TEST_PAGE);
    WebElement element = driver.findElements(By.name("nameTest")).get(0);
    Assert.assertEquals(element.getText(), "click me");
  }

  @Test()
  public void testShouldBeAbleToClickOnElement() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement ckeckbox = driver.findElement(By.id("checkedchecky"));
    Assert.assertEquals(ckeckbox.isSelected(), true);
    ckeckbox.click();
    Assert.assertEquals(ckeckbox.isSelected(), false);
  }

  @Test
  public void testShouldBeAbleToGetAttributeOfTextField() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement button = driver.findElement(By.cssSelector("input[id='inputWithText']"));
    Assert.assertEquals(button.getAttribute("value"), "Example text");
    Assert.assertEquals(button.getAttribute("type"), "text");
  }

  @Test
  public void testShouldBeAbleToGetAttributeOfButton() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    WebElement button = driver.findElement(By.cssSelector("input[id='submitButton']"));
    Assert.assertEquals(button.getAttribute("value"), "Hello there");
  }

  @Test
  public void testShouldBeAbleToSendKeysAndClearAnElement() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    waitFor(pageTitleToBe(driver, "We Leave From Here"), 10, TimeUnit.SECONDS);

    WebElement inputField = driver.findElement(By.id("email"));
    String text = "a.anyString@not.existent%.1.de";
    inputField.sendKeys(text);
    Assert.assertEquals(inputField.getAttribute("value"), text);
    inputField.clear();
    Assert.assertEquals(inputField.getAttribute("text"), null);
  }

  @Test
  public void testShouldBeAbleToGetSelectedStateOfElement() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    waitFor(pageTitleToBe(driver, "We Leave From Here"), 10, TimeUnit.SECONDS);

    WebElement element = driver.findElement(By.id("checky"));
    Assert.assertEquals(element.isSelected(), false);
    element.click();
    Assert.assertEquals(element.isSelected(), true);
  }

  /**
   * Based on the default test emulator used to verify build that has a resolution of 320x480
   * pixels.
   */
  @Test
  public void testShouldBeAbleToGetSizeOfElement() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    waitFor(pageTitleToBe(driver, "We Leave From Here"), 10, TimeUnit.SECONDS);

    WebElement element = driver.findElement(By.id("checky"));
    Dimension size = element.getSize();
    Assert.assertEquals(size.width, 19);
    Assert.assertEquals(size.height, 19);
  }

  /**
   * Based on the default test emulator used to verify build that has a resolution of 320x480
   * pixels.
   */
  @Test
  public void testShouldBeAbleToGetLocationOfElement() {
    openWebdriverTestPage(HtmlTestData.FORM_PAGE);
    waitFor(pageTitleToBe(driver, "We Leave From Here"), 10, TimeUnit.SECONDS);

    WebElement element = driver.findElement(By.id("checky"));
    Point location = element.getLocation();
    Assert.assertEquals(location.x, 151);
    Assert.assertEquals(location.y, 113);
  }
}
