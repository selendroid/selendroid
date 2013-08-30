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

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import io.selendroid.SelendroidCapabilities;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.SelendroidResponse;
import org.webbitserver.HttpRequest;

public class GetCapabilities extends BaseSelendroidServerHandler {
  private static final Logger log = Logger.getLogger(GetCapabilities.class.getName());

  public GetCapabilities(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    log.info("get capabilities command");
    String sessionId = getSessionId();

    SelendroidCapabilities caps = getSelendroidDriver().getSessionCapabilities(sessionId);
    if (caps == null) {
      return new SelendroidResponse(sessionId, 13, new SelendroidException("Session was not found"));
    }
    return new SelendroidResponse(sessionId, new JSONObject(caps.asMap()));
  }
}
