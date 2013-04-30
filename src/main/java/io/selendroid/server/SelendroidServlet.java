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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class SelendroidServlet implements HttpHandler {
  public static final int INTERNAL_SERVER_ERROR = 500;
  protected Map<String, Class<? extends RequestHandler>> getHandler =
      new HashMap<String, Class<? extends RequestHandler>>();
  protected Map<String, Class<? extends RequestHandler>> postHandler =
      new HashMap<String, Class<? extends RequestHandler>>();
  protected Map<String, Class<? extends RequestHandler>> deleteHandler =
      new HashMap<String, Class<? extends RequestHandler>>();
  protected Map<String, Class<? extends RequestHandler>> redirectHandler =
      new HashMap<String, Class<? extends RequestHandler>>();

  private SelendroidConfiguration configuration;

  public SelendroidServlet(SelendroidConfiguration configuration) {
    this.configuration = configuration;
  }

  protected void init() {
    postHandler.put("/wd/hub/session", CreateSessionHandler.class);
    getHandler.put("/wd/hub/sessions", ListSessionsHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId", DeleteSessionHandler.class);
    redirectHandler.put("/wd/hub/session/:sessionId", RequestRedirectHandler.class);
  }

  @Override
  public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control)
      throws Exception {
    RequestHandler handler = null;
    if ("GET".equals(request.method())) {
      handler = findMatcher(request, response, getHandler);
    } else if ("POST".equals(request.method())) {
      handler = findMatcher(request, response, postHandler);
    } else if ("DELETE".equals(request.method())) {
      handler = findMatcher(request, response, deleteHandler);
    }
    if (handler == null) {
      replyWithServerError(response);
      return;
    }
    Response result = handler.handle();
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

  private boolean isNewSessionRequest(HttpRequest request) {
    if ("POST".equals(request.method()) && "/wd/hub/session".equals(request.uri())) {
      return true;
    }
    return false;
  }

  protected void replyWithServerError(HttpResponse response) {
    System.out.println("replyWithServerError 500");
    response.status(INTERNAL_SERVER_ERROR);
    response.end();
  }

  protected RequestHandler findMatcher(HttpRequest request, HttpResponse response,
      Map<String, Class<? extends RequestHandler>> handler) {
    for (Map.Entry<String, Class<? extends RequestHandler>> entry : handler.entrySet()) {
      if (isFor(entry.getKey(), request.uri())) {
        return instantiateHandler(entry, request);
      }
    }
    return null;
  }

  protected RequestHandler instantiateHandler(
      Map.Entry<String, Class<? extends RequestHandler>> entry, HttpRequest request) {
    RequestHandler handler = null;
    try {
      Constructor<? extends RequestHandler> handlerConstr =
          entry.getValue().getConstructor(HttpRequest.class, String.class);
      handler = handlerConstr.newInstance(request, entry.getKey());
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error occured while creating handler: " + e);
    }

    return handler;
  }

  protected boolean isFor(String mapperUrl, String urlToMatch) {
    String[] sections = mapperUrl.split("/");
    if (urlToMatch == null) {
      return sections.length == 0;
    }
    if (urlToMatch.contains("?")) {
      urlToMatch = urlToMatch.substring(0, urlToMatch.indexOf("?"));
    }
    String[] allParts = urlToMatch.split("/");
    if (sections.length != allParts.length) {
      return false;
    }
    for (int i = 0; i < sections.length; i++) {
      // to work around a but in Selenium Grid 2.31.0
      String sectionElement = sections[i].replaceAll("\\?.*", "");
      if (!(sectionElement.startsWith(":") || sectionElement.equals(allParts[i]))) {
        return false;
      }
    }

    return true;
  }

}
