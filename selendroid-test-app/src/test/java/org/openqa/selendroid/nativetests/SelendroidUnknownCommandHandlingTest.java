package org.openqa.selendroid.nativetests;

import java.util.concurrent.TimeUnit;

import org.openqa.selendroid.support.BaseAndroidTest;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.UnsupportedCommandException;
import org.testng.annotations.Test;


public class SelendroidUnknownCommandHandlingTest extends BaseAndroidTest {

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGoBack() {
    driver.navigate().back();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGoForward() {
    driver.navigate().forward();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGoRefresh() {
    driver.navigate().refresh();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetWindowHandle() {
    driver.getWindowHandle();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetWindowHandles() {
    driver.getWindowHandles();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToAddACookie() {
    Cookie cookie = new Cookie("selendroid", "test");
    driver.manage().addCookie(cookie);
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToDeleteAllCookies() {
    driver.manage().deleteAllCookies();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToDeleteCookie() {
    Cookie cookie = new Cookie("selendroid", "test");
    driver.manage().deleteCookie(cookie);
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToDeleteCookieNamed() {
    driver.manage().deleteCookieNamed("selendroid");
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetNamedCookie() {
    driver.manage().getCookieNamed("selendroid");
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetCookies() {
    driver.manage().getCookies();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToActivateIMEEngine() {
    driver.manage().ime().activateEngine("selendroid");
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToDeactivateIME() {
    driver.manage().ime().deactivate();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetActiveEngine() {
    driver.manage().ime().getActiveEngine();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetAvailableEngines() {
    driver.manage().ime().getAvailableEngines();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetActivatedStateOfIME() {
    driver.manage().ime().isActivated();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetWindowPosition() {
    driver.manage().window().getPosition();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetWindowSize() {
    driver.manage().window().getSize();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToGetWindowsMaximizedState() {
    driver.manage().window().maximize();
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToSetPosition() {
    Point targetPosition = new Point(1, 2);
    driver.manage().window().setPosition(targetPosition);
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToSetSize() {
    Dimension targetSize = new Dimension(320, 480);
    driver.manage().window().setSize(targetSize);
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToSetPageLoadTimeout() {
    driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
  }

  @Test(expectedExceptions = {UnsupportedCommandException.class})
  public void testShouldNotBeAbleToSetScriptTimeout() {
    driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
  }
}
