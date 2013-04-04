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
package org.openqa.selendroid.server.common;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class BaseServlet {
  public static final int INTERNAL_SERVER_ERROR = 500;

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
      SelendroidLogger.log("Error occured while creating handler: ", e);
    }

    return handler;
  }

  protected String getParameter(String configuredUri, String actualUri, String param) {
    String[] configuredSections = configuredUri.split("/");
    String[] currentSections = actualUri.split("/");
    if (configuredSections.length != currentSections.length) {
      return null;
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
}
