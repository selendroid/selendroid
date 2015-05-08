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
package io.selendroid.server.model;

import io.selendroid.server.android.internal.Dimension;
import io.selendroid.server.android.internal.Point;
import io.selendroid.server.common.exceptions.ElementNotVisibleException;
import io.selendroid.server.common.exceptions.NoSuchElementException;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.model.interactions.AndroidCoordinates;
import io.selendroid.server.model.interactions.Coordinates;
import io.selendroid.server.model.internal.AbstractWebElementContext;
import io.selendroid.server.model.js.AndroidAtoms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;

public class AndroidWebElement implements AndroidElement {
  private final String id;
  private WebView webview;
  private KnownElements ke;
  private SelendroidWebDriver driver;
  private SearchContext elementContext = null;
  private Coordinates coordinates = null;
  private final static long SENDKEYS_TIMEOUT = 5000L;
  private final static long POLLING_INTERVAL = 50L;


  private class ElementSearchContext extends AbstractWebElementContext {
    public ElementSearchContext(KnownElements knownElements, WebView webview,
        SelendroidWebDriver driver) {
      super(knownElements, webview, driver);
    }

    @Override
    protected AndroidElement lookupElement(String strategy, String locator) {
      JSONObject result =
          (JSONObject) driver.executeAtom(AndroidAtoms.FIND_ELEMENT, null, strategy, locator,
              AndroidWebElement.this);
      AndroidElement element = replyElement(result);
      if (element == null) {
        throw new NoSuchElementException("element was not found.");
      }
      return element;
    }

    @Override
    protected List<AndroidElement> lookupElements(String strategy, String locator) {
      JSONArray result =
          (JSONArray) driver.executeAtom(AndroidAtoms.FIND_ELEMENTS, null, strategy, locator,
              AndroidWebElement.this);

      List<AndroidElement> elements = replyElements(result);
      return elements;
    }
  }

  public AndroidWebElement(String id, WebView webview, SelendroidWebDriver driver,
      KnownElements knownElements) {
    this.id = id;
    this.webview = webview;
    this.driver = driver;
    this.elementContext = new ElementSearchContext(knownElements, webview, driver);
    this.ke = knownElements;
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
  public void enterText(CharSequence... keysToSend) {
    StringBuilder sb = new StringBuilder();
    for (CharSequence keys : keysToSend) {
      sb.append(keys);
    }
    sendKeys(sb.toString());
  }

  public void sendKeys(final CharSequence value) {
    if (value == null || value.length() == 0) {
      return;
    }
    // focus on the element
    this.click();
    driver.waitUntilEditAreaHasFocus();
    // Move the cursor to the end of the test input.
    // The trick is to set the value after the cursor
    String originalText =
        (String) driver.executeScript("arguments[0].focus();"
            + "arguments[0].value=arguments[0].value;return arguments[0].value", this, null);

    driver.getKeySender().send(value);

    // wait for keys to have been sent to the element
    long timeout = System.currentTimeMillis() + SENDKEYS_TIMEOUT;
    while (timeout < System.currentTimeMillis()) {
      String newValue = (String) driver.executeScript("return arguments[0].value;");
      if (newValue.length() > originalText.length()) break;
      try {
        Thread.sleep(POLLING_INTERVAL);
      } catch (InterruptedException e) {
        // being kind to the system, if system is interrupting just break out.
        break;
      }
    }
  }

  public boolean isEnabled() {
    return (Boolean) driver.executeAtom(AndroidAtoms.IS_ENABLED, null, this);
  }

  public boolean isSelected() {
    return (Boolean) driver.executeAtom(AndroidAtoms.IS_SELECTED, null, this);
  }

  @Override
  public String getText() {
    return (String) driver.executeAtom(AndroidAtoms.GET_TEXT, null, this);
  }

  public String getTagName() {
    Object result = driver.executeScript("return arguments[0].tagName", this, null);
    if (result == null) {
      return null;
    }
    try {
      // Specs "dictate" the tag names to be lower-case
      return new JSONObject((String) result).getString("value").toLowerCase();
    } catch (JSONException e) {
      return null;
    }

  }

  public boolean isDisplayed() {
    return (Boolean) driver.executeAtom(AndroidAtoms.IS_DISPLAYED, null, this);
  }

  /**
   * Where on the page is the top left-hand corner of the rendered element? it's part of
   * RenderedWebElement
   * 
   * @return A point, containing the location of the top left-hand corner of the element
   */
  public Point getLocation() {
    JSONObject result =
        (JSONObject) driver.executeAtom(AndroidAtoms.GET_TOP_LEFT_COORDINATES, null, this);

    try {
      return new Point(result.getInt("x"), result.getInt("y"));
    } catch (JSONException e) {
      throw new SelendroidException(e);
    }
  }

  /**
   * @return a {@link Point} where x is the width, and y is the height.
   */
  public Dimension getSize() {
    JSONObject dimension = (JSONObject) driver.executeAtom(AndroidAtoms.GET_SIZE, null, this);
    try {
      return new Dimension(dimension.getInt("width"), dimension.getInt("height"));
    } catch (JSONException e) {
      throw new SelendroidException(e);
    }
  }

  private Point getCenterCoordinates() {
    if (!isDisplayed()) {
      final String msg = "This WebElement is not visible and may not be clicked.";
      if (android.os.Build.VERSION.SDK_INT <= 18) {
        throw new ElementNotVisibleException(msg);
      } else {
        // just give it a try
        Log.w("SelendroidWebDriver", msg);
      }
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
    String[] result = null;
    try {
      JSONObject response = new JSONObject((String) driver.executeScript(sizeJs, this, null));
      result = response.getString("value").split(",");
    } catch (JSONException e) {
      throw new SelendroidException(e);
    }

    int xSize = (new Double(result[0])).intValue();
    int ySize = (new Double(result[1])).intValue();
    return new Point(topLeft.x + xSize / 2, topLeft.y+ ySize / 2);
  }

  @Override
  public void click() {
    String tagName = getTagName();
    if ((tagName != null && "OPTION".equals(tagName.toUpperCase())) || driver.isInFrame()) {
      driver.resetPageIsLoading();
      driver.executeAtom(AndroidAtoms.CLICK, null, this);
      driver.waitForPageToLoad();
      if (driver.isInFrame()) {
        return;
      }
    }

    Point center = getCenterCoordinates();
    long downTime = SystemClock.uptimeMillis();
    final List<MotionEvent> events = new ArrayList<MotionEvent>();

    MotionEvent downEvent =
        MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, center.x,
            center.y, 0);
    events.add(downEvent);
    MotionEvent upEvent =
        MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, center.x,
            center.y, 0);

    events.add(upEvent);

    driver.resetPageIsLoading();
    driver.getMotionSender().send(events);

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
      driver.executeAtom(AndroidAtoms.SUBMIT, null, this);
      driver.waitForPageToLoad();
    }
  }

  public void clear() {
    driver.executeAtom(AndroidAtoms.CLEAR, null, this);
  }

  public String getAttribute(String name) {
    return (String) driver.executeAtom(AndroidAtoms.GET_ATTRIBUTE_VALUE, null, this, name);
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

  public Coordinates getCoordinates() {
    if (coordinates == null) {
      coordinates =
          new AndroidCoordinates(id, id.equals("0") ? new Point(0, 0) : getCenterCoordinates());
    }
    return coordinates;
  }

  @Override
  public String toString() {
    return "{\"ELEMENT\":\"" + id + "\"}";
  }

  @Override
  public void setText(CharSequence... keysToSend) {
    StringBuilder sb = new StringBuilder();
    for (CharSequence keys : keysToSend) {
      sb.append(keys);
    }
    JSONArray parameter = new JSONArray();
    parameter.put(this);
    parameter.put(getText() + sb.toString());

    driver.executeScript("arguments[0].value = arguments[1];" +
            "var inputEvent = document.createEvent('Event');" +
            "inputEvent.initEvent('input', true, true);" +
            "arguments[0].dispatchEvent(inputEvent);"
            , parameter, ke);
  }

  @Override
  public String id() {
    return id;
  }
}
