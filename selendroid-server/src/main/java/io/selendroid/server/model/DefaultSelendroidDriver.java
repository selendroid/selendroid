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
import io.selendroid.android.AndroidTouchScreen;
import io.selendroid.android.AndroidWait;
import io.selendroid.android.KeySender;
import io.selendroid.android.ViewHierarchyAnalyzer;
import io.selendroid.android.WindowType;
import io.selendroid.android.internal.Dimension;
import io.selendroid.exceptions.NoSuchElementException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.Session;
import io.selendroid.server.model.internal.AbstractNativeElementContext;
import io.selendroid.server.model.internal.AbstractWebElementContext;
import io.selendroid.server.model.internal.execute_native.FindElementByAndroidTag;
import io.selendroid.server.model.internal.execute_native.FindRId;
import io.selendroid.server.model.internal.execute_native.GetL10nKeyTranslation;
import io.selendroid.server.model.internal.execute_native.InvokeMenuAction;
import io.selendroid.server.model.internal.execute_native.NativeExecuteScript;
import io.selendroid.server.model.js.AndroidAtoms;
import io.selendroid.util.Preconditions;
import io.selendroid.util.SelendroidLogger;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.View;
import android.webkit.WebView;


public class DefaultSelendroidDriver implements SelendroidDriver {
  public static final String BROWSER_NAME = "browserName";
  public static final String PLATFORM = "platform";
  public static final String SUPPORTS_JAVASCRIPT = "javascriptEnabled";
  public static final String TAKES_SCREENSHOT = "takesScreenshot";
  public static final String VERSION = "version";
  public static final String SUPPORTS_ALERTS = "handlesAlerts";
  public static final String ROTATABLE = "rotatable";
  public static final String ACCEPT_SSL_CERTS = "acceptSslCerts";
  private boolean done = false;
  private SearchContext nativeSearchScope = null;
  private SearchContext webviewSearchScope = null;
  private ServerInstrumentation serverInstrumentation = null;
  private Session session = null;
  private final Object syncObject = new Object();
  private KeySender keySender = null;
  private TouchScreen touch;
  private SelendroidNativeDriver selendroidNativeDriver = null;
  private SelendroidWebDriver selendroidWebDriver = null;
  private String activeWindowType = null;

  private Map<String, NativeExecuteScript> nativeExecuteScriptMap =
      new HashMap<String, NativeExecuteScript>();


  public DefaultSelendroidDriver(ServerInstrumentation instrumentation) {
    serverInstrumentation = instrumentation;
    keySender = new KeySender(serverInstrumentation);
    touch = new AndroidTouchScreen(serverInstrumentation);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openqa.selenium.android.server.AndroidDriver#findElement(org.openqa.selenium.android.common
   * .model.By)
   */
  @Override
  public AndroidElement findElement(final By by) {
    if (by == null) {
      throw new IllegalArgumentException("By cannot be null.");
    }

    SearchContext context = getSearchContext();
    AndroidElement found = by.findElement(context);
    long timeout = getTimeout();

    while (found == null && (System.currentTimeMillis() < timeout)) {
      sleepQuietly(AndroidWait.DEFAULT_SLEEP_INTERVAL);
      found = by.findElement(context);
    }
    return found;
  }

  private long getTimeout() {
    return System.currentTimeMillis() + serverInstrumentation.getAndroidWait().getTimeoutInMillis();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openqa.selenium.android.server.AndroidDriver#findElements(org.openqa.selenium.android.common
   * .model.By)
   */
  @Override
  public List<AndroidElement> findElements(By by) {
    if (by == null) {
      throw new IllegalArgumentException("By cannot be null.");
    }
    long timeout = getTimeout();
    SearchContext context = getSearchContext();

    List<AndroidElement> found = by.findElements(context);
    while (found.isEmpty() && (System.currentTimeMillis() < timeout)) {
      sleepQuietly(AndroidWait.DEFAULT_SLEEP_INTERVAL);
      found = by.findElements(context);
    }
    return found;
  }

  private SearchContext getSearchContext() {
    if (isNativeWindowMode()) {
      Preconditions.checkNotNull(nativeSearchScope);
      return nativeSearchScope;
    } else {
      Preconditions.checkNotNull(webviewSearchScope);
      return webviewSearchScope;
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#getSession()
   */
  @Override
  public Session getSession() {
    return session;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#getSessionCapabilities(java.lang.String)
   */
  @Override
  public JSONObject getSessionCapabilities(String sessionId) {
    SelendroidLogger.log("session: " + sessionId);

    JSONObject copy;
    try {
      if (session.getCapabilities().names() != null) {
        copy =
            new JSONObject(session.getCapabilities(), session.getCapabilities().names().join(",")
                .split(","));
      } else {
        copy = new JSONObject();
      }
      copy.put(TAKES_SCREENSHOT, true);
      copy.put(BROWSER_NAME, "selendroid");
      copy.put(ROTATABLE, false);
      copy.put(PLATFORM, "android");
      copy.put(SUPPORTS_ALERTS, false);
      copy.put(SUPPORTS_JAVASCRIPT, true);
      copy.put("version", serverInstrumentation.getServerVersion());
      copy.put(ACCEPT_SSL_CERTS, true);
      SelendroidLogger.log("capabilities: " + copy);
      return copy;
    } catch (JSONException e) {
      throw new SelendroidException(e);
    }
  }

  public static void sleepQuietly(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException cause) {
      Thread.currentThread().interrupt();
      throw new SelendroidException(cause);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#stopSession()
   */
  @Override
  public void stopSession() {
    serverInstrumentation.finishAllActivities();
    this.activeWindowType = WindowType.NATIVE_APP.name();
    this.session = null;
    nativeSearchScope = null;
    selendroidNativeDriver = null;
    selendroidWebDriver = null;
    webviewSearchScope = null;
  }

  /*
   * 
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#takeScreenshot()
   */
  @Override
  @SuppressWarnings("deprecation")
  public byte[] takeScreenshot() {
    ViewHierarchyAnalyzer viewAnalyzer = ViewHierarchyAnalyzer.getDefaultInstance();

    // TODO ddary review later, but with getRecentDecorView() it seems to work better
    // long drawingTime = 0;
    // View container = null;
    // for (View view : viewAnalyzer.getTopLevelViews()) {
    // if (view != null && view.isShown() && view.hasWindowFocus()
    // && view.getDrawingTime() > drawingTime) {
    // container = view;
    // drawingTime = view.getDrawingTime();
    // }
    // }
    // final View mainView = container;
    final View mainView = viewAnalyzer.getRecentDecorView();
    if (mainView == null) {
      throw new SelendroidException("No open windows.");
    }
    done = false;
    long end =
        System.currentTimeMillis() + serverInstrumentation.getAndroidWait().getTimeoutInMillis();
    final byte[][] rawPng = new byte[1][1];
    ServerInstrumentation.getInstance().getCurrentActivity().runOnUiThread(new Runnable() {
      public void run() {
        synchronized (syncObject) {
          Display display =
              serverInstrumentation.getCurrentActivity().getWindowManager().getDefaultDisplay();
          Point size = new Point();
          try {
            display.getSize(size);
          } catch (NoSuchMethodError ignore) { // Older than api level 13
            size.x = display.getWidth();
            size.y = display.getHeight();
          }

          // Get root view
          View view = mainView.getRootView();

          // Create the bitmap to use to draw the screenshot
          final Bitmap bitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
          final Canvas canvas = new Canvas(bitmap);

          // Get current theme to know which background to use
          final Activity activity = serverInstrumentation.getCurrentActivity();
          final Theme theme = activity.getTheme();
          final TypedArray ta =
              theme.obtainStyledAttributes(new int[] {android.R.attr.windowBackground});
          final int res = ta.getResourceId(0, 0);
          final Drawable background = activity.getResources().getDrawable(res);

          // Draw background
          background.draw(canvas);

          // Draw views
          view.draw(canvas);

          ByteArrayOutputStream stream = new ByteArrayOutputStream();
          if (!bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream)) {
            throw new RuntimeException("Error while compressing screenshot image.");
          }
          try {
            stream.flush();
            stream.close();
          } catch (IOException e) {
            throw new RuntimeException("I/O Error while capturing screenshot: " + e.getMessage());
          } finally {
            Closeable closeable = (Closeable) stream;
            try {
              if (closeable != null) {
                closeable.close();
              }
            } catch (IOException ioe) {
              // ignore
            }
          }
          rawPng[0] = stream.toByteArray();
          mainView.destroyDrawingCache();
          done = true;
          syncObject.notify();
        }
      }
    });

    waitForDone(end, serverInstrumentation.getAndroidWait().getTimeoutInMillis(),
        "Failed to take screenshot.");
    return rawPng[0];
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

  public void switchDriverMode(String type) {
    Preconditions.checkNotNull(type);
    if (type.equals(activeWindowType)) {
      return;
    } else {
      this.activeWindowType = type;
    }
    if (WindowType.NATIVE_APP.name().equals(type)) {
      this.webviewSearchScope = null;
      this.selendroidWebDriver = null;
    } else {
      initSelendroidWebDriver(type);
    }
    session.getKnownElements().clear();
  }

  private void initSelendroidWebDriver(String type) {
    this.selendroidWebDriver = new SelendroidWebDriver(serverInstrumentation, type);
    webviewSearchScope =
        new WebviewSearchScope(session.getKnownElements(), selendroidWebDriver.getWebview(),
            selendroidWebDriver);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openqa.selenium.android.server.AndroidDriver#initializeSessionForCapabilities(com.google
   * .gson.JsonObject)
   */
  @Override
  public String initializeSession(JSONObject desiredCapabilities) {
    if (this.session != null) {
      session.getKnownElements().clear();
      return session.getSessionId();
    }
    activeWindowType = WindowType.NATIVE_APP.name();
    this.session = new Session(desiredCapabilities, UUID.randomUUID().toString());
    nativeSearchScope =
        new NativeSearchScope(serverInstrumentation, getSession().getKnownElements());

    selendroidNativeDriver =
        new SelendroidNativeDriver(serverInstrumentation, (NativeSearchScope) nativeSearchScope);
    serverInstrumentation.startMainActivity();
    SelendroidLogger.log("new s: " + session.getSessionId());

    nativeExecuteScriptMap.put("invokeMenuActionSync", new InvokeMenuAction(session,
        serverInstrumentation));
    nativeExecuteScriptMap.put("findRId", new FindRId(serverInstrumentation));
    nativeExecuteScriptMap.put("getL10nKeyTranslation", new GetL10nKeyTranslation(
        serverInstrumentation));
    nativeExecuteScriptMap.put("findElementByAndroidTag",
        new FindElementByAndroidTag(session.getKnownElements(), serverInstrumentation));

    return session.getSessionId();
  }

  @Override
  public Keyboard getKeyboard() {
    return keySender.getKeyboard();
  }

  @Override
  public TouchScreen getTouch() {
    return touch;
  }

  public class WebviewSearchScope extends AbstractWebElementContext {
    public WebviewSearchScope(KnownElements knownElements, WebView webview,
        SelendroidWebDriver driver) {
      super(knownElements, webview, driver);
    }

    @Override
    protected AndroidElement lookupElement(String strategy, String locator) {
      AndroidElement element = null;


      Object result = driver.executeAtom(AndroidAtoms.FIND_ELEMENT, strategy, locator);
      if (result == null) {
        return null;
      }
      element = replyElement((JSONObject) result);
      return element;
    }

    @Override
    protected List<AndroidElement> lookupElements(String strategy, String locator) {
      List<AndroidElement> elements = null;

      Object result = driver.executeAtom(AndroidAtoms.FIND_ELEMENTS, strategy, locator);
      if (result == null) {
        return new ArrayList<AndroidElement>();
      }
      elements = replyElements((JSONArray) result);
      if (elements == null || elements.isEmpty()) {
        throw new NoSuchElementException("The element was not found.");
      }
      return elements;
    }
  }

  public class NativeSearchScope extends AbstractNativeElementContext {
    public NativeSearchScope(ServerInstrumentation instrumentation, KnownElements knownElements) {
      super(instrumentation, knownElements);
    }

    @Override
    protected View getRootView() {
      return viewAnalyzer.getRecentDecorView();
    }

    @Override
    protected List<View> getTopLevelViews() {
      List<View> views = new ArrayList<View>();
      views.addAll(viewAnalyzer.getTopLevelViews());
      if (instrumentation.getCurrentActivity() != null
          && instrumentation.getCurrentActivity().getCurrentFocus() != null) {
        views.add(instrumentation.getCurrentActivity().getCurrentFocus());
      }
      // sort them to have most recently drawn view show up first
      Collections.sort(views, new Comparator<View>() {
        @Override
        public int compare(View view, View view1) {
          return view.getDrawingTime() < view1.getDrawingTime()
              ? 1
              : (view.getDrawingTime() == view1.getDrawingTime() ? 0 : -1);
        }
      });

      return views;
    }
  }

  @Override
  public String getCurrentUrl() {
    if (isNativeWindowMode()) {
      return selendroidNativeDriver.getCurrentUrl();
    } else {
      return selendroidWebDriver.getCurrentUrl();
    }
  }

  @Override
  public Object getWindowSource() {
    Object source = null;
    try {
      // if (isNativeWindowMode()) {
      source = selendroidNativeDriver.getWindowSource();
      // } else {
      // source = selendroidWebDriver.getWindowSource();
      // }
    } catch (JSONException e) {
      throw new SelendroidException(e);
    }
    return source;
  }

  @Override
  public String getTitle() {
    if (isNativeWindowMode()) {
      return selendroidNativeDriver.getTitle();
    } else {
      return selendroidWebDriver.getTitle();
    }
  }

  @Override
  public void get(String url) {
    if (isNativeWindowMode()) {
      selendroidNativeDriver.get(url);
    } else {
      selendroidWebDriver.get(url);
    }
  }

  public boolean isNativeWindowMode() {
    if (WindowType.NATIVE_APP.name().equals(activeWindowType)) {
      return true;
    }
    return false;
  }

  @Override
  public Object executeScript(String script, JSONArray args) {
    if (isNativeWindowMode()) {
      if (nativeExecuteScriptMap.containsKey(script)) {
        return nativeExecuteScriptMap.get(script).executeScript(args);
      }
      throw new UnsupportedOperationException(
          "Executing arbitrary script is only available in web views.");
    }
    return selendroidWebDriver.executeScript(script, args);
  }

  @Override
  public Object executeScript(String script, Object... args) {
    JSONArray array = new JSONArray();
    for (int i = 0; i < args.length; i++) {
      array.put(args[i]);
    }
    return executeScript(script, array);
  }

  @Override
  public String getWindowHandle() {
    return activeWindowType;
  }

  @Override
  public Dimension getWindowSize() {
    if (isNativeWindowMode()) {
      return selendroidNativeDriver.getWindowSize();
    } else {
      return new Dimension(selendroidWebDriver.getWebview().getWidth(), selendroidWebDriver
          .getWebview().getHeight());
    }
  }

  @Override
  public Set<String> getWindowHandles() {
    Set<String> windowHandles = new HashSet<String>();
    windowHandles.add(WindowType.NATIVE_APP.name());
    List<WebView> webview = ViewHierarchyAnalyzer.getDefaultInstance().findWebViews();
    if (webview != null) {
      for (int i = 0; i < webview.size(); i++) {
        windowHandles.add(WindowType.WEBVIEW.name() + "_" + i);
      }
    }
    return windowHandles;
  }

  @Override
  public void addCookie(String url, Cookie cookie) {

    if (selendroidWebDriver != null) selendroidWebDriver.setCookies(url, cookie);

  }

  @Override
  public Set<Cookie> getCookies(String url) {


    Set<Cookie> coo = new HashSet<Cookie>();
    if (selendroidWebDriver != null) {
      coo.addAll(selendroidWebDriver.getCookies(url));
    }
    return coo;
  }

  @Override
  public void deleteCookie(String url) {

    if (selendroidWebDriver != null) {
      selendroidWebDriver.removeAllCookie(url);
    }
  }

  @Override
  public void deleteNamedCookie(String url, String name) {

    if (selendroidWebDriver != null) {
      selendroidWebDriver.remove(url, name);
    }
  }

}
