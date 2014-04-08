/*
 * Copyright 2014 eBay Software Foundation and selendroid committers.
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

import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.Session;
import io.selendroid.util.SelendroidLogger;

import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

/**
 * Allow a command to be configured during runtime.
 */
public class SetCommandConfiguration extends RequestHandler {

  public SetCommandConfiguration(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handle(HttpRequest request) throws JSONException {
    SelendroidLogger.info("set command configuration command");
    JSONObject payload = getPayload(request);
    Session session = getSelendroidDriver(request).getSession();
    String command = payload.getString("command");
    payload.remove("command");
    session.setCommandConfiguration(command, payload);

    return new SelendroidResponse(getSessionId(request), "");
  }
}
