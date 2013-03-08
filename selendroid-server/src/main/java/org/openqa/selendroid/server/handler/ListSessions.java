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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ListSessions extends RequestHandler {

  public ListSessions(HttpRequest request,String mappedUri) {
    super(request,mappedUri);
  }

  @Override
  public Response handle() {

    JsonArray sessions = new JsonArray();
    if (getSelendroidDriver().getSession() != null) {
      JsonObject sessionResponse = new JsonObject();
      sessionResponse.addProperty("id", getSelendroidDriver().getSession().getSessionId());
      sessionResponse.add("capabilities", getSelendroidDriver().getSession().getCapabilities());
      sessions.add(sessionResponse);
    }
    return new Response(null, sessions);
  }
}
