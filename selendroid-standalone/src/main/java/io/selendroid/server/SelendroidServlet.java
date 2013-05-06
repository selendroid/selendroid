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

import io.selendroid.SelendroidConfiguration;
import io.selendroid.server.handler.CreateSessionHandler;
import io.selendroid.server.handler.DeleteSessionHandler;
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

  private SelendroidConfiguration configuration;
  private SelendroidDriver driver;

  public SelendroidServlet(SelendroidConfiguration configuration, SelendroidDriver driver) {
    this.configuration = configuration;
    this.driver = driver;
  }

  protected void init() {
    postHandler.put("/wd/hub/session", CreateSessionHandler.class);
    getHandler.put("/wd/hub/sessions", ListSessionsHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId", DeleteSessionHandler.class);
    redirectHandler.put("/wd/hub/session/:sessionId", RequestRedirectHandler.class);
  }

  @Override
  public void handleRequest(HttpRequest request, HttpResponse response, BaseRequestHandler handler) {
    if (handler == null) {
      replyWithServerError(response);
      return;
    }

    Response result;
    try {
      result = handler.handle();
    } catch (JSONException e) {
      replyWithServerError(response);
      return;
    }
    if (isNewSessionRequest(request)) {
      response.status(301);
      String session = result.getSessionId();

      String newSessionUri = "http://" + request.header("Host") + request.uri() + "/" + session;
      System.out.println("new Session URL: " + newSessionUri);
      response.header("location", newSessionUri);
    } else {
      response.status(200);
    }

  }
}
