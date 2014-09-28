package io.selendroid.driver;

import io.selendroid.support.BaseAndroidTest;
import io.selendroid.webviewdrivertests.HtmlTestData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExecuteAsyncScriptTest extends BaseAndroidTest {

  private JavascriptExecutor executor;

  @Before
  public void asyncSetup() {
    driver().manage().timeouts().setScriptTimeout(0, TimeUnit.SECONDS);
    openWebdriverTestPage(HtmlTestData.ABOUT_BLANK);
    executor = driver();
  }

  @Test
  public void shouldNotTimeoutIfCallbackInvokedImmediately() {
    Object result = executor.executeAsyncScript("arguments[arguments.length - 1](123);");
    Assert.assertThat(result, instanceOf(Number.class));
    assertEquals(123, ((Number) result).intValue());
  }

  @Test
  public void shouldBeAbleToReturnJavascriptPrimitivesFromAsyncScripts_NeitherNullNorUndefined() {
    assertEquals(123, ((Number) executor.executeAsyncScript(
        "arguments[arguments.length - 1](123);")).longValue());
    assertEquals("abc", executor.executeAsyncScript("arguments[arguments.length - 1]('abc');"));
    assertFalse((Boolean) executor.executeAsyncScript("arguments[arguments.length - 1](false);"));
    assertTrue((Boolean) executor.executeAsyncScript("arguments[arguments.length - 1](true);"));
  }

  @Test
  public void shouldBeAbleToReturnJavascriptPrimitivesFromAsyncScripts_NullAndUndefined() {
    assertNull(executor.executeAsyncScript("arguments[arguments.length - 1](null)"));
    assertNull(executor.executeAsyncScript("arguments[arguments.length - 1]()"));
  }

  @Test
  public void shouldBeAbleToReturnAnArrayLiteralFromAnAsyncScript() {
    Object result = executor.executeAsyncScript("arguments[arguments.length - 1]([]);");
    assertNotNull("Expected not to be null!", result);
    assertThat(result, instanceOf(List.class));
    assertTrue(((List<?>) result).isEmpty());
  }

  @Test
  public void shouldBeAbleToReturnAnArrayObjectFromAnAsyncScript() {
    Object result = executor.executeAsyncScript("arguments[arguments.length - 1](new Array());");
    assertNotNull("Expected not to be null!", result);
    assertThat(result, instanceOf(List.class));
    assertTrue(((List<?>) result).isEmpty());
  }

  @Test
  public void shouldBeAbleToReturnArraysOfPrimitivesFromAsyncScripts() {
    Object result = executor.executeAsyncScript(
        "arguments[arguments.length - 1]([null, 123, 'abc', true, false]);");

    assertNotNull(result);
    assertThat(result, instanceOf(List.class));

    Iterator<?> results = ((List<?>) result).iterator();
    assertNull(results.next());
    assertEquals(123, ((Number) results.next()).longValue());
    assertEquals("abc", results.next());
    assertTrue((Boolean) results.next());
    assertFalse((Boolean) results.next());
    assertFalse(results.hasNext());
  }

  @Test
  public void shouldBeAbleToReturnWebElementsFromAsyncScripts() {
    Object result = executor.executeAsyncScript("arguments[arguments.length - 1](document.body);");
    assertThat(result, instanceOf(WebElement.class));
    assertEquals("body", ((WebElement) result).getTagName().toLowerCase());
  }

  @Test
  public void shouldBeAbleToReturnArraysOfWebElementsFromAsyncScripts() {
    Object result = executor.executeAsyncScript(
        "arguments[arguments.length - 1]([document.body, document.body]);");
    assertNotNull(result);
    assertThat(result, instanceOf(List.class));

    List<?> list = (List<?>) result;
    assertEquals(2, list.size());
    assertThat(list.get(0), instanceOf(WebElement.class));
    assertThat(list.get(1), instanceOf(WebElement.class));
    assertEquals("body", ((WebElement) list.get(0)).getTagName().toLowerCase());
    assertEquals(list.get(0), list.get(1));
  }

  @Test
  public void shouldTimeoutIfScriptDoesNotInvokeCallback() {
    try {
      // Script is expected to be async and explicitly callback, so this should timeout.
      executor.executeAsyncScript("return 1 + 2;");
      fail("Should have thrown a TimeOutException!");
    } catch (TimeoutException exception) {
      // Do nothing.
    }
  }

  @Test
  public void shouldTimeoutIfScriptDoesNotInvokeCallbackWithAZeroTimeout() {
    try {
      executor.executeAsyncScript("window.setTimeout(function() {}, 0);");
      fail("Should have thrown a TimeOutException!");
    } catch (TimeoutException exception) {
      // Do nothing.
    }
  }

  @Test
  public void shouldNotTimeoutIfScriptCallsbackInsideAZeroTimeout() {
    executor.executeAsyncScript(
        "var callback = arguments[arguments.length - 1];" +
            "window.setTimeout(function() { callback(123); }, 0)");
  }

  @Test
  public void shouldTimeoutIfScriptDoesNotInvokeCallbackWithLongTimeout() {
    driver().manage().timeouts().setScriptTimeout(500, TimeUnit.MILLISECONDS);
    try {
      executor.executeAsyncScript(
          "var callback = arguments[arguments.length - 1];" +
              "window.setTimeout(callback, 1500);");
      fail("Should have thrown a TimeOutException!");
    } catch (TimeoutException exception) {
      // Do nothing.
    }
  }

  @Test
  public void shouldDetectPageLoadsWhileWaitingOnAnAsyncScriptAndReturnAnError() {
    driver().manage().timeouts().setScriptTimeout(100, TimeUnit.MILLISECONDS);
    try {
      executor.executeAsyncScript("window.location = '" + HtmlTestData.JAVASCRIPT_PAGE + "';");
      fail();
    } catch (WebDriverException expected) {
    }
  }

  @Test
  public void shouldCatchErrorsWhenExecutingInitialScript() {
    try {
      executor.executeAsyncScript("throw Error('you should catch this!');");
      fail();
    } catch (WebDriverException expected) {
    }
  }

  @Test
  public void shouldNotTimeoutWithMultipleCallsTheFirstOneBeingSynchronous() {
    driver().manage().timeouts().setScriptTimeout(10, TimeUnit.MILLISECONDS);
    assertTrue((Boolean) executor.executeAsyncScript("arguments[arguments.length - 1](true);"));
    assertTrue((Boolean) executor.executeAsyncScript(
        "var cb = arguments[arguments.length - 1]; window.setTimeout(function(){cb(true);}, 9);"));
  }

  @Test
  @Ignore("We currently don't propagate causes properly when redirecting through Standalone.")
  public void shouldCatchErrorsWithMessageAndStacktraceWhenExecutingInitialScript() {
    String js = "function functionB() { throw Error('errormessage'); };"
        + "function functionA() { functionB(); };"
        + "functionA();";
    try {
      executor.executeAsyncScript(js);
      fail("Expected an exception");
    } catch (WebDriverException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("errormessage"));

      StackTraceElement [] st = e.getCause().getStackTrace();
      boolean seen = false;
      for (StackTraceElement s: st) {
        if (s.getMethodName().equals("functionB")) {
          seen = true;
        }
      }
      assertTrue("Stacktrace has not js method info", seen);
    }
  }

  @Test
  public void shouldBeAbleToPassMultipleArgumentsToAsyncScripts() {
    Number result = (Number) executor
        .executeAsyncScript("arguments[arguments.length - 1](arguments[0] + arguments[1]);", 1, 2);
    assertEquals(3, result.intValue());
  }
}
