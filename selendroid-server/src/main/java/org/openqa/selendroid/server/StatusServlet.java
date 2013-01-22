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

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import com.google.gson.JsonObject;


public class StatusServlet implements HttpHandler {

  @Override
  public void handleHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse,
      HttpControl httpControl) throws Exception {
    if (!"GET".equals(httpRequest.method())) {
      httpResponse.status(500);
      httpResponse.end();
      return;
    }
    System.out.println("get Status Servlet Called");
    JsonObject build = new JsonObject();
    build.addProperty("version", "0.1-snapshot");

    JsonObject os = new JsonObject();
    os.addProperty("arch", System.getProperty("os.arch"));
    os.addProperty("name", System.getProperty("os.name"));
    os.addProperty("version", System.getProperty("os.version"));

    JsonObject json = new JsonObject();
    json.add("build", build);
    json.add("os", os);
    httpResponse.header("Content-Type", "text/plain");
    httpResponse.content("{status: 0, value: " + json.toString() + "}");
    httpResponse.end();
  }
}
