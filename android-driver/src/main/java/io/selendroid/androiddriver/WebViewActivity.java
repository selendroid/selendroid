/*
 * Copyright 2013-2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.androiddriver;

import android.app.Activity;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web_view);
    WebView webview = (WebView) findViewById(R.id.webview);
    WebSettings settings = webview.getSettings();
    // viewport meta tag support
    settings.setUseWideViewPort(true);
    settings.setLoadWithOverviewMode(true);

    webview.setWebViewClient(new AndroidDriverClient());
    webview.loadData("<html><body><h1 id='AndroidDriver'>Android driver webview app</h1>" +
        "</body></html>", "text/html", "UTF-8");
  }

  private class AndroidDriverClient extends WebViewClient {
    @Override
    public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
      handler.proceed();
    }
  }


}
