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
package org.openqa.selendroid.server.model;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.android.internal.Point;
import org.openqa.selendroid.server.exceptions.ElementNotVisibleException;
import org.openqa.selendroid.server.model.internal.AbstractWebElementContext;
import org.openqa.selendroid.server.model.js.AndroidAtoms;
import org.openqa.selendroid.server.webview.EventSender;

import android.app.Activity;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AndroidWebElement implements AndroidElement {
  private final String id;
  private WebView webview;
  private SelendroidWebDriver driver;
  private SearchContext elementContext = null;


  private class ElementSearchContext extends AbstractWebElementContext {
    public ElementSearchContext(KnownElements knownElements, WebView webview,
        SelendroidWebDriver driver) {
      super(knownElements, webview, driver);
    }

    @Override
    protected AndroidElement lookupElement(String strategy, String locator) {
      JsonElement result =
          (JsonElement) driver.executeAtom(AndroidAtoms.FIND_ELEMENT, strategy, locator, id);
      return replyElement(result);
    }

    @Override
    protected List<AndroidElement> lookupElements(String strategy, String locator) {
      JsonElement result =
          (JsonElement) driver.executeAtom(AndroidAtoms.FIND_ELEMENTS, strategy, locator, id);

      return replyElements(result);
    }
  }

  public AndroidWebElement(String id, WebView webview, SelendroidWebDriver driver,
      KnownElements knownElements) {
    this.id = id;
    this.webview = webview;
    this.driver = driver;
    this.elementContext = new ElementSearchContext(knownElements, webview, driver);
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
  public AndroidElement findElement(By by) throws NoSuchElementException {
    return by.findElement(elementContext);
  }

  public List<AndroidElement> findElements(By by) throws NoSuchElementException {
    return by.findElements(elementContext);
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
    // driver.executeScript("arguments[0].focus();arguments[0].value=arguments[0].value;", this);
    final WebView view = webview;
    // final Semaphore sem = new Semaphore(0);
    // ServerInstrumentation.getInstance().runOnUiThread(new Runnable() {
    //
    // @Override
    // public void run() {
    EventSender.sendKeys(view, value);
    // sem.release();
    // }
    // });

  }

  public boolean isEnabled() {
    return ((JsonElement) driver.executeAtom(AndroidAtoms.IS_ENABLED, this)).getAsBoolean();
  }

  public boolean isSelected() {
    Boolean selected =
        ((JsonElement) driver.executeAtom(AndroidAtoms.IS_SELECTED, this)).getAsBoolean();
    return selected;
  }

  @Override
  public String getText() {
    JsonElement response = (JsonElement) driver.executeAtom(AndroidAtoms.GET_TEXT, this);
    if (response == null) {
      return null;
    }
    return response.getAsString();
  }

  public String getTagName() {
    return (String) driver.executeScript("return arguments[0].tagName", this);
  }

  public boolean isDisplayed() {
    JsonElement result = (JsonElement) driver.executeAtom(AndroidAtoms.IS_DISPLAYED, this);
    if (result != null) {
      return result.getAsBoolean();
    }
    return false;
  }

  /**
   * Where on the page is the top left-hand corner of the rendered element? it's part of
   * RenderedWebElement
   * 
   * @return A point, containing the location of the top left-hand corner of the element
   */
  public Point getLocation() {
    Object object = driver.executeAtom(AndroidAtoms.GET_TOP_LEFT_COORDINATES, this);
    JsonObject result = ((JsonElement) object).getAsJsonObject();

    return new Point(result.get("x").getAsInt(), result.get("y").getAsInt());
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
    JsonObject response =
        new JsonParser().parse((String) driver.executeScript(sizeJs, this)).getAsJsonObject();
    String[] result = response.get("value").getAsString().split(",");
    return new Point(topLeft.x + Integer.parseInt(result[0]) / 2, topLeft.y
        + Integer.parseInt(result[1]) / 2);
  }

  @Override
  public void click() {
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
    JsonElement element =
        ((JsonElement) driver.executeAtom(AndroidAtoms.GET_ATTRIBUTE_VALUE, this, name));
    if (element == null) {
      return null;
    }
    return element.getAsString();
  }

  public String getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AndroidWebElement other = (AndroidWebElement) obj;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    return true;
  }

}
