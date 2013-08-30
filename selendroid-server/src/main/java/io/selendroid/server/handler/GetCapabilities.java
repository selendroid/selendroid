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

import io.selendroid.server.RequestHandler;
import org.json.JSONException;
import org.json.JSONObject;

import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.Session;
import io.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

public class GetCapabilities extends RequestHandler {
  public GetCapabilities(HttpRequest request,String mappedUri) {
    super(request,mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    SelendroidLogger.log("get capabilities command");
    Session session = getSelendroidDriver().getSession();

    JSONObject caps = getSelendroidDriver().getSessionCapabilities(session.getSessionId());

    return new SelendroidResponse(session.getSessionId(), caps);
  }
}
