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

import java.util.Locale;

import org.json.JSONObject;
import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;


public class StatusServlet implements HttpHandler {
  private ServerInstrumentation serverInstrumentation;

  public StatusServlet(ServerInstrumentation serverInstrumentation) {
    this.serverInstrumentation = serverInstrumentation;
  }

  @Override
  public void handleHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse,
      HttpControl httpControl) throws Exception {
    if (!"GET".equals(httpRequest.method())) {
      httpResponse.status(500);
      httpResponse.end();
      return;
    }
    SelendroidLogger.log("get Status Servlet Called");
    JSONObject build = new JSONObject();
    build.put("version", serverInstrumentation.getSelendroidVersionNumber());
    build.put("browserName", "selendroid");

    JSONObject os = new JSONObject();
    os.put("arch", android.os.Build.CPU_ABI);
    os.put("name", "Android");
    os.put("version", android.os.Build.VERSION.SDK_INT);
    os.put("locale", Locale.getDefault().toString());

    JSONObject json = new JSONObject();
    json.put("build", build);
    json.put("os", os);
    httpResponse.header("Content-Type", "text/plain");
    httpResponse.content("{status: 0, value: " + json.toString() + "}");
    httpResponse.end();
  }
}
