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
package io.selendroid.server.inspector;

import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.common.http.HttpResponse;
import io.selendroid.server.common.http.HttpServlet;
import io.selendroid.server.inspector.view.InspectorView;
import io.selendroid.server.inspector.view.ResourceView;
import io.selendroid.server.inspector.view.TreeView;
import io.selendroid.server.inspector.view.WebViewContentView;
import io.selendroid.server.model.SelendroidDriver;



public class InspectorServlet implements HttpServlet {
  private SelendroidDriver driver = null;
  private InspectorView inspectorView = null;
  private ResourceView resourceView = null;
  private TreeView treeView = null;
  private WebViewContentView webViewContentView = null;
  public static final String INSPECTOR = "/inspector";
  public static final String INSPECTOR_RESSOURCE = INSPECTOR + "/resources";

  public InspectorServlet(SelendroidDriver driver, ServerInstrumentation instrumentation) {
    this.driver = driver;
    this.inspectorView = new InspectorView(instrumentation, driver);
    this.resourceView = new ResourceView(instrumentation, driver);
    this.treeView = new TreeView(instrumentation, driver);
    this.webViewContentView = new WebViewContentView(instrumentation, driver);
  }

  @Override
  public void handleHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse)
      throws Exception {

    if (httpRequest.uri().startsWith(INSPECTOR)) {
      if (httpRequest.uri().equals(INSPECTOR) || httpRequest.uri().equals(INSPECTOR + "/")) {
        httpResponse.setStatus(301);
        String session = null;
        if (driver.getSession() != null) {
          session = driver.getSession().getSessionId();
          String divider = httpRequest.uri().endsWith("/") ? "" : "/";
          String newSessionUri =
              "http://" + httpRequest.header("Host") + httpRequest.uri() + divider + "session/"
                  + session + "/";

          httpResponse.sendRedirect(newSessionUri).end();
        } else {
          httpResponse.setContentType("text/html").setStatus(200)
              .setContent("Selendroid Inspector can only be used with an active test session.")
              .end();
        }
      } else if (httpRequest.uri().startsWith(INSPECTOR) && httpRequest.uri().endsWith("/tree")) {
        treeView.render(httpRequest, httpResponse);
      } else if (httpRequest.uri().startsWith(INSPECTOR + "/session/")) {
        inspectorView.render(httpRequest, httpResponse);
      } else if (httpRequest.uri().startsWith(INSPECTOR_RESSOURCE)) {
        resourceView.render(httpRequest, httpResponse);
      } else if (httpRequest.uri().equals(INSPECTOR + "/latestWebView")) {
        webViewContentView.render(httpRequest, httpResponse);
      }
    }
  }
}
