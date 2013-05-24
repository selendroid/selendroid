package io.selendroid.driver;

import static io.selendroid.waiter.TestWaiter.waitFor;

import java.util.Set;

import io.selendroid.support.BaseAndroidTest;
import io.selendroid.waiter.WaitingConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WindowHandlingTests extends BaseAndroidTest {

  @Test
  public void assertsThatDriverIsAbleToGetCurrentNativeWindow() {
    String windowHandle = driver.getWindowHandle();
    Assert.assertEquals(windowHandle, NATIVE_APP);
  }

  @Test
  public void assertsThatDriverIsAbleToGetCurrentWebViewWindow() {
    openWebViewActivity();
    Assert.assertEquals(driver.getWindowHandle(), NATIVE_APP);
    driver.switchTo().window(WEBVIEW);
    Assert.assertEquals(driver.getWindowHandle(), WEBVIEW);
  }


  @Test
  public void assertsThatDriverIsAbleToGetWindowHandlesOnMainActivity() {
    Set<String> windowHandles = driver.getWindowHandles();
    Assert.assertEquals(windowHandles.iterator().next(), NATIVE_APP);
    Assert.assertEquals(windowHandles.size(), 1);
  }

  @Test
  public void assertsThatDriverIsAbleToGetWindowHandlesOnWebViewActivity() {
    openWebViewActivity();
    Set<String> windowHandles = driver.getWindowHandles();
    Assert.assertEquals(windowHandles.size(), 2);
    Assert.assertTrue(windowHandles.contains(NATIVE_APP), "Should be able to find native context");
    Assert.assertTrue(windowHandles.contains(WEBVIEW), "Should be able to find webview context");
  }

  private void openWebViewActivity() {
    String activityClass = "org.openqa.selendroid.testapp." + "WebViewActivity";
    driver.switchTo().window(NATIVE_APP);
    driver.get("and-activity://" + activityClass);
    waitFor(WaitingConditions.driverUrlToBe(driver, "and-activity://WebViewActivity"));
  }
}
