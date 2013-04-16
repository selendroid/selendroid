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

import java.util.ArrayList;

import org.openqa.selendroid.testapp.server.HttpServer;
import org.openqa.selendroid.testapp.webdrivertestserver.AppServer;
import org.openqa.selendroid.testapp.webdrivertestserver.Pages;
import org.openqa.selendroid.testapp.webdrivertestserver.WebbitAppServer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.common.base.Throwables;

public class WebViewActivity extends Activity {
  private HttpServer server = null;
  private WebView mainWebView = null;
  private Spinner testDataSpinner = null;
  private Pages webdriverTestPages = null;
  private ArrayAdapter<SpinnerItem> arrayAdapter = null;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    server = HttpServer.getInstance();
    // currently not used
    // serverThread = new HttpdThread();
    // serverThread.start();
    // webdriverTestPages = new Pages(serverThread.getServer());
    super.onCreate(savedInstanceState);
    setContentView(R.layout.webview);


    mainWebView = (WebView) findViewById(R.id.mainWebView);
    mainWebView.setWebViewClient(new MyWebViewClient());
    testDataSpinner = (Spinner) findViewById(R.id.spinner_webdriver_test_data);
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
    arrayAdapter.add(new SpinnerItem("about:blank", "about:blank"));

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

  // @Override
  // protected void onDestroy() {
  // if (server != null) {
  // server.stop();
  // try {
  // server.waitUntilShutdown();
  // } catch (InterruptedException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // }
  // super.onDestroy();
  // }

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
  private class HttpdThread extends Thread {
    private final AppServer server;
    private Looper looper;

    public HttpdThread() {

      server = new WebbitAppServer();

    }

    @Override
    public void run() {
      Looper.prepare();
      looper = Looper.myLooper();
      server.start();
      Looper.loop();
    }

    public AppServer getServer() {
      return server;
    }

    public void stopLooping() {
      if (looper == null) {
        return;
      }
      looper.quit();
    }
  }
  private class MyWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      view.loadUrl(url);
      return true;
    }
  }
}
