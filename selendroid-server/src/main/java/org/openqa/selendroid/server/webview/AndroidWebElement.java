/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.server.webview;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Semaphore;

import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.android.AndroidKeys;
import org.openqa.selendroid.android.internal.Point;
import org.openqa.selendroid.server.exceptions.ElementNotVisibleException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.server.model.By;
import org.openqa.selendroid.server.webview.js.AndroidAtoms;

import android.app.Activity;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;

public class AndroidWebElement implements AndroidElement {
  private String id;
  private WebView webview;
  private SelendroidWebDriver driver;

  public AndroidWebElement(String id, WebView webview, SelendroidWebDriver driver) {
    this.id = id;
    this.webview = webview;
    this.driver = driver;
  }

  @Override
  public AndroidElement getParent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<AndroidElement> getChildren() {
    throw new UnsupportedOperationException();
  }

  @Override
  public AndroidElement findElement(By c) throws NoSuchElementException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T findElement(Class<T> type, By c) throws NoSuchElementException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void enterText(CharSequence text) {
    sendKeys(text);
  }

  public void sendKeys(final CharSequence value) {
    if (value == null || value.length() == 0) {
      return;
    }
    if (!isEnabled()) {
      // throw new NativeAndroidDriverExcepction("Cannot send keys to disabled element.");
    }
    // focus on the element
    // this.click();
    // driver.waitUntilEditAreaHasFocus();
    // Move the cursor to the end of the test input.
    // The trick is to set the value after the cursor
    driver.executeScript("arguments[0].focus();arguments[0].value=arguments[0].value;", this);
    final WebView view = webview;
    final Semaphore sem = new Semaphore(0);
    // ServerInstrumentation.getInstance().runOnUiThread(new Runnable() {
    //
    // @Override
    // public void run() {
    EventSender.sendKeys(view, value);
    sem.release();
    // }
    // });

  }

  private static int indexOfSpecialKey(CharSequence string, int startIndex) {
    for (int i = startIndex; i < string.length(); i++) {
      if (AndroidKeys.hasAndroidKeyEvent(string.charAt(i))) {
        return i;
      }
    }
    return string.length();
  }

  public boolean isEnabled() {
    return ((JsonElement) driver.executeAtom(AndroidAtoms.IS_ENABLED, this)).getAsBoolean();
  }

  @Override
  public String getText() {
    return (String) driver.executeAtom(AndroidAtoms.GET_TEXT, this);
  }

  public String getTagName() {
    return (String) driver.executeScript("return arguments[0].tagName", this);
  }

  public boolean isDisplayed() {
    return Boolean.getBoolean((String) driver.executeAtom(AndroidAtoms.IS_DISPLAYED, this));
  }

  /**
   * Where on the page is the top left-hand corner of the rendered element? it's part of
   * RenderedWebElement
   * 
   * @return A point, containing the location of the top left-hand corner of the element
   */
  public Point getLocation() {
    Map<String, Long> map =
        (Map<String, Long>) driver.executeAtom(AndroidAtoms.GET_TOP_LEFT_COORDINATES, this);
    return new Point(map.get("x").intValue(), map.get("y").intValue());
  }

  private Point getCenterCoordinates() {
    if (!isDisplayed()) {
      throw new ElementNotVisibleException(
          "This WebElement is not visisble and may not be clicked.");
    }
    driver.setEditAreaHasFocus(false);
    Point topLeft = getLocation();
    String sizeJs =
        "var __webdriver_w = 0;" + "var __webdriver_h = 0;"
            + "if (arguments[0].getClientRects && arguments[0].getClientRects()[0]) {"
            + "  __webdriver_w = arguments[0].getClientRects()[0].width;"
            + "  __webdriver_h = arguments[0].getClientRects()[0].height;" + " } else {"
            + "  __webdriver_w = arguments[0].offsetWidth;"
            + "  __webdriver_h = arguments[0].offsetHeight;"
            + "}; return __webdriver_w + ',' + __webdriver_h;";
    String[] result = ((String) driver.executeScript(sizeJs, this)).split(",");
    return new Point(topLeft.x + Integer.parseInt(result[0]) / 2, topLeft.y
        + Integer.parseInt(result[1]) / 2);
  }

  @Override
  public void click() {
    System.out.println("click web element");
    String tagName = getTagName();
    if (tagName != null && "OPTION".equals(tagName.toUpperCase())) {
      driver.resetPageIsLoading();
      driver.executeAtom(AndroidAtoms.CLICK, this);
      driver.waitForPageToLoad();
    }

    Point center = getCenterCoordinates();
    long downTime = SystemClock.uptimeMillis();
    final List<MotionEvent> events = Lists.newArrayList();

    MotionEvent downEvent =
        MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, center.x,
            center.y, 0);
    events.add(downEvent);
    MotionEvent upEvent =
        MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, center.x,
            center.y, 0);

    events.add(upEvent);

    driver.resetPageIsLoading();
    Activity current = ServerInstrumentation.getInstance().getCurrentActivity();
    EventSender.sendMotion(events, webview, current);

    // If the page started loading we should wait
    // until the page is done loading.
    driver.waitForPageToLoad();
  }

  public void submit() {
    System.out.println("submit");
    String tagName = getTagName();
    if ("button".equalsIgnoreCase(tagName) || "submit".equalsIgnoreCase(getAttribute("type"))
        || "img".equalsIgnoreCase(tagName)) {
      this.click();
    } else {
      driver.resetPageIsLoading();
      driver.executeAtom(AndroidAtoms.SUBMIT, this);
      driver.waitForPageToLoad();
    }
  }

  public void clear() {
    driver.executeAtom(AndroidAtoms.CLEAR, this);
  }

  public String getAttribute(String name) {
    return ((JsonElement) driver.executeAtom(AndroidAtoms.GET_ATTRIBUTE_VALUE, this, name))
        .getAsString();
  }

  public String getId() {
    return id;
  }
}
