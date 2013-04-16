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
package org.openqa.selendroid.server.inspector.view;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.inspector.SelendroidInspectorView;
import org.openqa.selendroid.server.inspector.TreeUtil;
import org.openqa.selendroid.server.model.SelendroidDriver;
import org.openqa.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.nio.charset.Charset;

public class TreeView extends SelendroidInspectorView {
  public TreeView(ServerInstrumentation serverInstrumentation, SelendroidDriver driver) {
    super(serverInstrumentation, driver);
  }

  public void render(HttpRequest request, HttpResponse response) throws JSONException {
    JSONObject source = null;
    try {
      source = (JSONObject) driver.getWindowSource();
    } catch (SelendroidException e) {
      SelendroidLogger.log("error getting WindowSource in TreeView", e);
      response.header("Content-type", "application/x-javascript").charset(Charset.forName("UTF-8"))
          .content("{}").end();
      return;
    }
    String convertedTree = TreeUtil.createFromNativeWindowsSource(source).toString();
    response.header("Content-type", "application/x-javascript").charset(Charset.forName("UTF-8"))
        .content(convertedTree).end();
  }
}
