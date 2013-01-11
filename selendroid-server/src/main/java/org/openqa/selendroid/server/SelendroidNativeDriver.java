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

package org.openqa.selendroid.server;

import java.util.Collection;

import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.server.model.AndroidNativeElement;

import android.app.Activity;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SelendroidNativeDriver extends AbstractSelendroidDriver {


  public SelendroidNativeDriver(ServerInstrumentation serverInstrumentation) {
    super.serverInstrumentation = serverInstrumentation;
  }

  private void addChildren(JsonObject parent, AndroidElement parentElement) {
    Collection<AndroidElement> children = parentElement.getChildren();
    if (children == null || children.isEmpty()) {
      return;
    }
    JsonArray childs = new JsonArray();
    for (AndroidElement child : children) {
      if (((AndroidNativeElement) child).getView().getId() != ((AndroidNativeElement) parentElement)
          .getView().getId() && ((AndroidNativeElement) child).getView().getId() != View.NO_ID) {
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
    AndroidNativeElement rootElement = ((NativeSearchScope) nativeSearchScope).getElementTree();
    JsonObject root = rootElement.toJson();
    root.addProperty("activity", serverInstrumentation.getCurrentActivity().getComponentName()
        .toShortString());
    addChildren(root, rootElement);

    return root;
  }
}
