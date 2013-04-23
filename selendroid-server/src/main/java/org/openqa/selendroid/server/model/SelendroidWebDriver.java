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

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.android.ViewHierarchyAnalyzer;
import org.openqa.selendroid.android.internal.DomWindow;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.exceptions.StaleElementReferenceException;
import org.openqa.selendroid.server.model.js.AndroidAtoms;
import org.openqa.selendroid.util.SelendroidLogger;

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

  public SelendroidWebDriver(ServerInstrumentation serverInstrumentation) {
    this.serverInstrumentation = serverInstrumentation;
    init();
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
  private String convertToJsArgs(JSONArray args) throws JSONException{
    StringBuilder toReturn = new StringBuilder();

    int length = args.length();
    for (int i = 0; i < length; i++) {
      toReturn.append((i > 0) ? "," : "");
      toReturn.append(convertToJsArgs(args.get(i)));
    }
    SelendroidLogger.log("convertToJsArgs: " + toReturn.toString());
    return toReturn.toString();
  }

  private String convertToJsArgs(Object obj) {
    StringBuilder toReturn = new StringBuilder();
    if (obj instanceof List<?>) {
      toReturn.append("[");
      List<Object> aList = (List<Object>) obj;
      for (int j = 0; j < aList.size(); j++) {
        String comma = ((j == 0) ? "" : ",");
        toReturn.append(comma + convertToJsArgs(aList.get(j)));
      }
      toReturn.append("]");
    } else if (obj instanceof Map<?, ?>) {
      Map<Object, Object> aMap = (Map<Object, Object>) obj;
      String toAdd = "{";
      for (Object key : aMap.keySet()) {
        toAdd += key + ":" + convertToJsArgs(aMap.get(key)) + ",";
      }
      toReturn.append(toAdd.substring(0, toAdd.length() - 1) + "}");
    } else if (obj instanceof AndroidWebElement) {
      // A WebElement is represented in JavaScript by an Object as
      // follow: {"ELEMENT":"id"} where "id" refers to the id
      // of the HTML element in the javascript cache that can
      // be accessed throught bot.inject.cache.getCache_()
      toReturn.append("{\"" + ELEMENT_KEY + "\":\"" + ((AndroidWebElement) obj).getId()
          + "\"}");
    } else if (obj instanceof DomWindow) {
      // A DomWindow is represented in JavaScript by an Object as
      // follow {"WINDOW":"id"} where "id" refers to the id of the
      // DOM window in the cache.
      toReturn.append("{\"" + WINDOW_KEY + "\":\"" + ((DomWindow) obj).getKey() + "\"}");
    } else if (obj instanceof Number || obj instanceof Boolean) {
      toReturn.append(String.valueOf(obj));
    } else if (obj instanceof String) {
      toReturn.append(escapeAndQuote((String) obj));
    }
    SelendroidLogger.log("convertToJsArgs: " + toReturn.toString());
    return toReturn.toString();
  }

  public Object executeAtom(AndroidAtoms atom, Object ... args) {
    JSONArray array = new JSONArray();
    for (int i = 0; i < args.length; i++) {
      array.put(args[i]);
    }
    try {
      return executeAtom(atom, array);
    } catch (JSONException je) {
      je.printStackTrace();
      throw new RuntimeException(je);
    }
  }

  public Object executeAtom(AndroidAtoms atom, JSONArray args) throws JSONException {
    final String myScript = atom.getValue();
    String scriptInWindow =
        "(function(){ " + " var win; try{win=window;}catch(e){win=window;}" + "with(win){return ("
            + myScript + ")(" + convertToJsArgs(args) + ")}})()";
    String jsResult = executeJavascriptInWebView("alert('selendroid:'+" + scriptInWindow + ")");


    SelendroidLogger.log("jsResult: " + jsResult);
    if (jsResult == null || "undefined".equals(jsResult)) {
      return null;
    }

    try {
      JSONObject json = new JSONObject(jsResult);
      if (0 != json.optInt("status")) {
        Object value = json.get("value");
        if ((value instanceof String && value.equals("Element does not exist in cache")) ||
            (value instanceof JSONObject &&
                ((JSONObject)value).getString("message").equals("Element does not exist in cache"))) {
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
    ServerInstrumentation.getInstance().runOnUiThread(new Runnable() {
      public void run() {
        if (webview.getUrl() == null) {
          return;
        }
        webview.loadUrl("javascript:" + script);
      }
    });
    long timeout =
        System.currentTimeMillis() + 60000; /* how long to wait to allow the script to run? This could be arbitrarily high for some users... setting extremely high for now (1 min) */
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
      return injectJavascript(script, false, new JSONArray());
    } catch (JSONException je) {
      je.printStackTrace();
      throw new RuntimeException(je);
    }
  }

  public Object executeScript(String script, JSONArray args) {
    try {
      return injectJavascript(script, false, args);
    } catch (JSONException je) {
      je.printStackTrace();
      throw new RuntimeException(je);
    }
  }

  public Object executeScript(String script, Object args) {
    try {
      return injectJavascript(script, false, args);
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
    serverInstrumentation.runOnUiThread(new Runnable() {
      public void run() {
        webview.loadUrl(url);
      }
    });
  }

  public Object getWindowSource() throws JSONException {
    JSONObject source =
        new JSONObject(
            (String) executeScript("return (new XMLSerializer()).serializeToString(document.documentElement);"));
    return source.getString("value");
  }

  protected void init() {
    System.out.println("Selendroid webdriver init");
    long start = System.currentTimeMillis();
    webview = ViewHierarchyAnalyzer.getDefaultInstance().findWebView();
    while (webview == null
        && (System.currentTimeMillis() - start <= ServerInstrumentation.getInstance()
            .getAndroidWait().getTimeoutInMillis())) {
      DefaultSelendroidDriver.sleepQuietly(500);
      webview = ViewHierarchyAnalyzer.getDefaultInstance().findWebView();
    }

    if (webview == null) {
      throw new SelendroidException("No webview found on current activity.");
    }
    configureWebView(webview);
  }

  private void configureWebView(final WebView view) {
    ServerInstrumentation.getInstance().runOnUiThread(new Runnable() {

      @Override
      public void run() {
        view.clearCache(true);
        view.clearFormData();
        view.clearHistory();
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setNetworkAvailable(true);
        view.setWebChromeClient(new MyWebChromeClient());

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
      }
    });
  }

  Object injectJavascript(String toExecute, boolean isAsync, Object args) throws JSONException {
    String executeScript = AndroidAtoms.EXECUTE_SCRIPT.getValue();
    String window = "window;";
    toExecute =
        "var win_context; try{win_context= " + window + "}catch(e){"
            + "win_context=window;}with(win_context){" + toExecute + "}";
    String wrappedScript =
        "(function(){" + "var win; try{win=" + window + "}catch(e){win=window}"
            + "with(win){return (" + executeScript + ")(" + escapeAndQuote(toExecute) + ", [";
    if (args instanceof JSONArray) {
      wrappedScript += convertToJsArgs((JSONArray)args);
    } else {
      wrappedScript += convertToJsArgs(args);
    }
    wrappedScript += "], true)}})()";
    return executeJavascriptInWebView("alert('selendroid:'+" + wrappedScript + ")");
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


  public class MyWebChromeClient extends WebChromeClient {

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
        return super.onJsAlert(view, url, message, jsResult);
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
    serverInstrumentation.runOnUiThread(new Runnable() {
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
}
