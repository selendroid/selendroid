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
import io.selendroid.android.KeySender;
import io.selendroid.android.MotionSender;
import io.selendroid.android.ViewHierarchyAnalyzer;
import io.selendroid.android.WebViewKeySender;
import io.selendroid.android.WebViewMotionSender;
import io.selendroid.android.internal.DomWindow;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.StaleElementReferenceException;
import io.selendroid.server.model.internal.WebViewHandleMapper;
import io.selendroid.server.model.js.AndroidAtoms;
import io.selendroid.util.SelendroidLogger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class SelendroidWebDriver {
  private static final String ELEMENT_KEY = "ELEMENT";
  private static final long FOCUS_TIMEOUT = 1000L;
  private static final long LOADING_TIMEOUT = 30000L;
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
  private SelendroidWebChromeClient chromeClient = null;
  private DomWindow currentWindowOrFrame;
  private Queue<String> currentAlertMessage = new LinkedList<String>();
  private TouchScreen touch;
  private KeySender keySender;
  private MotionSender motionSender;
  private long scriptTimeout = 60000L;
  private long asyncScriptTimeout = 0L;
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
      SelendroidLogger.info("failed to figure out what this is to convert to execute script:" + obj);
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
      je.printStackTrace();
      throw new RuntimeException(je);
    }
  }

  public Object executeAtom(AndroidAtoms atom, JSONArray args, KnownElements ke)
      throws JSONException {
    final String myScript = atom.getValue();
    String scriptInWindow =
        "(function(){ " + " var win; try{win=" + getWindowString() + "}catch(e){win=window;}"
            + "with(win){return (" + myScript + ")(" + convertToJsArgs(args, ke) + ")}})()";
    String jsResult = executeJavascriptInWebView("alert('selendroid:'+" + scriptInWindow + ")");


    SelendroidLogger.info("jsResult: " + jsResult);
    if (jsResult == null || "undefined".equals(jsResult)) {
      return null;
    }

    try {
      JSONObject json = new JSONObject(jsResult);
      if (0 != json.optInt("status")) {
        Object value = json.get("value");
        if ((value instanceof String && value.equals("Element does not exist in cache"))
            || (value instanceof JSONObject && ((JSONObject) value).getString("message").equals(
                "Element does not exist in cache"))) {
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
        // seems to be needed
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
    try {
      return injectJavascript(script, new JSONArray(), null);
    } catch (JSONException je) {
      je.printStackTrace();
      throw new RuntimeException(je);
    }
  }

  public Object executeScript(String script, JSONArray args, KnownElements ke) {
    try {
      return injectJavascript(script, args, ke);
    } catch (JSONException je) {
      je.printStackTrace();
      throw new RuntimeException(je);
    }
  }

  public Object executeScript(String script, Object args, KnownElements ke) {
    try {
      return injectJavascript(script, args, ke);
    } catch (JSONException je) {
      je.printStackTrace();
      throw new RuntimeException(je);
    }
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
          chromeClient = new SelendroidWebChromeClient();
          view.setWebChromeClient(chromeClient);

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
          SelendroidLogger.error("An error occured while configuring the web view", e);
        }
      }
    });
  }

  private String getWindowString() {
    String window = "";
    if (!currentWindowOrFrame.getKey().equals("")) {
      window = "document['$wdc_']['" + currentWindowOrFrame.getKey() + "'] ||";
    }
    return (window += "window");
  }

  Object injectJavascript(String toExecute, Object args, KnownElements ke) throws JSONException {
    String executeScript = AndroidAtoms.EXECUTE_SCRIPT.getValue();
    toExecute =
        "var win_context; try{win_context= " + getWindowString() + "}catch(e){"
            + "win_context=window;}with(win_context){" + toExecute + "}";
    String wrappedScript =
        "(function(){ var win; try{win=" + getWindowString() + "}catch(e){win=window}"
            + "with(win){return (" + executeScript + ")(" + escapeAndQuote(toExecute) + ", ["
            + convertToJsArgs(args, ke) + "], true)}})()";
    return executeJavascriptInWebView("alert('selendroid:'+" + wrappedScript + ")");
  }

  Object injectAtomJavascript(String toExecute, Object args, KnownElements ke) throws JSONException {
    return executeJavascriptInWebView("alert('selendroid:'+ (" + toExecute + ")("
        + convertToJsArgs(args, ke) + "))");
  }

  public Object executeAsyncJavascript(String toExecute, JSONArray args, KnownElements ke) {
    try {
      String callbackFunction = "function(result){alert('selendroid:'+result);}";
      String script = "try {(" + AndroidAtoms.EXECUTE_ASYNC_SCRIPT.getValue() + ")(" + escapeAndQuote(toExecute) + ", ["
          + convertToJsArgs(args, ke) + "], " + asyncScriptTimeout + ", " + callbackFunction + ","
          + "true, " + getWindowString() + ")}catch(e){alert('selendroid:{\"status\":13,\"value\":\"' + e + '\"}')}";
      return executeJavascriptInWebView(script);
    } catch (JSONException je) {
      je.printStackTrace();
      throw new RuntimeException(je);
    }
  }

  Boolean isInFrame() {
    return !currentWindowOrFrame.getKey().equals("");
  }

  /*
   * an attempt to help get the proper coordinates of the frame there are seemingly too many other
   * factors to get this adequately this would be to facilitate 'native' clicks on webelements in
   * frames see AndroidWebElement#click Point getFrameLocation() { if
   * (!currentWindowOrFrame.getKey().equals("")) { String script = "function(){var w = " +
   * getWindowString() +
   * "getFrameTop = function(f_win){return f_win.frameElement.getBoundingClientRect().top + " +
   * "(f_win.parent.frameElement ? getFrameTop(f_win.parent):0);};" +
   * "getFrameLeft = function(f_win){return f_win.frameElement.getBoundingClientRect().left + " +
   * "(f_win.parent.frameElement ? getFrameLeft(f_win.parent):0);};" +
   * "return [getFrameTop(w), getFrameLeft(w)]}"; JSONArray ret = null; try { String val =
   * (String)injectAtomJavascript(script, null, null); SelendroidLogger.log("val - frame location: "
   * + val); ret = new JSONArray("[" + val + "]"); return new Point(ret.getInt(0), ret.getInt(1)); }
   * catch (JSONException e) { } } return new Point(0, 0); }
   */


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

      long end = System.currentTimeMillis() + LOADING_TIMEOUT;
      while (!pageDoneLoading && pageStartedLoading && (System.currentTimeMillis() < end)) {
        try {
          syncObject.wait(LOADING_TIMEOUT);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
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


  public class SelendroidWebChromeClient extends WebChromeClient {

    /**
     * Unconventional way of adding a Javascript interface but the main reason why I took this way
     * is that it is working stable compared to the webview.addJavascriptInterface way.
     */
    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult jsResult) {
      if (message != null && message.startsWith("selendroid:")) {
        jsResult.confirm();

        synchronized (syncObject) {
          result = message.replaceFirst("selendroid:", "");
          syncObject.notify();
        }

        return true;
      } else {
        currentAlertMessage.add(message == null ? "null" : message);
        SelendroidLogger.info("new alert message: " + message);
        return super.onJsAlert(view, url, message, jsResult);
      }
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
      currentAlertMessage.add(message == null ? "null" : message);
      SelendroidLogger.info("new confirm message: " + message);
      return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
        JsPromptResult result) {
      currentAlertMessage.add(message == null ? "null" : message);
      SelendroidLogger.info("new prompt message: " + message);
      return super.onJsPrompt(view, url, message, defaultValue, result);
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
        e.printStackTrace();
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
    pageDoneLoading = false;
    runSynchronously(new Runnable() {
      public void run() {
        webview.goBack();
      }
    }, 500);
    waitForPageToLoad();
  }

  public void forward() {
    pageDoneLoading = false;
    runSynchronously(new Runnable() {
      public void run() {
        webview.goForward();
      }
    }, 500);
    waitForPageToLoad();
  }

  public void refresh() {
    pageDoneLoading = false;
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
}
