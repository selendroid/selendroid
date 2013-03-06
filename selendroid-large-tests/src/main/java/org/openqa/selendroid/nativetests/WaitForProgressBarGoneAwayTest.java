package org.openqa.selendroid.nativetests;

import static org.openqa.selendroid.webviewdrivertests.waiter.TestWaiter.waitFor;

import org.openqa.selendroid.tests.internal.BaseAndroidTest;
import org.openqa.selendroid.webviewdrivertests.waiter.WaitingConditions;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WaitForProgressBarGoneAwayTest extends BaseAndroidTest {
  public static final String ACTIVITY_CLASS = "org.openqa.selendroid.testapp."
      + "HomeScreenActivity";

  protected void precondition() {
    driver.switchTo().window(NATIVE_APP);
    driver.get("and-activity://" + ACTIVITY_CLASS);
    waitFor(WaitingConditions.driverUrlToBe(driver, "and-activity://HomeScreenActivity"));
  }

  @Test
  public void progressBarIsPresentTest() throws Exception {
    precondition();

    driver.findElement(By.id("waitingButtonTest")).click();
    waitFor(WaitingConditions.alertToBePresent(driver));
    Assert.assertTrue(driver.getPageSource().contains("Mercedes Benz"));
  }
}
