package io.selendroid.nativetests;

import static io.selendroid.waiter.TestWaiter.waitFor;
import static io.selendroid.waiter.TestWaiter.waitForElement;
import io.selendroid.TestGroups;
import io.selendroid.support.BaseAndroidTest;
import io.selendroid.waiter.WaitingConditions;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {TestGroups.NATIVE})
public class WaitForProgressBarGoneAwayTest extends BaseAndroidTest {
  public static final String ACTIVITY_CLASS = "io.selendroid.testapp." + "HomeScreenActivity";
  public static final By byIdUsernameLocator = By.id("label_username");
  public static final By byNameUsernameLocator = By.name("label_usernameCD");
  public static final By byLinkTextUsernameLocator = By.linkText("Username");


  protected void precondition() {
    driver.switchTo().window(NATIVE_APP);
    driver.get("and-activity://" + ACTIVITY_CLASS);
    waitFor(WaitingConditions.driverUrlToBe(driver, "and-activity://HomeScreenActivity"));
  }

  @Test
  public void testShouldBeAbleToPassWithCorrectTimeoutAndByIdLocator() {
    precondition();
    int timeout = 32;
    driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    startTimeOutTest(timeout, byIdUsernameLocator);
  }

  @Test
  public void testShouldBeAbleToPassWithCorrectTimeoutAndByNameLocator() {
    precondition();
    int timeout = 32;
    driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    startTimeOutTest(timeout, byNameUsernameLocator);
  }

  @Test
  public void testShouldBeAbleToPassWithCorrectTimeoutAndByLinkTextLocator() {
    precondition();
    int timeout = 32;
    driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    startTimeOutTest(timeout, byIdUsernameLocator);
  }

  @Test(enabled = true)
  public void testShouldNotBeAbleToPassWithTooShortTimeout() {
    precondition();
    int timeout = 5;
    driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    try {
      startTimeOutTest(timeout, byIdUsernameLocator);
      Assert.fail("This should not happen.");
    } catch (Exception e) {
      // expected
    }
  }

  private void startTimeOutTest(int timeout, By locatorUsernameLabel) {
    driver.findElement(By.id("waitingButtonTest")).click();
    // the popup dialog wait for some time until then the user registration page is opened
    Assert.assertEquals(driver.findElement(locatorUsernameLabel).getText(), "Username");
  }

  @Test
  public void testShouldBeAbleToWaitUntilToastWasDisplayed() throws Exception {
    driver.findElement(By.id("showToastButton")).click();
    WebElement toast = waitForElement(By.linkText(""), 4, driver);
    Assert.assertNotNull(toast);
  }
}
