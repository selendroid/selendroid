/*
 * Copyright 2011 Selenium committers Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.selendroid.server.android;


import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.common.exceptions.SelendroidException;
import android.app.Activity;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.webkit.WebView;

public class WebViewMotionSender implements MotionSender {
  private ServerInstrumentation instrumentation;
  private final Object syncObject = new Object();
  private volatile boolean done;
  private final WebView webview;

  public WebViewMotionSender(WebView webView, ServerInstrumentation instrumentation) {
    this.webview = webView;
    this.instrumentation = instrumentation;
  }

  private void waitForNotification(long timeout, String errorMsg) {
    while (!done && (System.currentTimeMillis() < timeout)) {
      try {
        syncObject.wait(AndroidWait.DEFAULT_SLEEP_INTERVAL);
      } catch (InterruptedException e) {
        throw new SelendroidException(errorMsg, e);
      }
    }
  }

  @Override
  public boolean send(final Iterable<MotionEvent> events) {
    long timeout =
        System.currentTimeMillis() + instrumentation.getAndroidWait().getTimeoutInMillis();
    Activity activity = instrumentation.getCurrentActivity();
    synchronized (syncObject) {
      done = false;

      activity.runOnUiThread(new Runnable() {
        public void run() {
          float zoom = webview.getScale();
          for (MotionEvent event : events) {
            event.setLocation(zoom * event.getX(), zoom * event.getY());
            try {
              event.setSource(InputDevice.SOURCE_CLASS_POINTER);
            } catch (NoSuchMethodError e) {
              throw new SelendroidException("You are using an Android WebDriver APK "
                  + "for ICS SDKs or more recent SDK versions. For more info see "
                  + "http://code.google.com/p/selenium/wiki/AndroidDriver#Supported_Platforms.", e);
            }
            webview.dispatchTouchEvent(event);
            synchronized (syncObject) {
              done = true;
              syncObject.notify();
            }
          }
        }
      });
      waitForNotification(timeout, "Failed to send motion events.");
    }
    return true;
  }
}
