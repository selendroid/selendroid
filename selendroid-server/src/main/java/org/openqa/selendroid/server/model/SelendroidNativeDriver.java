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

import android.app.Activity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.DefaultSelendroidDriver.NativeSearchScope;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

public class SelendroidNativeDriver {
  public final String ACTIVITY_URL_PREFIX = "and-activity://";
  private ServerInstrumentation serverInstrumentation;
  private NativeSearchScope nativeSearchScope;

  public SelendroidNativeDriver(ServerInstrumentation serverInstrumentation,
      NativeSearchScope nativeSearchScope) {
    this.serverInstrumentation = serverInstrumentation;
    this.nativeSearchScope = nativeSearchScope;
  }

  private void addChildren(JSONObject parent, AndroidElement parentElement) throws JSONException {
    Collection<AndroidElement> children = parentElement.getChildren();
    if (children == null || children.isEmpty()) {
      return;
    }
    JSONArray childs = new JSONArray();
    for (AndroidElement child : children) {
      if (((AndroidNativeElement) child).getView() != ((AndroidNativeElement) parentElement)
          .getView()) {
        JSONObject jsonChild = ((AndroidNativeElement) child).toJson();
        childs.put(jsonChild);

        addChildren(jsonChild, child);
      }
    }
    parent.put("children", childs);
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
    AndroidNativeElement rootElement = nativeSearchScope.getElementTree();
    JSONObject root = rootElement.toJson();
    if (root == null) {
      return new JSONObject();
    }
    root.put("activity", serverInstrumentation.getCurrentActivity().getComponentName()
        .toShortString());

    addChildren(root, rootElement);

    return root;
  }

  public String getTitle() {
    throw new UnsupportedOperationException();
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

    Class<?> clazz;
    try {
      clazz = Class.forName(dest.getAuthority());
    } catch (ClassNotFoundException exception) {
      exception.printStackTrace();
      throw new SelendroidException("The specified Activity class does not exist: "
          + dest.getAuthority(), exception);
    }

    serverInstrumentation.startActivity(clazz);
    DefaultSelendroidDriver.sleepQuietly(500);
  }
}
