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
package org.openqa.selendroid.server.handler;

import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.webbitserver.HttpRequest;

import com.google.gson.JsonObject;

public class GetStatus extends RequestHandler {
  public GetStatus(HttpRequest req) {
    super(req);
  }

  @Override
  public Response handle() {
    System.out.println("get Status command");
    JsonObject build = new JsonObject();
    build.addProperty("version", "0.1-snapshot");
    
    JsonObject os = new JsonObject();
    os.addProperty("arch", System.getProperty("os.arch"));
    os.addProperty("name", System.getProperty("os.name"));
    os.addProperty("version", System.getProperty("os.version"));

    JsonObject json = new JsonObject();
    json.add("build", build);
    json.add("os", os);

    return new Response(null, 200, json);
  }
}
