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

import io.selendroid.testapp.server.HttpServer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class WebViewActivity extends Activity {
  private HttpServer server = null;
  private WebView mainWebView = null;
  private Spinner testDataSpinner = null;
  private ArrayAdapter<SpinnerItem> arrayAdapter = null;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    server = HttpServer.getInstance();

    setContentView(io.selendroid.testapp.R.layout.webview);

    mainWebView = (WebView) findViewById(io.selendroid.testapp.R.id.mainWebView);
    mainWebView.setWebViewClient(new WebViewClient());
    testDataSpinner =
        (Spinner) findViewById(io.selendroid.testapp.R.id.spinner_webdriver_test_data);
    arrayAdapter =
        new ArrayAdapter<SpinnerItem>(this, android.R.layout.simple_spinner_item,
            new ArrayList<SpinnerItem>());
    arrayAdapter.add(new SpinnerItem("'Say Hello'-Demo", "http://localhost:4450/"));
    arrayAdapter.add(new SpinnerItem("xhtmlTestPage", "file:///android_asset/web/xhtmlTest.html"));
    arrayAdapter.add(new SpinnerItem("formPage", "file:///android_asset/web/formPage.html"));
    arrayAdapter.add(new SpinnerItem("selectableItemsPage",
        "file:///android_asset/web/selectableItems.html"));
    arrayAdapter
        .add(new SpinnerItem("nestedPage", "file:///android_asset/web/nestedElements.html"));
    arrayAdapter.add(new SpinnerItem("javascriptPage",
        "file:///android_asset/web/javascriptPage.html"));
    arrayAdapter.add(new SpinnerItem("missedJsReferencePage",
        "file:///android_asset/web/missedJsReference.html"));
    arrayAdapter.add(new SpinnerItem("actualXhtmlPage",
        "file:///android_asset/web/actualXhtmlPage.xhtml"));
    arrayAdapter
        .add(new SpinnerItem("Click Source", "file:///android_asset/web/click_source.html"));
    arrayAdapter.add(new SpinnerItem("Clicks", "file:///android_asset/web/clicks.html"));
    arrayAdapter.add(new SpinnerItem("Long Content Page",
        "file:///android_asset/web/longContentPage.html"));
    arrayAdapter.add(new SpinnerItem("TestClickPage1",
        "file:///android_asset/web/test_click_page1.html"));
    arrayAdapter.add(new SpinnerItem("TestClickPage2",
        "file:///android_asset/web/test_click_page2.html"));
    arrayAdapter.add(new SpinnerItem("about:blank", "about:blank"));
    arrayAdapter.add(new SpinnerItem("iframes", "file:///android_asset/web/iframes.html"));

    testDataSpinner.setAdapter(arrayAdapter);
    testDataSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

      @Override
      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        SpinnerItem item = (SpinnerItem) testDataSpinner.getSelectedItem();
        mainWebView.loadUrl(item.url);
      }

      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
        SpinnerItem item = (SpinnerItem) testDataSpinner.getSelectedItem();
        mainWebView.loadUrl(item.url);
      }
    });
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onStart() {
    mainWebView.loadUrl("about:blank");
    super.onStart();
  }

  public void showHomeScreenDialog(View view) {
    Intent nextScreen = new Intent(getApplicationContext(), HomeScreenActivity.class);
    startActivity(nextScreen);
  }

  public class SpinnerItem {
    private String text;
    public String url;

    SpinnerItem(String text, String url) {
      this.text = text;
      this.url = url;
    }

    @Override
    public String toString() {
      return text;
    }
  }
}
