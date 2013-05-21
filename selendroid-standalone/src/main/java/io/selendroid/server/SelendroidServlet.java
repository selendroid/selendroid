/*
 * Copyright 2013 selendroid committers.
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
package io.selendroid.server;

import io.selendroid.server.handler.CreateSessionHandler;
import io.selendroid.server.handler.DeleteSessionHandler;
import io.selendroid.server.handler.GetCapabilities;
import io.selendroid.server.handler.ListSessionsHandler;
import io.selendroid.server.handler.RequestRedirectHandler;
import io.selendroid.server.model.SelendroidDriver;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.openqa.selendroid.server.BaseRequestHandler;
import org.openqa.selendroid.server.BaseServlet;
import org.openqa.selendroid.server.Response;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class SelendroidServlet extends BaseServlet {
  protected Map<String, Class<? extends BaseRequestHandler>> redirectHandler =
      new HashMap<String, Class<? extends BaseRequestHandler>>();
  private SelendroidDriver driver;

  public SelendroidServlet(SelendroidDriver driver) {
    this.driver = driver;
    init();
  }

  protected void init() {
    postHandler.put("/wd/hub/session", CreateSessionHandler.class);
    getHandler.put("/wd/hub/sessions", ListSessionsHandler.class);
    getHandler.put("/wd/hub/session/:sessionId", GetCapabilities.class);
    deleteHandler.put("/wd/hub/session/:sessionId", DeleteSessionHandler.class);
    redirectHandler.put("/wd/hub/session/", RequestRedirectHandler.class);
  }

  @Override
  public void handleRequest(HttpRequest request, HttpResponse response,
      BaseRequestHandler foundHandler) {
    BaseRequestHandler handler = null;
    if (foundHandler == null) {
      if (redirectHandler.isEmpty() == false) {
        // trying to find an redirect handler
        for (Map.Entry<String, Class<? extends BaseRequestHandler>> entry : redirectHandler
            .entrySet()) {
          if (request.uri().startsWith(entry.getKey())) {
            String sessionId =
                getParameter("/wd/hub/session/:sessionId", request.uri(), ":sessionId", false);
            if (driver.isValidSession(sessionId)) {
              handler = instantiateHandler(entry, request);
              request.data().put(SESSION_ID_KEY, sessionId);
            }
          }
        }
      }
      if (handler == null) {
        replyWithServerError(response);
        return;
      }
    } else {
      handler = foundHandler;
    }

    String sessionId = getParameter(handler.getMappedUri(), request.uri(), ":sessionId");
    if (sessionId != null) {
      request.data().put(SESSION_ID_KEY, sessionId);
    }
    request.data().put(DRIVER_KEY, driver);

    Response result;
    try {
      result = handler.handle();
    } catch (JSONException e) {
      replyWithServerError(response);
      return;
    }
    handleResponse(request, response, result);
  }
}
