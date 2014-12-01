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
package io.selendroid.standalone.server.handler;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.selendroid.standalone.server.BaseSelendroidStandaloneHandler;
import org.json.JSONException;
import org.json.JSONObject;

import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.StatusCode;
import io.selendroid.server.common.http.HttpRequest;

public class CreateSessionHandler extends BaseSelendroidStandaloneHandler {
  private static final Logger log = Logger.getLogger(CreateSessionHandler.class.getName());

  public CreateSessionHandler(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handleRequest(HttpRequest request, JSONObject payload) throws JSONException {
    JSONObject desiredCapabilities = payload.getJSONObject("desiredCapabilities");
    try {
      String sessionID = getSelendroidDriver(request).createNewTestSession(desiredCapabilities);
      SelendroidCapabilities caps = getSelendroidDriver(request).getSessionCapabilities(sessionID);

      return new SelendroidResponse(sessionID, new JSONObject(caps.asMap()));
    } catch (Exception e) {
      log.log(Level.SEVERE, "Error while creating new session", e);
      return new SelendroidResponse("", StatusCode.SESSION_NOT_CREATED_EXCEPTION, e);
    }
  }
}
