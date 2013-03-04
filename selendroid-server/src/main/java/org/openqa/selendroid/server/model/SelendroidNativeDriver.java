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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.server.exceptions.SelendroidException;

import android.app.Activity;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SelendroidNativeDriver extends AbstractSelendroidDriver {
  public final String ACTIVITY_URL_PREFIX = "and-activity://";

  public SelendroidNativeDriver(ServerInstrumentation serverInstrumentation) {
    super(serverInstrumentation);
  }

  private void addChildren(JsonObject parent, AndroidElement parentElement) {
    Collection<AndroidElement> children = parentElement.getChildren();
    if (children == null || children.isEmpty()) {
      return;
    }
    JsonArray childs = new JsonArray();
    for (AndroidElement child : children) {
      if (((AndroidNativeElement) child).getView() != ((AndroidNativeElement) parentElement)
          .getView()) {
        JsonObject jsonChild = ((AndroidNativeElement) child).toJson();
        childs.add(jsonChild);

        addChildren(jsonChild, child);
      }
    }
    parent.add("children", childs);
  }


  /*
   * (non-Javadoc)
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#getCurrentUrl()
   */
  @Override
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
  @Override
  public JsonObject getWindowSource() {
    if (nativeSearchScope == null) {
      throw new SelendroidException("No active session found.");
    }
    AndroidNativeElement rootElement = ((NativeSearchScope) nativeSearchScope).getElementTree();
    JsonObject root = rootElement.toJson();
    if (root == null) {
      return new JsonObject();
    }
    root.addProperty("activity", serverInstrumentation.getCurrentActivity().getComponentName()
        .toShortString());
    addChildren(root, rootElement);

    return root;
  }

  @Override
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

  @Override
  public void get(String url) {
    URI dest;
    try {
      dest = new URI(url);
    } catch (URISyntaxException exception) {
      throw new IllegalArgumentException(exception);
    }

    if (!"and-activity".equals(dest.getScheme())) {
      throw new SelendroidException("Unrecognized scheme in URI: " + dest.toString());
    } else if (!Strings.isNullOrEmpty(dest.getPath())) {
      throw new SelendroidException("Unrecognized path in URI: " + dest.toString());
    }
    URI currentUri = getCurrentURI();
    if (currentUri != null && dest.getPath().contains(currentUri.getPath())) {
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
  }

  public ServerInstrumentation getServerInstrumentation() {
    return serverInstrumentation;
  }
}
