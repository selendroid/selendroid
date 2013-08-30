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
import io.selendroid.server.SelendroidResponse;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

public class CreateSessionHandler extends BaseSelendroidServerHandler {
  private static final Logger log = Logger.getLogger(CreateSessionHandler.class.getName());

  public CreateSessionHandler(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    JSONObject payload = getPayload();
    log.info("new session command with capabilities: " + payload.toString(2));

    JSONObject desiredCapabilities = payload.getJSONObject("desiredCapabilities");

    String sessionID = null;
    try {
      sessionID = getSelendroidDriver().createNewTestSession(desiredCapabilities, 5);
    } catch (Exception e) {
      log.severe("Error while creating new session: " + e.getMessage());
      e.printStackTrace();
      return new SelendroidResponse("", 33, e);
    }
    return new SelendroidResponse(sessionID, 0, desiredCapabilities);
  }
}
