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
package org.openqa.selendroid.testapp;

import org.openqa.selendroid.testapp.server.HttpServer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {
  private static String TAG = "NativeAndroidDriver-demoapp";
  private HttpServer server = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    server = HttpServer.getInstance();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.webview);

    WebView mainWebView = (WebView) findViewById(R.id.mainWebView);
    WebSettings webSettings = mainWebView.getSettings();
    webSettings.setJavaScriptEnabled(true);

    mainWebView.setWebViewClient(new MyCustomWebViewClient());
    mainWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    mainWebView.loadUrl("http://localhost:4450");
  }

  public void runAtoms(final String scriptSrc, final WebView webView) {
    webView.post(new Runnable() {
      @Override
      public void run() {
        webView.loadUrl("javascript:" + scriptSrc);
      }
    });
  }

  public void showHomeScreenDialog(View view) {
    Intent nextScreen = new Intent(getApplicationContext(), HomeScreenActivity.class);
    startActivity(nextScreen);
  }

  private class MyCustomWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      view.loadUrl(url);
      return true;
    }
  }

  @Override
  protected void onDestroy() {
    server.stop();
    super.onDestroy();
  }
}
