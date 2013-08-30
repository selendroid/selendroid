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
package io.selendroid.server.handler;

import io.selendroid.server.BaseSelendroidServerHandler;
import io.selendroid.server.Response;
import io.selendroid.server.model.ActiveSession;

import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.selendroid.server.SelendroidResponse;
import org.webbitserver.HttpRequest;

public class ListSessionsHandler extends BaseSelendroidServerHandler {
  private static final Logger log = Logger.getLogger(ListSessionsHandler.class.getName());

  public ListSessionsHandler(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    log.info("list sessions command");
    JSONArray sessions = new JSONArray();
    List<ActiveSession> activeSessions = getSelendroidDriver().getActiceSessions();
    if (activeSessions != null && activeSessions.isEmpty() == false) {
      for (ActiveSession session : activeSessions) {
        JSONObject sessionResponse = new JSONObject();
        sessionResponse.put("id", session.getSessionKey());
        sessionResponse.put("capabilities",
            new JSONObject(session.getDesiredCapabilities().asMap()));
        sessions.put(sessionResponse);
      }
    }
    return new SelendroidResponse(null, sessions);

  }

}
