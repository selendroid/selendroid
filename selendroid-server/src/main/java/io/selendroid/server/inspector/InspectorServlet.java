/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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

import io.selendroid.ServerInstrumentation;
import io.selendroid.server.inspector.view.InspectorView;
import io.selendroid.server.inspector.view.ResourceView;
import io.selendroid.server.inspector.view.TreeView;
import io.selendroid.server.inspector.view.WebViewContentView;
import io.selendroid.server.model.SelendroidDriver;

import java.nio.charset.Charset;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class InspectorServlet implements HttpHandler {
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
  public void handleHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse,
      HttpControl httpControl) throws Exception {
    System.out.println("inspector uri: " + httpRequest.uri());
    if (httpRequest.uri().startsWith(INSPECTOR)) {
      if (httpRequest.uri().equals(INSPECTOR) || httpRequest.uri().equals(INSPECTOR + "/")) {
        httpResponse.status(301);
        String session = null;
        if (driver.getSession() != null) {
          session = driver.getSession().getSessionId();
          String divider = httpRequest.uri().endsWith("/") ? "" : "/";
          String newSessionUri =
              "http://" + httpRequest.header("Host") + httpRequest.uri() + divider + "session/"
                  + session + "/";
          System.out.println("new inspector URL: " + newSessionUri);
          httpResponse.header("Location", newSessionUri).end();
        } else {
          httpResponse.header("Content-Type", "text/html").charset(Charset.forName("UTF-8"))
              .status(200)
              .content("Selendroid Inspector can only be used with an active test session.").end();
        }
      } else if (httpRequest.uri().startsWith(INSPECTOR + "/session/")) {
        inspectorView.render(httpRequest, httpResponse);
      } else if (httpRequest.uri().equals(INSPECTOR + "/tree")) {
        treeView.render(httpRequest, httpResponse);
      } else if (httpRequest.uri().startsWith(INSPECTOR_RESSOURCE)) {
        resourceView.render(httpRequest, httpResponse);
      } else if (httpRequest.uri().equals(INSPECTOR + "/latestWebView")) {
        webViewContentView.render(httpRequest, httpResponse);
      }
    } else {
      httpControl.nextHandler();
    }
  }
}
