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

import android.app.Activity;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.webkit.WebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.android.internal.Dimension;
import org.openqa.selendroid.android.internal.Point;
import org.openqa.selendroid.server.exceptions.ElementNotVisibleException;
import org.openqa.selendroid.server.exceptions.NoSuchElementException;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.interactions.AndroidCoordinates;
import org.openqa.selendroid.server.model.interactions.Coordinates;
import org.openqa.selendroid.server.model.internal.AbstractWebElementContext;
import org.openqa.selendroid.server.model.js.AndroidAtoms;
import org.openqa.selendroid.server.webview.EventSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AndroidWebElement implements AndroidElement {
  private final String id;
  private WebView webview;
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
          (JSONObject) driver.executeAtom(AndroidAtoms.FIND_ELEMENT, strategy, locator,
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
          (JSONArray) driver.executeAtom(AndroidAtoms.FIND_ELEMENTS, strategy, locator,
              AndroidWebElement.this);

      List<AndroidElement> elements = replyElements(result);
      if (elements == null || elements.isEmpty()) {
        throw new NoSuchElementException("no elements were found.");
      }
      return elements;
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
    String originalText = (String) driver.executeScript("arguments[0].focus();" +
        "arguments[0].value=arguments[0].value;return arguments[0].value", this);

    EventSender.sendKeys(webview, value);

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
    return (Boolean) driver.executeAtom(AndroidAtoms.IS_ENABLED, this);
  }

  public boolean isSelected() {
    return (Boolean) driver.executeAtom(AndroidAtoms.IS_SELECTED, this);
  }

  @Override
  public String getText() {
    return (String) driver.executeAtom(AndroidAtoms.GET_TEXT, this);
  }

  public String getTagName() {
    return (String) driver.executeScript("return arguments[0].tagName", this);
  }

  public boolean isDisplayed() {
    return (Boolean) driver.executeAtom(AndroidAtoms.IS_DISPLAYED, this);
  }

  /**
   * Where on the page is the top left-hand corner of the rendered element? it's part of
   * RenderedWebElement
   * 
   * @return A point, containing the location of the top left-hand corner of the element
   */
  public Point getLocation() {
    JSONObject result =
        (JSONObject) driver.executeAtom(AndroidAtoms.GET_TOP_LEFT_COORDINATES, this);

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
    JSONObject dimension = (JSONObject) driver.executeAtom(AndroidAtoms.GET_SIZE, this);
    try {
      return new Dimension(dimension.getInt("width"), dimension.getInt("height"));
    } catch (JSONException e) {
      throw new SelendroidException(e);
    }
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
    String[] result = null;
    try {
      JSONObject response = new JSONObject((String) driver.executeScript(sizeJs, this));
      result = response.getString("value").split(",");
    } catch (JSONException e) {
      throw new SelendroidException(e);
    }

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
    return (String) driver.executeAtom(AndroidAtoms.GET_ATTRIBUTE_VALUE, this, name);
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
}
