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

import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.android.ViewHierarchyAnalyzer;
import org.openqa.selendroid.android.internal.DomWindow;
import org.openqa.selendroid.server.Session;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.js.AndroidAtoms;
import org.openqa.selendroid.util.SelendroidLogger;

import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SelendroidWebDriver extends AbstractSelendroidDriver {
  private static final String ELEMENT_KEY = "ELEMENT";
  private static final long FOCUS_TIMEOUT = 1000L;
  private static final long LOADING_TIMEOUT = 30000L;
  private static final long POLLING_INTERVAL = 50L;
  private static final long START_LOADING_TIMEOUT = 700L;
  static final long UI_TIMEOUT = 3000L;
  private volatile boolean pageDoneLoading;
  private volatile boolean pageStartedLoading;
  private volatile String result;
  private volatile boolean resultReady;
  private volatile WebView webview = null;
  private static final String WINDOW_KEY = "WINDOW";
  private volatile boolean editAreaHasFocus;

  public SelendroidWebDriver(Session session) {
    super.session = session;
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
  private String convertToJsArgs(Object... args) {
    StringBuilder toReturn = new StringBuilder();

    int length = args.length;
    for (int i = 0; i < length; i++) {
      toReturn.append((i > 0) ? "," : "");
      if (args[i] instanceof List<?>) {
        toReturn.append("[");
        List<Object> aList = (List<Object>) args[i];
        for (int j = 0; j < aList.size(); j++) {
          String comma = ((j == 0) ? "" : ",");
          toReturn.append(comma + convertToJsArgs(aList.get(j)));
        }
        toReturn.append("]");
      } else if (args[i] instanceof Map<?, ?>) {
        Map<Object, Object> aMap = (Map<Object, Object>) args[i];
        String toAdd = "{";
        for (Object key : aMap.keySet()) {
          toAdd += key + ":" + convertToJsArgs(aMap.get(key)) + ",";
        }
        toReturn.append(toAdd.substring(0, toAdd.length() - 1) + "}");
      } else if (args[i] instanceof AndroidWebElement) {
        // A WebElement is represented in JavaScript by an Object as
        // follow: {"ELEMENT":"id"} where "id" refers to the id
        // of the HTML element in the javascript cache that can
        // be accessed throught bot.inject.cache.getCache_()
        toReturn.append("{\"" + ELEMENT_KEY + "\":\"" + ((AndroidWebElement) args[i]).getId()
            + "\"}");
      } else if (args[i] instanceof DomWindow) {
        // A DomWindow is represented in JavaScript by an Object as
        // follow {"WINDOW":"id"} where "id" refers to the id of the
        // DOM window in the cache.
        toReturn.append("{\"" + WINDOW_KEY + "\":\"" + ((DomWindow) args[i]).getKey() + "\"}");
      } else if (args[i] instanceof Number || args[i] instanceof Boolean) {
        toReturn.append(String.valueOf(args[i]));
      } else if (args[i] instanceof String) {
        toReturn.append(escapeAndQuote((String) args[i]));
      }
    }
    SelendroidLogger.log("convertToJsArgs: " + toReturn.toString());
    return toReturn.toString();
  }

  public Object executeAtom(AndroidAtoms atom, Object... args) {
    final String myScript = atom.getValue();
    String scriptInWindow =
        "(function(){ " + " var win; try{win=window;}catch(e){win=window;}" + "with(win){return ("
            + myScript + ")(" + convertToJsArgs(args) + ")}})()";
    String jsResult = executeJavascriptInWebView("alert('selendroid:'+" + scriptInWindow + ")");


    SelendroidLogger.log("jsResult: " + jsResult);
    if (jsResult == null || "undefined".equals(jsResult)) {
      return null;
    }


    JsonObject json = new JsonParser().parse(jsResult).getAsJsonObject();

    if (0 != json.get("status").getAsInt()) {
      // TODO improve error handling
      throw new SelendroidException("Result status != 0");
    }
    JsonElement value = json.get("value");
    if (value.isJsonObject()) {
      return value.getAsJsonObject();
    }
    return value;
  }

  private String executeJavascriptInWebView(final String script) {
    result = null;
    resultReady = false;
    ServerInstrumentation.getInstance().runOnUiThread(new Runnable() {
      public void run() {
        if (webview.getUrl() == null) {
          return;
        }
        webview.loadUrl("javascript:" + script);
      }
    });
    long timeout =
        System.currentTimeMillis() + serverInstrumentation.getAndroidWait().getTimeoutInMillis();
    synchronized (syncObject) {
      while (!resultReady && (System.currentTimeMillis() < timeout)) {
        try {
          syncObject.wait(2000);
        } catch (InterruptedException e) {
          throw new SelendroidException(e);
        }
      }

      return result;
    }
  }

  public Object executeScript(String script, Object... args) {
    return injectJavascript(script, false, args);
  }

  @Override
  public String getCurrentUrl() {
    if (webview == null) {
      throw new SelendroidException("No open web view.");
    }
    long end = System.currentTimeMillis() + UI_TIMEOUT;
    final String[] url = new String[1];
    done = false;
    serverInstrumentation.runOnUiThread(new Runnable() {
      public void run() {
        synchronized (syncObject) {
          url[0] = webview.getUrl();
          done = true;
          syncObject.notify();
        }
      }
    });
    waitForDone(end, UI_TIMEOUT, "Failed to get url");
    return url[0];
  }


  @Override
  public void get(final String url) {
    serverInstrumentation.runOnUiThread(new Runnable() {
      public void run() {
        webview.loadUrl(url);
      }
    });
  }

  @Override
  public Object getWindowSource() {
    JsonObject source =
        new JsonParser()
            .parse(
                (String) executeScript("return (new XMLSerializer()).serializeToString(document.documentElement);"))
            .getAsJsonObject();
    return source.get("value").getAsString();
  }

  protected void init() {
    System.out.println("webdriver init");
    long start = System.currentTimeMillis();
    webview = ViewHierarchyAnalyzer.getDefaultInstance().findWebView();
    while (webview == null
        && (System.currentTimeMillis() - start <= ServerInstrumentation.getInstance()
            .getAndroidWait().getTimeoutInMillis())) {
      sleepQuietly(500);
      webview = ViewHierarchyAnalyzer.getDefaultInstance().findWebView();
    }

    if (webview == null) {
      throw new SelendroidException("No webview found on current activity.");
    }
    configureWebView(webview);

    webviewSearchScope = new WebviewSearchScope(session.getKnownElements(), webview, this);
  }

  private void configureWebView(final WebView view) {
    System.out.println("Configuring webview");
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

  Object injectJavascript(String toExecute, boolean isAsync, Object... args) {
    String executeScript = AndroidAtoms.EXECUTE_SCRIPT.getValue();
    String window = "window;";
    toExecute =
        "var win_context; try{win_context= " + window + "}catch(e){"
            + "win_context=window;}with(win_context){" + toExecute + "}";
    String wrappedScript =
        "(function(){" + "var win; try{win=" + window + "}catch(e){win=window}"
            + "with(win){return (" + executeScript + ")(" + escapeAndQuote(toExecute) + ", ["
            + convertToJsArgs(args) + "], true)}})()";
    return executeJavascriptInWebView("alert('selendroid:'+" + wrappedScript + ")");
  }

  public AndroidWebElement newAndroidElement(String id) {
    return webviewSearchScope.newAndroidWebElementById(id);
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
          result = message.replaceFirst("selendroid:", "");;
          resultReady = true;
          syncObject.notify();
        }

        return true;
      } else {
        return super.onJsAlert(view, url, message, jsResult);
      }
    }
  }


  @Override
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
}
