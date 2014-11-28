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
package io.selendroid.server.model;

import android.app.Activity;
import android.util.DisplayMetrics;
import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.android.AndroidTouchScreen;
import io.selendroid.server.android.InstrumentedMotionSender;
import io.selendroid.server.android.internal.Dimension;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.common.exceptions.UnsupportedOperationException;
import io.selendroid.server.model.DefaultSelendroidDriver.NativeSearchScope;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class SelendroidNativeDriver {
  public final String ACTIVITY_URL_PREFIX = "and-activity://";
  private ServerInstrumentation serverInstrumentation;
  private NativeSearchScope nativeSearchScope;
  private TouchScreen touch;

  public SelendroidNativeDriver(ServerInstrumentation serverInstrumentation,
      NativeSearchScope nativeSearchScope) {
    this.serverInstrumentation = serverInstrumentation;
    this.nativeSearchScope = nativeSearchScope;
    this.touch =
        new AndroidTouchScreen(serverInstrumentation, new InstrumentedMotionSender(
            serverInstrumentation));
  }

  public TouchScreen getTouch() {
    return touch;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#getCurrentUrl()
   */
  public String getCurrentUrl() {
    Activity activity = serverInstrumentation.getCurrentActivity();
    if (activity == null) {
      return null;
    }
    return "and-activity://" + activity.getLocalClassName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#getSourceOfCurrentActivity()
   */
  public JSONObject getWindowSource() throws JSONException {
    JSONObject rootElement = nativeSearchScope.getElementTree();

    return rootElement;
  }

  public String getTitle() {
    throw new UnsupportedOperationException(
        "Get title is not supported for SelendroidNativeDriver.");
  }

  private URI getCurrentURI() {
    String currentActivityUrl = getCurrentUrl();
    if (currentActivityUrl == null) {
      return null;
    }
    URI current;
    try {
      current = new URI(currentActivityUrl);
    } catch (URISyntaxException exception) {
      throw new IllegalArgumentException(exception);
    }
    return current;
  }

  public void get(String url) {
    URI dest;
    try {
      dest = new URI(url);
    } catch (URISyntaxException exception) {
      throw new IllegalArgumentException(exception);
    }

    if (!"and-activity".equals(dest.getScheme())) {
      throw new SelendroidException("Unrecognized scheme in URI: " + dest.toString());
    } else if (dest.getPath() != null && !dest.getPath().equals("")) {
      throw new SelendroidException("Unrecognized path in URI: " + dest.toString());
    }

    URI currentUri = getCurrentURI();

    if (currentUri != null && dest.getAuthority().endsWith(currentUri.getAuthority())) {
      // ignore request, activity is already open
      return;
    }

    serverInstrumentation.startActivity(dest.getAuthority());
    DefaultSelendroidDriver.sleepQuietly(500);
  }

  public Dimension getWindowSize() {
    DisplayMetrics metrics = new DisplayMetrics();
    serverInstrumentation
        .getCurrentActivity()
        .getWindowManager()
        .getDefaultDisplay()
        .getMetrics(metrics);

    return new Dimension(metrics.widthPixels, metrics.heightPixels);
  }

  public void forward() {
    throw new UnsupportedOperationException("Forward is not supported for SelendroidNativeDriver.");
  }

  public void refresh() {
    throw new UnsupportedOperationException("Refresh is not supported for SelendroidNativeDriver.");
  }
}
