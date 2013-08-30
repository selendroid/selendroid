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
package io.selendroid.server;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public abstract class BaseServlet implements HttpHandler {
  public static final String SESSION_ID_KEY = "SESSION_ID_KEY";
  public static final String ELEMENT_ID_KEY = "ELEMENT_ID_KEY";
  public static final String NAME_ID_KEY = "NAME_ID_KEY";
  public static final String DRIVER_KEY = "DRIVER_KEY";
  public static final int INTERNAL_SERVER_ERROR = 500;

  protected Map<String, Class<? extends BaseRequestHandler>> getHandler =
      new HashMap<String, Class<? extends BaseRequestHandler>>();
  protected Map<String, Class<? extends BaseRequestHandler>> postHandler =
      new HashMap<String, Class<? extends BaseRequestHandler>>();
  protected Map<String, Class<? extends BaseRequestHandler>> deleteHandler =
      new HashMap<String, Class<? extends BaseRequestHandler>>();

  protected BaseRequestHandler findMatcher(HttpRequest request, HttpResponse response,
      Map<String, Class<? extends BaseRequestHandler>> handler) {
    for (Map.Entry<String, Class<? extends BaseRequestHandler>> entry : handler.entrySet()) {
      if (isFor(entry.getKey(), request.uri())) {
        return instantiateHandler(entry, request);
      }
    }
    return null;
  }

  /**
   * adds all the handlers to this registry: {@link #getHandler}, {@link #postHandler},
   * {@link #deleteHandler}
   */
  protected abstract void init();

  protected BaseRequestHandler instantiateHandler(
      Map.Entry<String, Class<? extends BaseRequestHandler>> entry, HttpRequest request) {
    BaseRequestHandler handler = null;
    try {
      Constructor<? extends BaseRequestHandler> handlerConstr =
          entry.getValue().getConstructor(HttpRequest.class, String.class);
      handler = handlerConstr.newInstance(request, entry.getKey());
    } catch (Exception e) {
      System.out.println("Error occured while creating handler: " + e);
    }

    return handler;
  }

  @Override
  public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control)
      throws Exception {
    BaseRequestHandler handler = null;
    if ("GET".equals(request.method())) {
      handler = findMatcher(request, response, getHandler);
    } else if ("POST".equals(request.method())) {
      handler = findMatcher(request, response, postHandler);
    } else if ("DELETE".equals(request.method())) {
      handler = findMatcher(request, response, deleteHandler);
    }
    handleRequest(request, response, handler);
  }

  public abstract void handleRequest(HttpRequest request, HttpResponse response,
      BaseRequestHandler handler);

  protected String getParameter(String configuredUri, String actualUri, String param) {
    return getParameter(configuredUri, actualUri, param, true);
  }

  protected String getParameter(String configuredUri, String actualUri, String param,
      boolean sectionLengthValidation) {
    String[] configuredSections = configuredUri.split("/");
    String[] currentSections = actualUri.split("/");
    if (sectionLengthValidation == true) {
      if (configuredSections.length != currentSections.length) {
        return null;
      }
    }
    for (int i = 0; i < currentSections.length; i++) {
      if (configuredSections[i].contains(param)) {
        return currentSections[i];
      }
    }
    return null;
  }

  protected void replyWithServerError(HttpResponse response) {
    System.out.println("replyWithServerError 500");
    response.status(INTERNAL_SERVER_ERROR);
    response.end();
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

  protected boolean isNewSessionRequest(HttpRequest request) {
    if ("POST".equals(request.method()) && "/wd/hub/session".equals(request.uri())) {
      return true;
    }
    return false;
  }

  protected void handleResponse(HttpRequest request, HttpResponse response,
      SelendroidResponse result) {
    response.header("Content-Type", "application/json");
    response.charset(Charset.forName("UTF-8"));

    if (isNewSessionRequest(request) && result.getStatus() == 0) {
      response.status(301);
      String session = result.getSessionId();

      String newSessionUri = "http://" + request.header("Host") + request.uri() + "/" + session;
      System.out.println("new Session URL: " + newSessionUri);
      response.header("location", newSessionUri);
    } else {
      response.status(200);
    }

    if (result != null) {
      String resultString = result.render();
      response.content(resultString);
    }
    response.end();
  }
}
