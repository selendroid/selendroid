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

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.*;
import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.android.*;
import io.selendroid.server.android.internal.DomWindow;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.common.exceptions.StaleElementReferenceException;
import io.selendroid.server.common.exceptions.TimeoutException;
import io.selendroid.server.model.internal.WebViewHandleMapper;
import io.selendroid.server.model.js.AndroidAtoms;
import io.selendroid.server.util.SelendroidLogger;
import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.engine.SystemWebChromeClient;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SelendroidWebDriver {
  private static final String ELEMENT_KEY = "ELEMENT";
  private static final long FOCUS_TIMEOUT = 1000L;
  private static final long POLLING_INTERVAL = 50L;
  private static final long START_LOADING_TIMEOUT = 700L;
  static final long UI_TIMEOUT = 3000L;
  private volatile boolean pageDoneLoading;
  private volatile boolean pageStartedLoading;
  private volatile String result;
  private volatile WebView webview = null;
  private static final String WINDOW_KEY = "WINDOW";
  private volatile boolean editAreaHasFocus;
  private final Object syncObject = new Object();
  private boolean done = false;
  private ServerInstrumentation serverInstrumentation = null;
  private SessionCookieManager sm = new SessionCookieManager();
  private WebChromeClient chromeClient = null;
  private DomWindow currentWindowOrFrame;
  private Queue<String> currentAlertMessage = new LinkedList<String>();
  private TouchScreen touch;
  private KeySender keySender;
  private MotionSender motionSender;
  private long scriptTimeout = 60000L;
  private long asyncScriptTimeout = 0L;
  private long pageLoadTimeout = 30000L;
  private final String contextHandle;

  public SelendroidWebDriver(ServerInstrumentation serverInstrumentation, String handle) {
    this.contextHandle = WebViewHandleMapper.normalizeHandle(handle);
    this.serverInstrumentation = serverInstrumentation;
    init(handle);
    keySender = new WebViewKeySender(serverInstrumentation, webview);
  }

  private static String escapeAndQuote(final String toWrap) {
    StringBuilder toReturn = new StringBuilder("\"");
    for (int i = 0; i < toWrap.length(); i++) {
      char c = toWrap.charAt(i);
      if (c == '\"') {
        toReturn.append("\\\"");
      } else if (c == '\\') {
        toReturn.append("\\\\");
      } else {
        toReturn.append(c);
      }
    }
    toReturn.append("\"");
    return toReturn.toString();
  }

  @SuppressWarnings("unchecked")
  private String convertToJsArgs(JSONArray args, KnownElements ke) throws JSONException {
    StringBuilder toReturn = new StringBuilder();

    int length = args.length();
    for (int i = 0; i < length; i++) {
      toReturn.append((i > 0) ? "," : "");
      toReturn.append(convertToJsArgs(args.get(i), ke));
    }
    SelendroidLogger.info("convertToJsArgs: " + toReturn.toString());
    return toReturn.toString();
  }

  private String convertToJsArgs(Object obj, KnownElements ke) throws JSONException {
    StringBuilder toReturn = new StringBuilder();
    if (obj == null || obj.equals(null)) {
      return "null";
    }
    if (obj instanceof JSONArray) {
      return convertToJsArgs((JSONArray) obj, ke);
    }
    if (obj instanceof List<?>) {
      toReturn.append("[");
      List<Object> aList = (List<Object>) obj;
      for (int j = 0; j < aList.size(); j++) {
        String comma = ((j == 0) ? "" : ",");
        toReturn.append(comma + convertToJsArgs(aList.get(j), ke));
      }
      toReturn.append("]");
    } else if (obj instanceof Map<?, ?>) {
      Map<Object, Object> aMap = (Map<Object, Object>) obj;
      String toAdd = "{";
      for (Object key : aMap.keySet()) {
        toAdd += key + ":" + convertToJsArgs(aMap.get(key), ke) + ",";
      }
      toReturn.append(toAdd.substring(0, toAdd.length() - 1) + "}");
    } else if (obj instanceof AndroidWebElement) {
      // A WebElement is represented in JavaScript by an Object as
      // follow: {"ELEMENT":"id"} where "id" refers to the id
      // of the HTML element in the javascript cache that can
      // be accessed throught bot.inject.cache.getCache_()
      toReturn.append("{\"" + ELEMENT_KEY + "\":\"" + ((AndroidWebElement) obj).getId() + "\"}");
    } else if (obj instanceof DomWindow) {
      // A DomWindow is represented in JavaScript by an Object as
      // follow {"WINDOW":"id"} where "id" refers to the id of the
      // DOM window in the cache.
      toReturn.append("{\"" + WINDOW_KEY + "\":\"" + ((DomWindow) obj).getKey() + "\"}");
    } else if (obj instanceof Number || obj instanceof Boolean) {
      toReturn.append(String.valueOf(obj));
    } else if (obj instanceof String) {
      toReturn.append(escapeAndQuote((String) obj));
    } else if (obj instanceof JSONObject) {
      if (((JSONObject) obj).has(ELEMENT_KEY)) {
        try {
          AndroidElement ae = ke.get(((JSONObject) obj).getString(ELEMENT_KEY));
          toReturn.append(ae.toString());
        } catch (JSONException e) {
          SelendroidLogger.info("exception getting the element id: " + e.toString());
        }
      } else {
        // send across the object since it's not a webelement
        toReturn.append(obj.toString());
      }
    } else {
      SelendroidLogger
          .info("failed to figure out what this is to convert to execute script:" + obj);
    }
    SelendroidLogger.info("convertToJsArgs: " + toReturn.toString());
    return toReturn.toString();
  }

  public String getContextHandle() {
    return contextHandle;
  }

  public Object executeAtom(AndroidAtoms atom, KnownElements ke, Object... args) {
    JSONArray array = new JSONArray();
    for (int i = 0; i < args.length; i++) {
      array.put(args[i]);
    }
    try {
      return executeAtom(atom, array, ke);
    } catch (JSONException je) {
      SelendroidLogger.error("Failed to execute atom", je);
      throw new RuntimeException(je);
    }
  }

  public Object executeAtom(AndroidAtoms atom, JSONArray args, KnownElements ke)
      throws JSONException {
    final String myScript = atom.getValue();
    String scriptInWindow =
        "(function(){ " + " var win; try{win=" + getWindowString() + "}catch(e){win=window;}"
            + "with(win){return (" + myScript + ")(" + convertToJsArgs(args, ke) + ")}})()";
    String jsResult =
        executeJavascriptInWebView("alert('selendroid<' + document.charset + '>:'+"
            + scriptInWindow + ")");


    SelendroidLogger.info("jsResult: " + jsResult);
    if (jsResult == null || "undefined".equals(jsResult)) {
      return null;
    }

    try {
      JSONObject json = new JSONObject(jsResult);
      if (0 != json.optInt("status")) {
        Object value = json.get("value");
        if ((value instanceof String && value.equals("Element does not exist in cache")) ||
            ( value instanceof JSONObject &&
                (((JSONObject) value).getString("message").equals("Element does not exist in cache") ||
                 ((JSONObject) value).getString("message").equals("Element is no longer attached to the DOM")))) {
          throw new StaleElementReferenceException(json.optString("value"));
        }
        throw new SelendroidException(json.optString("value"));
      }
      if (json.isNull("value")) {
        return null;
      } else {
        return json.get("value");
      }
    } catch (JSONException e) {
      throw new SelendroidException(e);
    }
  }

  private String executeJavascriptInWebView(final String script) {
    result = null;
    ServerInstrumentation.getInstance().getCurrentActivity().runOnUiThread(new Runnable() {
      public void run() {
        if (webview.getUrl() == null) {
          return;
        }
        // needed in case the AUT re-set the WebChromeClient, which overrides selendroid's
        // which handles the onAlert, which is used to communicate response messages.
        // If this happens to cause undesired behavior for an end user, they should
        // switch back to NATIVE_APP and then to the webview again, this will allow
        // selendroid to wrap the 'new' chromeClient set by the AUT.
        webview.setWebChromeClient(chromeClient);
        webview.loadUrl("javascript:" + script);
      }
    });
    long timeout = System.currentTimeMillis() + scriptTimeout;
    synchronized (syncObject) {
      while (result == null && (System.currentTimeMillis() < timeout)) {
        try {
          syncObject.wait(2000);
        } catch (InterruptedException e) {
          throw new SelendroidException(e);
        }
      }

      return result;
    }
  }

  public Object executeScript(String script) {
    return injectJavascript(script, new JSONArray(), null);
  }

  public Object executeScript(String script, JSONArray args, KnownElements ke) {
    return injectJavascript(script, args, ke);
  }

  public Object executeScript(String script, Object args, KnownElements ke) {
    return injectJavascript(script, args, ke);
  }

  public String getCurrentUrl() {
    if (webview == null) {
      throw new SelendroidException("No open web view.");
    }
    long end = System.currentTimeMillis() + UI_TIMEOUT;
    final String[] url = new String[1];
    done = false;
    Runnable r = new Runnable() {
      public void run() {
        url[0] = webview.getUrl();
        synchronized (this) {
          this.notify();
        }
      }
    };
    runSynchronously(r, UI_TIMEOUT);
    return url[0];
  }


  public void get(final String url) {
    resetPageIsLoading();
    serverInstrumentation.getCurrentActivity().runOnUiThread(new Runnable() {
      public void run() {
        webview.loadUrl(url);
      }
    });
    waitForPageToLoad();
  }

  public String getWindowSource() throws JSONException {
    JSONObject source =
        new JSONObject(
            (String) executeScript("return (new XMLSerializer()).serializeToString(document.documentElement);"));
    return source.getString("value");
  }

  protected void init(String handle) {
    SelendroidLogger.info("Selendroid webdriver init");

    webview = WebViewHandleMapper.getWebViewByHandle(handle);

    if (webview == null) {
      throw new SelendroidException("No webview found on current activity.");
    }
    configureWebView(webview);
    currentWindowOrFrame = new DomWindow("");
    motionSender = new WebViewMotionSender(webview, serverInstrumentation);
    touch = new AndroidTouchScreen(serverInstrumentation, motionSender);
  }

  public TouchScreen getTouch() {
    return touch;
  }

  KeySender getKeySender() {
    return keySender;
  }

  MotionSender getMotionSender() {
    return motionSender;
  }

  private void configureWebView(final WebView view) {
    ServerInstrumentation.getInstance().getCurrentActivity().runOnUiThread(new Runnable() {

      @Override
      public void run() {
        try {

          view.clearCache(true);
          view.clearFormData();
          view.clearHistory();
          view.setFocusable(true);
          view.setFocusableInTouchMode(true);
          view.setNetworkAvailable(true);

          try {
            chromeClient = encapulateWebChromeClientForView(view);
            view.setWebChromeClient(chromeClient);
            view.setWebViewClient(encapulateWebViewClientForView(view));
          } catch (ClassCastException cce) {
            // the view can potentially have the class name be an empty string
            // if the app declared its WebView inside another class
            if (cce.getMessage().contains("cannot be cast to org.apache.cordova.CordovaChromeClient")) {
              chromeClient = encapsulatedCordovaChromeClientForView((CordovaWebView) view);
              view.setWebChromeClient(chromeClient);
              try {
                view.setWebViewClient(encapulateCordovaWebViewClientForView((CordovaWebView) view));
              } catch (Exception e) {
                // ignore... it's not *as* important to override the WebViewClient
              }
            } else {
              throw cce;
            }
          }

          WebSettings settings = view.getSettings();
          settings.setJavaScriptCanOpenWindowsAutomatically(true);
          settings.setSupportMultipleWindows(true);
          settings.setBuiltInZoomControls(true);
          settings.setJavaScriptEnabled(true);
          settings.setAppCacheEnabled(true);
          settings.setAppCacheMaxSize(10 * 1024 * 1024);
          settings.setAppCachePath("");
          settings.setDatabaseEnabled(true);
          settings.setDomStorageEnabled(true);
          settings.setGeolocationEnabled(true);
          settings.setSaveFormData(false);
          settings.setSavePassword(false);
          settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
          // Flash settings
          settings.setPluginState(WebSettings.PluginState.ON);

          // Geo location settings
          settings.setGeolocationEnabled(true);
          settings.setGeolocationDatabasePath("/data/data/selendroid");
        } catch (Exception e) {
          SelendroidLogger.error("Error configuring web view", e);
        }
      }
    });
  }

  private WebChromeClient encapulateWebChromeClientForView(WebView view) {
    if (view.getClass().getSimpleName().equalsIgnoreCase("CordovaWebView")) {
      return encapsulatedCordovaChromeClientForView((CordovaWebView)view);
    } else if (view.getClass().getSimpleName().equalsIgnoreCase("SystemWebView")) {
      return new ExtendedSystemWebChromeClient(new SystemWebViewEngine((SystemWebView)view));
    } else {
      try {
        Object webChromeClient = getWebClientViaReflection(view, "mWebChromeClient");
        if (webChromeClient != null) {
          return new WrappedChromeClient((WebChromeClient) webChromeClient);
        }
      } catch(Exception e) {}
      return new SelendroidWebChromeClient();
    }
  }

  private CordovaChromeClient encapsulatedCordovaChromeClientForView(CordovaWebView view) {
    try {
      if (reflectionGet(view, "chromeClient") != null) {
        return new WrappingCordovaChromeClient(null, view);
      }
    } catch (NoSuchFieldException nsfe) {}
    catch (IllegalAccessException iae) {}
    return new ExtendedCordovaChromeClient(view);
  }

  private WebViewClient encapulateWebViewClientForView(WebView view) {
    if (view.getClass().getSimpleName().equalsIgnoreCase("CordovaWebView")) {
      try {
        return encapulateCordovaWebViewClientForView((CordovaWebView) view);
      } catch (NoSuchFieldException nsfe) {}
      catch (IllegalAccessException iae) {}
      return new SelendroidWebClient();
    } else if (view.getClass().getSimpleName().equalsIgnoreCase("SystemWebView")) {
      return new SelendroidSystemWebViewClient((SystemWebView)view);
    } else {
      try {
        Object webViewClient = getWebClientViaReflection(view, "mWebViewClient");
        if (webViewClient != null) {
          return new WrappingSelendroidWebClient((WebViewClient) webViewClient);
        }
      } catch (NoSuchFieldException nsfe) {}
      catch (IllegalAccessException iae) {}
      return new SelendroidWebClient();
    }
  }

  private CordovaWebViewClient encapulateCordovaWebViewClientForView(CordovaWebView view) throws NoSuchFieldException, IllegalAccessException {
    if (reflectionGet(view, "viewClient") != null) {
      return new WrappingSelendroidCordovaWebClient(view);
    }
    return new SelendroidCordovaWebClient((CordovaInterface)reflectionGet(view, "cordova"), view);
  }

  private Object reflectionGet(Object object, String field) throws NoSuchFieldException, IllegalAccessException {
    Field f = object.getClass().getDeclaredField(field);
    f.setAccessible(true);
    return f.get(object);
  }

  private Object getWebClientViaReflection(WebView view, String clientField) throws NoSuchFieldException, IllegalAccessException {
    return reflectionGet(reflectionGet(reflectionGet(view, "mProvider"), "mContentsClientAdapter"), clientField);
  }

  private String getWindowString() {
    String window = "";
    if (!currentWindowOrFrame.getKey().equals("")) {
      window = "document['$wdc_']['" + currentWindowOrFrame.getKey() + "'] ||";
    }
    return (window += "window");
  }

  Object injectJavascript(String toExecute, Object args, KnownElements ke) {
    try {
      String executeScript = AndroidAtoms.EXECUTE_SCRIPT.getValue();
      toExecute =
          "var win_context; try{win_context= " + getWindowString() + "}catch(e){"
              + "win_context=window;}with(win_context){" + toExecute + "}";
      String wrappedScript =
          "(function(){ var win; try{win=" + getWindowString() + "}catch(e){win=window}"
              + "with(win){return (" + executeScript + ")(" + escapeAndQuote(toExecute) + ", ["
              + convertToJsArgs(args, ke) + "], true)}})()";
      return executeJavascriptInWebView("alert('selendroid<' + document.charset + '>:'+"
          + wrappedScript + ")");
    } catch (JSONException e) {
      SelendroidLogger.error("Failed to convert args to jsArgs", e);
      throw new RuntimeException(e);
    }
  }

  Object injectAtomJavascript(String toExecute, Object args, KnownElements ke) throws JSONException {
    return executeJavascriptInWebView("alert('selendroid<' + document.charset +'>:'+ (" + toExecute
        + ")(" + convertToJsArgs(args, ke) + "))");
  }

  public Object executeAsyncJavascript(String toExecute, JSONArray args, KnownElements ke) {
    try {
      String callbackFunction =
          "function(result){alert('selendroid<' + document.charset + '>:'+result);}";
      String script =
          "try {("
              + AndroidAtoms.EXECUTE_ASYNC_SCRIPT.getValue()
              + ")("
              + escapeAndQuote(toExecute)
              + ", ["
              + convertToJsArgs(args, ke)
              + "], "
              + asyncScriptTimeout
              + ", "
              + callbackFunction
              + ","
              + "true, "
              + getWindowString()
              + ")}catch(e){alert('selendroid<' + document.charset + '>:{\"status\":13,\"value\":\"' + e + '\"}')}";
      return executeJavascriptInWebView(script);
    } catch (JSONException je) {
      SelendroidLogger.error("Failed convert JSONArray to jsArgs", je);
      throw new RuntimeException(je);
    }
  }

  Boolean isInFrame() {
    return !currentWindowOrFrame.getKey().equals("");
  }

  void resetPageIsLoading() {
    pageStartedLoading = false;
    pageDoneLoading = false;
  }

  void setEditAreaHasFocus(boolean focused) {
    editAreaHasFocus = focused;
  }

  void waitForPageToLoad() {
    synchronized (syncObject) {
      long timeout = System.currentTimeMillis() + START_LOADING_TIMEOUT;
      while (!pageStartedLoading && (System.currentTimeMillis() < timeout)) {
        try {
          syncObject.wait(POLLING_INTERVAL);
        } catch (InterruptedException e) {
          throw new RuntimeException();
        }
      }
      long end = System.currentTimeMillis() + pageLoadTimeout;
      while (!pageDoneLoading && pageStartedLoading && (System.currentTimeMillis() < end)) {
        try {
          syncObject.wait(POLLING_INTERVAL);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      if (!pageDoneLoading && pageStartedLoading) {
        throw new TimeoutException(String.format("Timed out after %d seconds waiting for page to load",
          SECONDS.convert(pageLoadTimeout, MILLISECONDS)));
      }
    }
  }

  void waitUntilEditAreaHasFocus() {
    long timeout = System.currentTimeMillis() + FOCUS_TIMEOUT;
    while (!editAreaHasFocus && (System.currentTimeMillis() < timeout)) {
      try {
        Thread.sleep(POLLING_INTERVAL);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public class ExtendedCordovaChromeClient extends CordovaChromeClient {

    public Boolean callSuper = true;

    public ExtendedCordovaChromeClient(CordovaWebView app) {
      super(null, app);
    }

    /**
     * Unconventional way of adding a Javascript interface but the main reason why I took this way
     * is that it is working stable compared to the webview.addJavascriptInterface way.
     */
    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult jsResult) {
      if (message != null && message.startsWith("selendroid<")) {
        jsResult.confirm();

        synchronized (syncObject) {
          String res = message.replaceFirst("selendroid<", "");
          int i = res.indexOf(">:");
          String enc = res.substring(0, i);
          res = res.substring(i + 2);
          /*
           * Workaround for Japanese character encodings: Replace U+00A5 with backslash so that we
           * can properly parse JSON strings contains backslash escapes, since WebKit maps 0x5C
           * (used for character escaping in all of the Japanses character encodings) to U+00A5 (YEN
           * SIGN) and breaks escape characters.
           */
          if (("EUC-JP".equals(enc) || "Shift_JIS".equals(enc) || "ISO-2022-JP".equals(enc))
              && res.contains("\u00a5")) {
            SelendroidLogger.info("Perform workaround for japanese character encodings");
            SelendroidLogger.debug("Original String: " + res);
            res = res.replace("\u00a5", "\\");
            SelendroidLogger.debug("Replaced result: " + res);
          }
          result = res;
          syncObject.notify();
        }

        return true;
      } else if (callSuper) {
        currentAlertMessage.add(message == null ? "null" : message);
        SelendroidLogger.info("new alert message: " + message);
        return super.onJsAlert(view, url, message, jsResult);
      } else {
        return false;
      }
    }
  }

  public class WrappingCordovaChromeClient extends CordovaChromeClient {

    private CordovaChromeClient WCC;
    private ExtendedCordovaChromeClient selendroidCCC;

    public WrappingCordovaChromeClient(CordovaInterface ci, CordovaWebView view) throws NoSuchFieldException, IllegalAccessException {
      super(ci, view);
      selendroidCCC = new ExtendedCordovaChromeClient(view);
      selendroidCCC.callSuper = false;
      WCC = (CordovaChromeClient) reflectionGet(view, "chromeClient");
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
      // if the alert was a selendroid one, this method will return true, otherwise false
      // and the alert should be propagated to the original WebChromeClient
      if (!selendroidCCC.onJsAlert(view, url, message, result)) {
        return WCC.onJsAlert(view, url, message, result);
      }
      return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
      return WCC.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
      return WCC.onJsPrompt(view, url, message, defaultValue, result);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
      WCC.onProgressChanged(view, newProgress);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
      WCC.onReceivedTitle(view, title);
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
      WCC.onReceivedIcon(view, icon);
    }

    @Override
    public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
      WCC.onReceivedTouchIconUrl(view, url, precomposed);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
      WCC.onShowCustomView(view, callback);
    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        WCC.onShowCustomView(view, requestedOrientation, callback);
      }
    }

    @Override
    public void onHideCustomView() {
      WCC.onHideCustomView();
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
      return WCC.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
    }

    @Override
    public void onRequestFocus(WebView view) {
      WCC.onRequestFocus(view);
    }

    @Override
    public void onCloseWindow(WebView window) {
      WCC.onCloseWindow(window);
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
      return WCC.onJsBeforeUnload(view, url, message, result);
    }

    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {
      WCC.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
    }

    @Override
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
      WCC.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
      WCC.onGeolocationPermissionsShowPrompt(origin, callback);
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
      WCC.onGeolocationPermissionsHidePrompt();
    }

// can't build for things above 4.1.1.4
//    @Override
//    public void onPermissionRequest(PermissionRequest request) {
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        WCC.onPermissionRequest(request);
//      }
//    }
//
//    @Override
//    public void onPermissionRequestCanceled(PermissionRequest request) {
//      WCC.onPermissionRequestCanceled(request);
//    }

    @Override
    public boolean onJsTimeout() {
      return WCC.onJsTimeout();
    }

    @Override
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
      WCC.onConsoleMessage(message, lineNumber, sourceID);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
      return WCC.onConsoleMessage(consoleMessage);
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
      return WCC.getDefaultVideoPoster();
    }

    @Override
    public View getVideoLoadingProgressView() {
      return WCC.getVideoLoadingProgressView();
    }

    @Override
    public void getVisitedHistory(ValueCallback<String[]> callback) {
      WCC.getVisitedHistory(callback);
    }

// can't build over 4.1.1.4
//    @Override
//    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        return WCC.onShowFileChooser(webView, filePathCallback, fileChooserParams);
//      }
//      return false;
//    }
  }

  //Like ExtendedCordovaClient, but for Cordova 4.0.0
  public class ExtendedSystemWebChromeClient extends SystemWebChromeClient {


    public  ExtendedSystemWebChromeClient(SystemWebViewEngine parentEngine) {
      super(parentEngine);
    }

    /**
     * Unconventional way of adding a Javascript interface but the main reason why I took this way
     * is that it is working stable compared to the webview.addJavascriptInterface way.
     **/
   @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult jsResult) {
      if (message != null && message.startsWith("selendroid<")) {
        jsResult.confirm();

        synchronized (syncObject) {
          String res = message.replaceFirst("selendroid<", "");
          int i = res.indexOf(">:");
          String enc = res.substring(0, i);
          res = res.substring(i + 2);

          /* Workaround for Japanese character encodings: Replace U+00A5 with backslash so that we
           * can properly parse JSON strings contains backslash escapes, since WebKit maps 0x5C
           * (used for character escaping in all of the Japanses character encodings) to U+00A5 (YEN
           * SIGN) and breaks escape characters.
           */
          if (("EUC-JP".equals(enc) || "Shift_JIS".equals(enc) || "ISO-2022-JP".equals(enc))
              && res.contains("\u00a5")) {
            SelendroidLogger.info("Perform workaround for japanese character encodings");
            SelendroidLogger.debug("Original String: " + res);
            res = res.replace("\u00a5", "\\");
            SelendroidLogger.debug("Replaced result: " + res);
          }
          result = res;
          syncObject.notify();
        }

        return true;
      } else {
        currentAlertMessage.add(message == null ? "null" : message);
        SelendroidLogger.info("new alert message: " + message);
        return super.onJsAlert(view, url, message, jsResult);
      }
    }
  }

  public class SelendroidSystemWebViewClient extends SystemWebViewClient {
    public SelendroidSystemWebViewClient(SystemWebView view) {
      super(new SystemWebViewEngine(view));
    }
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      synchronized (syncObject) {
        pageStartedLoading = true;
        syncObject.notify();
      }
    }
    @Override
    public void onPageFinished(WebView view, String url) {
      synchronized (syncObject) {
        pageDoneLoading = true;
        syncObject.notify();
      }
    }
  }

  public class SelendroidCordovaWebClient extends CordovaWebViewClient {

    public SelendroidCordovaWebClient(CordovaInterface cordova, CordovaWebView view) {
      super(cordova, view);
    }
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      synchronized (syncObject) {
        pageStartedLoading = true;
        syncObject.notify();
      }
    }
    @Override
    public void onPageFinished(WebView view, String url) {
      synchronized (syncObject) {
        pageDoneLoading = true;
        syncObject.notify();
      }
    }
  }

  public class WrappingSelendroidCordovaWebClient extends CordovaWebViewClient {
    private CordovaWebViewClient selendroidCWVC;
    private CordovaWebViewClient CWVC;

    public WrappingSelendroidCordovaWebClient(CordovaWebView webView) throws NoSuchFieldException, IllegalAccessException {
      super(null, webView);
      CWVC = (CordovaWebViewClient)reflectionGet(webView, "viewClient");
      selendroidCWVC = new SelendroidCordovaWebClient(null, webView);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      selendroidCWVC.onPageStarted(view, url, favicon);
      CWVC.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      selendroidCWVC.onPageFinished(view, url);
      CWVC.onPageFinished(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return CWVC.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
      CWVC.onLoadResource(view, url);
    }

// can't build over 4.1.1.4
//    @Override
//    public void onPageCommitVisible(WebView view, String url) {
//      if (Build.VERSION.SDK_INT >= 23) {
//        CWVC.onPageCommitVisible(view, url);
//      }
//    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        return CWVC.shouldInterceptRequest(view, url);
      }
      return null;
    }

// can't build over 4.1.1.4
//    @Override
//    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        return CWVC.shouldInterceptRequest(view, request);
//      }
//      return null;
//    }

    @Override
    public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
      CWVC.onTooManyRedirects(view, cancelMsg, continueMsg);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
      CWVC.onReceivedError(view, errorCode, description, failingUrl);
    }

// can't build over 4.1.1.4
//    @Override
//    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//      if (Build.VERSION.SDK_INT >= 23) {
//        CWVC.onReceivedError(view, request, error);
//      }
//    }
//
//    @Override
//    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
//      if (Build.VERSION.SDK_INT >= 23) {
//        CWVC.onReceivedHttpError(view, request, errorResponse);
//      }
//    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
      CWVC.onFormResubmission(view, dontResend, resend);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
      CWVC.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
      CWVC.onReceivedSslError(view, handler, error);
    }

// can't build above 4.1.1.4 with maven! actually a good reason to switch to gradle.
//    @Override
//    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        CWVC.onReceivedClientCertRequest(view, request);
//      }
//    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
      CWVC.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
      return CWVC.shouldOverrideKeyEvent(view, event);
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
      CWVC.onUnhandledKeyEvent(view, event);
    }

// can't build over 4.1.1.4
//    @Override
//    public void onUnhandledInputEvent(WebView view, InputEvent event) {
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        CWVC.onUnhandledInputEvent(view, event);
//      }
//    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
      CWVC.onScaleChanged(view, oldScale, newScale);
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
        CWVC.onReceivedLoginRequest(view, realm, account, args);
      }
    }
  }

  public class SelendroidWebChromeClient extends WebChromeClient {

    private boolean callSuper = true;

    public SelendroidWebChromeClient() {
    }

    public SelendroidWebChromeClient(boolean callSuper) {
      this.callSuper = callSuper;
    }

    /**
     * Unconventional way of adding a Javascript interface but the main reason why I took this way
     * is that it is working stable compared to the webview.addJavascriptInterface way.
     */
    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult jsResult) {
      if (message != null && message.startsWith("selendroid<")) {
        jsResult.confirm();

        synchronized (syncObject) {
          String res = message.replaceFirst("selendroid<", "");
          int i = res.indexOf(">:");
          String enc = res.substring(0, i);
          res = res.substring(i + 2);
          /*
           * Workaround for Japanese character encodings: Replace U+00A5 with backslash so that we
           * can properly parse JSON strings contains backslash escapes, since WebKit maps 0x5C
           * (used for character escaping in all of the Japanses character encodings) to U+00A5 (YEN
           * SIGN) and breaks escape characters.
           */
          if (("EUC-JP".equals(enc) || "Shift_JIS".equals(enc) || "ISO-2022-JP".equals(enc))
              && res.contains("\u00a5")) {
            SelendroidLogger.info("Perform workaround for japanese character encodings");
            SelendroidLogger.debug("Original String: " + res);
            res = res.replace("\u00a5", "\\");
            SelendroidLogger.debug("Replaced result: " + res);
          }
          result = res;
          syncObject.notify();
        }

        return true;
      } else if (callSuper){
        currentAlertMessage.add(message == null ? "null" : message);
        SelendroidLogger.info("new alert message: " + message);
        return super.onJsAlert(view, url, message, jsResult);
      }
      return false;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
      currentAlertMessage.add(message == null ? "null" : message);
      SelendroidLogger.info("new confirm message: " + message);
      if (callSuper) {
        return super.onJsConfirm(view, url, message, result);
      }
      return false;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
        JsPromptResult result) {
      currentAlertMessage.add(message == null ? "null" : message);
      SelendroidLogger.info("new prompt message: " + message);
      if (callSuper) {
        return super.onJsPrompt(view, url, message, defaultValue, result);
      }
      return false;
    }

  }

  // Would be so awesome to use a Proxy here and just intercept the two methods we care about.
  // alas, WebChromeClient is a concrete class and there's no interface provided.
  // And trying to do alternatives in Android proved to be too difficult
  public class WrappedChromeClient extends WebChromeClient {

    private WebChromeClient WCC;
    private WebChromeClient selendroidWCC = new SelendroidWebChromeClient(false);

    public WrappedChromeClient(WebChromeClient webChromeClient) {
      WCC = webChromeClient;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
      // if the alert was a selendroid one, this method will return true, otherwise false
      // and the alert should be propagated to the original WebChromeClient
      if (!selendroidWCC.onJsAlert(view, url, message, result)) {
        return WCC.onJsAlert(view, url, message, result);
      }
      return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
      selendroidWCC.onJsConfirm(view, url, message, result);
      return WCC.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
      selendroidWCC.onJsPrompt(view, url, message, defaultValue, result);
      return WCC.onJsPrompt(view, url, message, defaultValue, result);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
      WCC.onProgressChanged(view, newProgress);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
      WCC.onReceivedTitle(view, title);
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
      WCC.onReceivedIcon(view, icon);
    }

    @Override
    public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
      WCC.onReceivedTouchIconUrl(view, url, precomposed);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
      WCC.onShowCustomView(view, callback);
    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        WCC.onShowCustomView(view, requestedOrientation, callback);
      }
    }

    @Override
    public void onHideCustomView() {
      WCC.onHideCustomView();
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
      return WCC.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
    }

    @Override
    public void onRequestFocus(WebView view) {
      WCC.onRequestFocus(view);
    }

    @Override
    public void onCloseWindow(WebView window) {
      WCC.onCloseWindow(window);
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
      return WCC.onJsBeforeUnload(view, url, message, result);
    }

    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {
      WCC.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
    }

    @Override
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
      WCC.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
      WCC.onGeolocationPermissionsShowPrompt(origin, callback);
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
      WCC.onGeolocationPermissionsHidePrompt();
    }

// can't build for things above 4.1.1.4
//    @Override
//    public void onPermissionRequest(PermissionRequest request) {
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        WCC.onPermissionRequest(request);
//      }
//    }
//
//    @Override
//    public void onPermissionRequestCanceled(PermissionRequest request) {
//      WCC.onPermissionRequestCanceled(request);
//    }

    @Override
    public boolean onJsTimeout() {
      return WCC.onJsTimeout();
    }

    @Override
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
      WCC.onConsoleMessage(message, lineNumber, sourceID);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
      return WCC.onConsoleMessage(consoleMessage);
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
      return WCC.getDefaultVideoPoster();
    }

    @Override
    public View getVideoLoadingProgressView() {
      return WCC.getVideoLoadingProgressView();
    }

    @Override
    public void getVisitedHistory(ValueCallback<String[]> callback) {
      WCC.getVisitedHistory(callback);
    }

// can't build over 4.1.1.4
//    @Override
//    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        return WCC.onShowFileChooser(webView, filePathCallback, fileChooserParams);
//      }
//      return false;
//    }
  }

  public class SelendroidWebClient extends WebViewClient {
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      synchronized (syncObject) {
        pageStartedLoading = true;
        syncObject.notify();
      }
    }
    @Override
    public void onPageFinished(WebView view, String url) {
      synchronized (syncObject) {
        pageDoneLoading = true;
        syncObject.notify();
      }
    }
  }

  // Would be so awesome to use a Proxy here and just intercept the two methods we care about.
  // alas, WebViewClient is a concrete class and there's no interface provided.
  // And trying to do alternatives in Android proved to be too difficult
  public class WrappingSelendroidWebClient extends WebViewClient {
    private WebViewClient selendroidWVC = new SelendroidWebClient();
    private WebViewClient WVC;

    public WrappingSelendroidWebClient(WebViewClient webViewClient) {
      WVC = webViewClient;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      selendroidWVC.onPageStarted(view, url, favicon);
      WVC.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      selendroidWVC.onPageFinished(view, url);
      WVC.onPageFinished(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return WVC.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
      WVC.onLoadResource(view, url);
    }

// can't build over 4.1.1.4
//    @Override
//    public void onPageCommitVisible(WebView view, String url) {
//      if (Build.VERSION.SDK_INT >= 23) {
//        WVC.onPageCommitVisible(view, url);
//      }
//    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        return WVC.shouldInterceptRequest(view, url);
      }
      return null;
    }

// can't build over 4.1.1.4
//    @Override
//    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        return WVC.shouldInterceptRequest(view, request);
//      }
//      return null;
//    }

    @Override
    public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
      WVC.onTooManyRedirects(view, cancelMsg, continueMsg);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
      WVC.onReceivedError(view, errorCode, description, failingUrl);
    }

// can't build over 4.1.1.4
//    @Override
//    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//      if (Build.VERSION.SDK_INT >= 23) {
//        WVC.onReceivedError(view, request, error);
//      }
//    }
//
//    @Override
//    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
//      if (Build.VERSION.SDK_INT >= 23) {
//        WVC.onReceivedHttpError(view, request, errorResponse);
//      }
//    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
      WVC.onFormResubmission(view, dontResend, resend);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
      WVC.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
      WVC.onReceivedSslError(view, handler, error);
    }

// can't build above 4.1.1.4 with maven! actually a good reason to switch to gradle.
//    @Override
//    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        WVC.onReceivedClientCertRequest(view, request);
//      }
//    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
      WVC.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
      return WVC.shouldOverrideKeyEvent(view, event);
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
      WVC.onUnhandledKeyEvent(view, event);
    }

// can't build over 4.1.1.4
//    @Override
//    public void onUnhandledInputEvent(WebView view, InputEvent event) {
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        WVC.onUnhandledInputEvent(view, event);
//      }
//    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
      WVC.onScaleChanged(view, oldScale, newScale);
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
        WVC.onReceivedLoginRequest(view, realm, account, args);
      }
    }
  }


  public String getTitle() {
    if (webview == null) {
      throw new SelendroidException("No open web view.");
    }
    long end = System.currentTimeMillis() + UI_TIMEOUT;
    final String[] title = new String[1];
    done = false;
    serverInstrumentation.getCurrentActivity().runOnUiThread(new Runnable() {
      public void run() {
        synchronized (syncObject) {
          title[0] = webview.getTitle();
          done = true;
          syncObject.notify();
        }
      }
    });
    waitForDone(end, UI_TIMEOUT, "Failed to get title");
    return title[0];
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

  private void runSynchronously(Runnable r, long timeout) {
    synchronized (r) {
      serverInstrumentation.getCurrentActivity().runOnUiThread(r);
      try {
        r.wait(timeout);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public WebView getWebview() {
    return webview;
  }

  public Set<Cookie> getCookies(String url) {

    return sm.getAllCookies(url);

  }

  public void removeAllCookie(String url) {

    sm.removeAllCookies(url);

  }

  public void remove(String url, String name) {

    sm.remove(url, name);

  }

  public void setCookies(String url, Cookie cookie) {

    sm.addCookie(url, cookie);

  }

  public void frame(int index) throws JSONException {
    currentWindowOrFrame =
        processFrameExecutionResult(injectAtomJavascript(AndroidAtoms.FRAME_BY_INDEX.getValue(),
            index, null));
  }

  public void frame(String frameNameOrId) throws JSONException {
    currentWindowOrFrame =
        processFrameExecutionResult(injectAtomJavascript(
            AndroidAtoms.FRAME_BY_ID_OR_NAME.getValue(), frameNameOrId, null));
  }

  public void frame(AndroidWebElement frameElement) {
    currentWindowOrFrame =
        processFrameExecutionResult(executeScript("return arguments[0].contentWindow;",
            frameElement, null));
  }

  public void switchToDefaultContent() {
    currentWindowOrFrame = new DomWindow("");
  }

  private DomWindow processFrameExecutionResult(Object result) {
    if (result == null || "undefined".equals(result)) {
      return null;
    }
    try {
      JSONObject json = new JSONObject((String) result);
      JSONObject value = json.getJSONObject("value");
      return new DomWindow(value.getString("WINDOW"));
    } catch (JSONException e) {
      throw new RuntimeException("Failed to parse JavaScript result: " + result.toString(), e);
    }
  }

  public void back() {
    resetPageIsLoading();
    runSynchronously(new Runnable() {
      public void run() {
        webview.goBack();
      }
    }, 500);
    waitForPageToLoad();
  }

  public void forward() {
    resetPageIsLoading();
    runSynchronously(new Runnable() {
      public void run() {
        webview.goForward();
      }
    }, 500);
    waitForPageToLoad();
  }

  public void refresh() {
    resetPageIsLoading();
    runSynchronously(new Runnable() {
      public void run() {
        webview.reload();
      }
    }, 500);
    waitForPageToLoad();
  }

  public boolean isAlertPresent() {
    SelendroidLogger.info("checking currentAlertMessage: " + currentAlertMessage.size());
    return !currentAlertMessage.isEmpty();
  }

  public String getCurrentAlertMessage() {
    SelendroidLogger.info("getting currentAlertMessage: " + currentAlertMessage.peek());
    return currentAlertMessage.peek();
  }

  public void clearCurrentAlertMessage() {
    SelendroidLogger.info("clearing the current alert message: " + currentAlertMessage.remove());
  }

  public void setAsyncScriptTimeout(long timeout) {
    asyncScriptTimeout = timeout;
  }

  public void setPageLoadTimeout(long timeout) {
    pageLoadTimeout = timeout;
  }
}
