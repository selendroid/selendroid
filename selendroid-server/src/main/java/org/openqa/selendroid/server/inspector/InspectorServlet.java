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
package org.openqa.selendroid.server.inspector;

import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.server.inspector.view.InspectorView;
import org.openqa.selendroid.server.inspector.view.ResourceView;
import org.openqa.selendroid.server.inspector.view.TreeView;
import org.openqa.selendroid.server.model.SelendroidDriver;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class InspectorServlet implements HttpHandler {
  private InspectorView inspectorView = null;
  private ResourceView resourceView = null;
  private TreeView treeView = null;
  public static final String INSPECTOR = "/inspector";
  public static final String INSPECTOR_RESSOURCE = INSPECTOR + "/resources";

  public InspectorServlet(SelendroidDriver driver, ServerInstrumentation instrumentation) {
    this.inspectorView = new InspectorView(instrumentation, driver);
    this.resourceView = new ResourceView(instrumentation, driver);
    this.treeView = new TreeView(instrumentation, driver);
  }

  @Override
  public void handleHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse,
      HttpControl httpControl) throws Exception {

    if (httpRequest.uri().startsWith(INSPECTOR)) {
      if (httpRequest.uri().equals(INSPECTOR)) {
        inspectorView.render(httpRequest, httpResponse);
      } else if (httpRequest.uri().equals(INSPECTOR + "/tree")) {
        treeView.render(httpRequest, httpResponse);
      } else if (httpRequest.uri().startsWith(INSPECTOR_RESSOURCE)) {
        resourceView.render(httpRequest, httpResponse);
      }
    } else {
      httpControl.nextHandler();
    }
  }
}
