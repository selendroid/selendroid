/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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


import io.selendroid.ServerInstrumentation;
import io.selendroid.android.AndroidWait;
import io.selendroid.android.KeySender;
import io.selendroid.android.ViewHierarchyAnalyzer;
import io.selendroid.android.internal.Dimension;
import io.selendroid.android.internal.Point;
import io.selendroid.exceptions.ElementNotVisibleException;
import io.selendroid.exceptions.NoSuchElementAttributeException;
import io.selendroid.exceptions.NoSuchElementException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.TimeoutException;
import io.selendroid.server.model.interactions.AndroidCoordinates;
import io.selendroid.server.model.interactions.Coordinates;
import io.selendroid.server.model.internal.AbstractNativeElementContext;
import io.selendroid.util.Function;
import io.selendroid.util.Preconditions;
import io.selendroid.util.SelendroidLogger;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Rect;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class AndroidNativeElement implements AndroidElement {
  // TODO revisit
  protected static final long DURATION_OF_LONG_PRESS = 750L;// (long)
                                                            // (ViewConfiguration.getLongPressTimeout()
                                                            // * 1.5f);
  private WeakReference<View> viewRef;
  private Collection<AndroidElement> children = new LinkedHashSet<AndroidElement>();
  private AndroidElement parent;
  private ServerInstrumentation instrumentation;
  private final KeySender keys;
  private SearchContext nativeElementSearchScope = null;
  private Coordinates coordinates = null;
  final Object syncObject = new Object();
  private Boolean done = false;
  private KnownElements ke;
  private int hashCode;
  static final long UI_TIMEOUT = 3000L;

  public AndroidNativeElement(View view, ServerInstrumentation instrumentation, KeySender keys,
      KnownElements ke) {
    Preconditions.checkNotNull(view);
    this.viewRef = new WeakReference<View>(view);
    hashCode = view.hashCode() + 31;
    this.instrumentation = instrumentation;
    this.keys = keys;
    this.nativeElementSearchScope = new NativeElementSearchScope(instrumentation, keys, ke);
    this.ke = ke;
  }

  @Override
  public AndroidElement getParent() {
    return parent;
  }

  public boolean isDisplayed() {
    boolean hasWindowFocus = getView().hasWindowFocus();
    boolean enabled = getView().isEnabled();
    int width = getView().getWidth();
    int height = getView().getHeight();
    // In the past we used `getView().isShown()` to identify if the element
    // is displayed. It seems like that it is better to look just at the
    // visibility of the view instead of verifying the ancestors as well.
    boolean isElementDisplayed = (View.VISIBLE == getView().getVisibility())
        && getView().isShown();


    return hasWindowFocus && enabled && isElementDisplayed && (width > 0) && (height > 0);
  }

  private void waitUntilIsDisplayed() {
    AndroidWait wait = instrumentation.getAndroidWait();

    try {
      wait.until(new Function<Void, Boolean>() {
        @Override
        public Boolean apply(Void input) {
          return isDisplayed();
        }
      });
    } catch (TimeoutException exception) {
      throw new ElementNotVisibleException(
          "You may only do passive read with element not displayed");
    }
  }

  protected void scrollIntoScreenIfNeeded() {
    Point leftTopLocation = getLocation();
    final int left = leftTopLocation.x;
    final int top = leftTopLocation.y;

    instrumentation.runOnMainSync(new Runnable() {
      @Override
      public void run() {
        synchronized (syncObject) {
          Rect r = new Rect(left, top, getView().getWidth(), getView().getHeight());

          getView().requestRectangleOnScreen(r);
          done = true;
          syncObject.notify();
        }
      }
    });
    long end = System.currentTimeMillis() + instrumentation.getAndroidWait().getTimeoutInMillis();
    synchronized (syncObject) {
      while (!done && System.currentTimeMillis() < end) {
        try {
          syncObject.wait(AndroidWait.DEFAULT_SLEEP_INTERVAL);
        } catch (InterruptedException e) {
          e.printStackTrace();
          throw new SelendroidException(e);
        }
      }
    }
  }

  @Override
  public void enterText(CharSequence... keysToSend) {
    final View viewview = getView();
    instrumentation.runOnMainSync(new Runnable() {
      @Override
      public void run() {
        viewview.requestFocus();
      }
    });
    click();

    StringBuilder sb = new StringBuilder();
    for (CharSequence keys : keysToSend) {
      sb.append(keys);
    }
    send(sb.toString());
  }

  @Override
  public String getText() {
    if (getView() instanceof TextView) {
      return ((TextView) getView()).getText().toString();
    }
    System.err.println("not supported elment for getting the text: "
        + getView().getClass().getSimpleName());
    return null;
  }

  @Override
  public void click() {
    waitUntilIsDisplayed();
    scrollIntoScreenIfNeeded();
    try {
      // is needed for recalculation of location
      Thread.sleep(300);
    } catch (InterruptedException e) {}
    int[] xy = new int[2];
    getView().getLocationOnScreen(xy);
    final int viewWidth = getView().getWidth();
    final int viewHeight = getView().getHeight();
    final float x = xy[0] + (viewWidth / 2.0f);
    float y = xy[1] + (viewHeight / 2.0f);

    clickOnScreen(x, y);
  }

  private void clickOnScreen(float x, float y) {
    final ServerInstrumentation inst = ServerInstrumentation.getInstance();
    long downTime = SystemClock.uptimeMillis();
    long eventTime = SystemClock.uptimeMillis();
    final MotionEvent event =
        MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
    final MotionEvent event2 =
        MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);

    try {
      inst.sendPointerSync(event);
      inst.sendPointerSync(event2);
      try {
        Thread.sleep(300);
      } catch (InterruptedException ignored) {}
    } catch (SecurityException e) {
      SelendroidLogger.log("error while clicking element", e);
    }
  }

  public Integer getAndroidId() {
    int viewId = getView().getId();
    return (viewId == View.NO_ID) ? null : viewId;
  }

  @Override
  public AndroidElement findElement(By by) throws NoSuchElementException {
    return by.findElement(nativeElementSearchScope);
  }

  @Override
  public List<AndroidElement> findElements(By by) throws NoSuchElementException {
    return by.findElements(nativeElementSearchScope);
  }

  @Override
  public Collection<AndroidElement> getChildren() {
    return children;
  }

  public void setParent(AndroidElement parent) {
    this.parent = parent;
  }

  public void addChild(AndroidElement child) {
    this.children.add(child);
  }

  public String toString() {
    return new StringBuilder().append("id: ").append(getView().getId()).append("view class: ")
        .append(getView().getClass()).append("view content desc: ")
        .append(getView().getContentDescription()).toString();
  }

  protected void send(CharSequence string) {
    keys.send(string);
  }

  public JSONObject toJson() throws JSONException {
    JSONObject object = new JSONObject();
    JSONObject l10n = new JSONObject();
    l10n.put("matches", 0);
    object.put("l10n", l10n);
    
    String label = String.valueOf(getView().getContentDescription());
    object.put("name", label == null ? "" : label);
    String id = getNativeId();
    object.put("id", id.startsWith("id/") ? id.replace("id/", "") : id);
    JSONObject rect = new JSONObject();

    object.put("rect", rect);
    JSONObject origin = new JSONObject();
    Point location = getLocation();
    origin.put("x", location.x);
    origin.put("y", location.y);
    rect.put("origin", origin);

    JSONObject size = new JSONObject();
    Dimension s = getSize();
    size.put("height", s.getHeight());
    size.put("width", s.getWidth());
    rect.put("size", size);

    object.put("ref", ke.getIdOfElement(this));
    object.put("type", getView().getClass().getSimpleName());
    String value = "";
    if (getView() instanceof TextView) {
      value = String.valueOf(((TextView) getView()).getText());
    }
    object.put("value", value);
    object.put("shown", getView().isShown());
    if (getView() instanceof WebView) {
      final WebView webview = (WebView) getView();
      final WebViewSourceClient client = new WebViewSourceClient();
      instrumentation.getCurrentActivity().runOnUiThread(new Runnable() {
        public void run() {
          synchronized (syncObject) {
            webview.getSettings().setJavaScriptEnabled(true);

            webview.setWebChromeClient(client);
            String script = "document.body.parentNode.innerHTML";
            webview.loadUrl("javascript:alert('selendroidSource:'+" + script + ")");
          }
        }
      });
      long end = System.currentTimeMillis() + 10000;
      waitForDone(end, UI_TIMEOUT, "Error while grabbing web view source code.");
      object.put("source", "<html>" + client.result + "</html>");
    }

    return object;
  }

  public class WebViewSourceClient extends WebChromeClient {
    public Object result = null;

    /**
     * Unconventional way of adding a Javascript interface but the main reason why I took this way
     * is that it is working stable compared to the webview.addJavascriptInterface way.
     */
    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult jsResult) {
      if (message != null && message.startsWith("selendroidSource:")) {
        jsResult.confirm();

        synchronized (syncObject) {
          result = message.replaceFirst("selendroidSource:", "");
          done = true;
          syncObject.notify();
        }

        return true;
      } else {
        return super.onJsAlert(view, url, message, jsResult);
      }
    }
  }

  private void waitForDone(long end, long timeout, String error) {
    synchronized (syncObject) {
      while (!done && System.currentTimeMillis() < end) {
        try {
          syncObject.wait(timeout);
        } catch (InterruptedException e) {
          throw new SelendroidException(error, e);
        }
      }
    }
  }

  private String getNativeId() {
    return ViewHierarchyAnalyzer.getNativeId(getView());
  }

  public View getView() {
    if (viewRef.get() == null) {
      throw new IllegalStateException(
          "Trying to access a native element that has already been garbage collected");
    }
    return viewRef.get();
  }

  @Override
  public void clear() {
    final View viewview = getView();
    instrumentation.runOnMainSync(new Runnable() {
      @Override
      public void run() {
        viewview.requestFocus();
        if (viewview instanceof EditText) {
          ((EditText) viewview).setText("");
        }
      }
    });
  }

  @Override
  public void submit() {
    throw new UnsupportedOperationException("Submit is not supported for native elements.");
  }

  @Override
  public boolean isSelected() {
    if (getView() instanceof CompoundButton) {
      return ((CompoundButton) getView()).isChecked();
    }

    throw new UnsupportedOperationException(
        "Is selected is only available for view class CheckBox and RadioButton.");
  }

  @Override
  public Point getLocation() {
    int[] xy = new int[2];
    getView().getLocationOnScreen(xy);
    return new Point(xy[0], xy[1]);
  }

  private class NativeElementSearchScope extends AbstractNativeElementContext {
    public NativeElementSearchScope(ServerInstrumentation instrumentation, KeySender keys,
        KnownElements knownElements) {
      super(instrumentation, keys, knownElements);
    }

    @Override
    protected View getRootView() {
      return getView();
    }

    protected List<View> getTopLevelViews() {
      return Arrays.asList(getView());
    }
  }

  @Override
  public Coordinates getCoordinates() {
    if (coordinates == null) {
      coordinates =
          new AndroidCoordinates(String.valueOf(getView().getId()), getCenterCoordinates());
    }
    return coordinates;
  }

  private Point getCenterCoordinates() {
    int height = getView().getHeight();
    int width = getView().getWidth();
    Point location = getLocation();
    int x = location.x + (height / 2);
    int y = location.y + (width / 2);
    return new Point(x, y);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AndroidNativeElement other = (AndroidNativeElement) obj;
    // Not calling getView() here so inserting into a set with stale elements doesn't suddenly start
    // throwing.
    if (viewRef.get() == null) {
      if (other.viewRef.get() != null) return false;
    } else if (!getView().equals(other.viewRef.get())) return false;
    return true;
  }

  @Override
  public Dimension getSize() {
    return new Dimension(getView().getWidth(), getView().getHeight());
  }

  @Override
  public String getAttribute(String attribute) {
    if (attribute.equalsIgnoreCase("nativeid")) {
      return getNativeId();
    }
    String name = capitalizeFirstLetter(attribute);
    Method method = getDeclaredMethod("get" + name);
    if (method == null) {
      method = getDeclaredMethod("is" + name);
      if (method == null) {
        throw new NoSuchElementAttributeException("The attribute with name '" + name
            + "' was not found.");
      }
    }
    try {
      Object result = method.invoke(getView());
      return String.valueOf(result);
    } catch (IllegalArgumentException e) {
      throw new SelendroidException(e);
    } catch (IllegalAccessException e) {
      throw new SelendroidException(e);
    } catch (InvocationTargetException e) {
      throw new SelendroidException(e);
    }
  }

  private String capitalizeFirstLetter(String name) {
    return name.substring(0, 1).toUpperCase() + name.substring(1);
  }

  private Method getDeclaredMethod(String name) {
    Preconditions.checkNotNull(name);

    Method method = null;
    try {
      method = getView().getClass().getMethod(name);
    } catch (NoSuchMethodException e) {
      // can happen
    }
    return method;
  }

  @Override
  public boolean isEnabled() {
    return getView().isEnabled();
  }

  @Override
  public String getTagName() {
    return getView().getClass().getSimpleName();
  }
}
