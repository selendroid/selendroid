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

import android.app.Activity;
import android.view.ViewParent;
import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.android.AndroidWait;
import io.selendroid.server.android.KeySender;
import io.selendroid.server.android.ViewHierarchyAnalyzer;
import io.selendroid.server.android.internal.Dimension;
import io.selendroid.server.android.internal.Point;
import io.selendroid.server.common.exceptions.ElementNotVisibleException;
import io.selendroid.server.common.exceptions.NoSuchElementAttributeException;
import io.selendroid.server.common.exceptions.NoSuchElementException;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.common.exceptions.TimeoutException;
import io.selendroid.server.model.interactions.AndroidCoordinates;
import io.selendroid.server.model.interactions.Coordinates;
import io.selendroid.server.model.internal.AbstractNativeElementContext;
import io.selendroid.server.util.Function;
import io.selendroid.server.util.Preconditions;
import io.selendroid.server.util.SelendroidLogger;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
  private final String id;

  public AndroidNativeElement(View view, ServerInstrumentation instrumentation, KeySender keys,
                              KnownElements ke) {
    Preconditions.checkNotNull(view);
    this.viewRef = new WeakReference<View>(view);
    hashCode = view.hashCode() + 31;
    this.instrumentation = instrumentation;
    this.keys = keys;
    this.nativeElementSearchScope = new NativeElementSearchScope(instrumentation, keys, ke);
    this.ke = ke;

    Random random = new Random();
    this.id = new UUID(random.nextLong(), random.nextLong()).toString();
  }

  @Override
  public AndroidElement getParent() {
    return parent;
  }

  public boolean isDisplayed() {
    View view = getView();
    boolean hasWindowFocus = view.hasWindowFocus();
    int width = view.getWidth();
    int height = view.getHeight();
    int visibility = view.getVisibility();
    boolean isVisible = (View.VISIBLE == visibility);

    // Check visibility of the view and its parents as well.
    // This is more reliable when transitions between activities are in progress.
    boolean isShown = view.isShown();

    boolean isDisplayed =
        hasWindowFocus && isVisible && isShown && (width > 0) && (height > 0);

    if (!isDisplayed) {
      Activity activity = instrumentation.getCurrentActivity();
      View focusedView = activity.getCurrentFocus();
      String displayCheckFailureMessage =
          String.format(
              "Display check failed\n" +
                  "for view: %s\n" +
                  "isVisible: %b\nvisibility: %d\nisShown: %b\nhasWindowFocus: %b\n" +
                  "width: %d\nheight: %d\ncurrent activity: %s\nfocused view: %s",
              view, isVisible, visibility, isShown, hasWindowFocus,
              width, height, activity, focusedView);
      SelendroidLogger.debug(displayCheckFailureMessage);
      if (!isShown) {
        logIsShownCheckFailure(view);
      }
      // Check the view belongs to the same view hierarchy as the view with current window focus.
      // If true, this usually means a system alert dialog is rendered on top of the view
      // (typically this is an app crash dialog).
      if (!hasWindowFocus) {
        if (activity != null && focusedView != null) {
          if (view.getRootView() == focusedView.getRootView()) {
             SelendroidLogger.debug("hasWindowFocus() check failed. " +
                    "This usually means the view is covered by a system dialog.");
          }
        }
      }
    }

    return isDisplayed;
  }

  /**
   * If view.isShown() == false, logs why exactly this evaluates to false.
   * Copied from Android's implementation of View.isShown().
   */
  private void logIsShownCheckFailure(View view) {
    try {
      SelendroidLogger.debug("Display check failed because View.isShown() == false");
      View current = view;
      do {
        if ((current.getVisibility()) != View.VISIBLE) {
          SelendroidLogger.debug(String.format(
              "isShown: View %s is not visible because its ancestor %s has visibility %d",
              view, current, current.getVisibility()));
          break;
        }
        ViewParent parent = current.getParent();
        if (parent == null) {
          SelendroidLogger.debug(String.format(
              "isShown: View %s is not visible because its ancestor %s has no parent " +
                  "(it is not attached to view root): ",
              view, current));
          break;
        }
        if (!(parent instanceof View)) {
          // The only case where View.isShown() returns true:
          // The view needs to have an ancestor that is not a View and all ancestors on the way up have to
          // be visible.
          break;
        }
        current = (View) parent;
      } while (current != null);
      SelendroidLogger.debug(String.format("View %s is not visible", view));
    } catch (Exception e) {
      // Don't let an exception in debug printing crash the caller
      SelendroidLogger.error("isShown() debug printing failed", e);
    }
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
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  @Override
  public void enterText(CharSequence... keysToSend) {
    requestFocus();

    StringBuilder sb = new StringBuilder();
    for (CharSequence keys : keysToSend) {
      sb.append(keys);
    }
    send(sb);
  }

  private void requestFocus() {
    final View viewview = getView();
    instrumentation.runOnMainSync(new Runnable() {
      @Override
      public void run() {
        viewview.requestFocus();
      }
    });
    click();
  }

  @Override
  public String getText() {
    if (getView() instanceof TextView) {
      return ((TextView) getView()).getText().toString();
    }
    SelendroidLogger.warning("Element does not support getText(): "
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
    SelendroidLogger.debug(String.format("Clicking at position [%f, %f]", x, y));
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
      SelendroidLogger.error("error while clicking element", e);
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
    CharSequence cd = getView().getContentDescription();
    if (cd != null && cd.length() > 0) {
      String label = String.valueOf(cd);
      object.put("name", label);
    } else {
      object.put("name", "");
    }
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
      TextView textView = (TextView) getView();
      value = String.valueOf(textView.getText());

      CharSequence error = textView.getError();
      if(error != null && error.length() > 0){
        SelendroidLogger.info("error: " + error);
        object.put("error", error);
      }
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
    final Object[] result = new Object[1];
    final Exception[] exception = new Exception[1];
    final Method m = method;
    instrumentation.runOnMainSync(new Runnable() {
      @Override
      public void run() {
        try {
          result[0] = m.invoke(getView());
        } catch(Exception e) {
          exception[0] = e;
        }
      }
    });
    if (exception.length == 1 && exception[0] != null) {
      throw new SelendroidException(exception[0]);
    }
    return String.valueOf(result[0]);
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

  @Override
  public void setText(CharSequence... keysToSend) {
    requestFocus();
    final View viewview = getView();
    StringBuilder sb = new StringBuilder();
    for (CharSequence keys : keysToSend) {
      sb.append(keys);
    }
    final String text = getText() + sb;
    instrumentation.runOnMainSync(new Runnable() {
      @Override
      public void run() {
        ((EditText) viewview).setText(text);
      }
    });
  }

  @Override
  public String id() {
    return this.id;
  }
}
