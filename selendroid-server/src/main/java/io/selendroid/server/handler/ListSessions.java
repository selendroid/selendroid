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
package io.selendroid.server.handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.http.HttpRequest;

public class ListSessions extends SafeRequestHandler {

  public ListSessions(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    JSONArray sessions = new JSONArray();
    if (getSelendroidDriver(request).getSession() != null) {
      JSONObject sessionResponse = new JSONObject();
      sessionResponse.put("id", getSelendroidDriver(request).getSession().getSessionId());
      sessionResponse.put("capabilities", getSelendroidDriver(request).getSession().getCapabilities());
      sessions.put(sessionResponse);
    }
    return new SelendroidResponse(null, sessions);
  }
}
