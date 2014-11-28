/*
 * Copyright 2014 eBay Software Foundation and selendroid committers.
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

package io.selendroid.server.model.internal;

import android.webkit.WebView;
import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.android.ViewHierarchyAnalyzer;
import io.selendroid.server.android.WindowType;
import io.selendroid.server.common.exceptions.NoSuchContextException;
import io.selendroid.server.model.DefaultSelendroidDriver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebViewHandleMapper {

  public static final String HANDLE_SEPARATOR = "_";

  public static Set<String> webViewHandles() {
    Set<String> webviewHandles = new HashSet<String>();

    List<WebView> webviews = getWebViews();
    if (webviews != null) {
      for (int i = 0; i < webviews.size(); i++) {
        webviewHandles.add(WindowType.WEBVIEW.name() + HANDLE_SEPARATOR + i);
      }
    }

    return webviewHandles;
  }

  public static WebView getWebViewByHandle(String handle) {
    int index = Integer.valueOf(normalizeHandle(handle).split("_")[1]);
    List<WebView> webviews = getWebViews();

    return webviews.get(index);
  }

  public static String normalizeHandle(String unnormalizedHandle) {
    if (!unnormalizedHandle.startsWith(WindowType.WEBVIEW.name())) {
      throw new NoSuchContextException(unnormalizedHandle);
    }

    if (!unnormalizedHandle.contains("_")) {
      return WindowType.WEBVIEW.name() + "_0";
    }

    return unnormalizedHandle;
  }

  private static List<WebView> getWebViews() {
    long start = System.currentTimeMillis();
    List<WebView> webviews = ViewHierarchyAnalyzer.getDefaultInstance().findWebViews();

    // Retry logic (using Implicit Wait)
    while (webviews == null
            && (System.currentTimeMillis() - start <= ServerInstrumentation.getInstance()
            .getAndroidWait().getTimeoutInMillis())) {
      DefaultSelendroidDriver.sleepQuietly(500);
      webviews = ViewHierarchyAnalyzer.getDefaultInstance().findWebViews();
    }

    return webviews;
  }
}
