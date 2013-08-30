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
package io.selendroid.server;

import org.json.JSONArray;
import org.json.JSONObject;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;


public class StatusServlet implements HttpHandler {
  private ServerDetails seledendroidServer;
  private JSONArray apps = null;

  public StatusServlet(ServerDetails seledendroidServer) {
    this.seledendroidServer = seledendroidServer;
  }

  @Override
  public void handleHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse,
      HttpControl httpControl) throws Exception {
    if (!"GET".equals(httpRequest.method())) {
      httpResponse.status(500);
      httpResponse.end();
      return;
    }

    JSONObject build = new JSONObject();
    build.put("version", seledendroidServer.getServerVersion());
    build.put("browserName", "selendroid");

    JSONObject os = new JSONObject();
    os.put("arch", seledendroidServer.getCpuArch());
    os.put("name", seledendroidServer.getOsName());
    os.put("version", seledendroidServer.getOsVersion());

    JSONObject json = new JSONObject();
    json.put("build", build);
    json.put("os", os);

    JSONArray devices = null;
    try {
      devices = seledendroidServer.getSupportedDevices();
    } catch (Exception e) {
      devices = new JSONArray();
    }

    json.put("supportedDevices", devices);

    if (apps == null || devices.length() == 0) {
      try {
        apps = seledendroidServer.getSupportedApps();
      } catch (Exception e) {
        apps = new JSONArray();
      }
    }
    json.put("supportedApps", apps);

    // httpResponse.header("Content-Type", "text/plain");
    httpResponse.header("Content-Type", "application/json");

    httpResponse.content("{status: 0, value: " + json + "}");
    httpResponse.end();
  }
}
