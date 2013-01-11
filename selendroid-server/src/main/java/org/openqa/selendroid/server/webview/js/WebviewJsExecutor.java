/*
 * Copyright 2010 Selenium committers
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

package org.openqa.selendroid.server.webview.js;

import android.webkit.WebView;

/**
 * Class that wraps synchronization housekeeping of execution of JavaScript code within WebView.
 */
public class WebviewJsExecutor {
  private static JavascriptResultNotifier resNotifier;

  /**
   * CAUTION: DOES PROBABLY NOT WORK WITH ANDROID 2.3.3:
   * 
   * @see http://www.jasonshah.com/handling-android-2-3-webviews-broken-addjavascriptinterface/
   */
  public static void executeJs(final WebView webview, JavascriptResultNotifier notifier,
      final String jsCode) {
    resNotifier = notifier;
    if (webview.getUrl() == null) {
      return;
    }
    webview.loadUrl("javascript:" + jsCode);
  }

  /**
   * Callback to report results of JavaScript code execution.
   * 
   * @param result Results (if returned) or an empty string.
   */
  //@JavascriptInterface
  public void resultAvailable(String result) {
    System.out.println("real result: " + result);
    resNotifier.notifyResultReady(result);
  }
}
