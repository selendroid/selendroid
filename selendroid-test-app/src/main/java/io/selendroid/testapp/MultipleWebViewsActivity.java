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
package io.selendroid.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MultipleWebViewsActivity extends Activity {
  private WebView webView1 = null;
  private WebView webView2 = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(io.selendroid.testapp.R.layout.multiple_webviews);


    webView1 = (WebView) findViewById(io.selendroid.testapp.R.id.webView1);
    webView1.setWebViewClient(new MyWebViewClient());
    webView2 = (WebView) findViewById(io.selendroid.testapp.R.id.webView2);
    webView2.setWebViewClient(new MyWebViewClient());
  }

  @Override
  protected void onStart() {
    webView1.loadUrl("file:///android_asset/multipleWebViews/file1.html");
    webView2.loadUrl("file:///android_asset/multipleWebViews/file2.html");

    super.onStart();
  }

  private class MyWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      view.loadUrl(url);
      return true;
    }
  }
}
