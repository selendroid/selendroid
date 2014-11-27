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
package io.selendroid.server.common;

import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.common.http.HttpResponse;
import io.selendroid.server.common.http.HttpServlet;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseServlet implements HttpServlet {
  public static final String SESSION_ID_KEY = "SESSION_ID_KEY";
  public static final String ELEMENT_ID_KEY = "ELEMENT_ID_KEY";
  public static final String COMMAND_NAME_KEY = "COMMAND_KEY";
  public static final String NAME_ID_KEY = "NAME_ID_KEY";
  public static final String DRIVER_KEY = "DRIVER_KEY";
  public static final int INTERNAL_SERVER_ERROR = 500;

  protected Map<String, BaseRequestHandler> getHandler = new HashMap<String, BaseRequestHandler>();
  protected Map<String, BaseRequestHandler> postHandler = new HashMap<String, BaseRequestHandler>();
  protected Map<String, BaseRequestHandler> deleteHandler =
      new HashMap<String, BaseRequestHandler>();

  private Map<String, String[]> mapperUrlSectionsCache = new HashMap<String, String[]>();

  protected BaseRequestHandler findMatcher(HttpRequest request,
      Map<String, BaseRequestHandler> handler) {
    String[] urlToMatchSections = getRequestUrlSections(request.uri());
    for (Map.Entry<String, ? extends BaseRequestHandler> entry : handler.entrySet()) {
      String[] mapperUrlSections = getMapperUrlSectionsCached(entry.getKey());
      if (isFor(mapperUrlSections, urlToMatchSections)) {
        return entry.getValue();
      }
    }
    return null;
  }

  /**
   * adds all the handlers to this registry: {@link #getHandler}, {@link #postHandler},
   * {@link #deleteHandler}
   */
  protected abstract void init();

  @Override
  public void handleHttpRequest(HttpRequest request, HttpResponse response) throws Exception {
    BaseRequestHandler handler = null;
    if ("GET".equals(request.method())) {
      handler = findMatcher(request, getHandler);
    } else if ("POST".equals(request.method())) {
      handler = findMatcher(request, postHandler);
    } else if ("DELETE".equals(request.method())) {
      handler = findMatcher(request, deleteHandler);
    }
    handleRequest(request, response, handler);
  }

  protected void register(Map<String, BaseRequestHandler> registerOn, BaseRequestHandler handler) {
    registerOn.put(handler.getMappedUri(), handler);
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
    if (sectionLengthValidation) {
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
    response.setStatus(INTERNAL_SERVER_ERROR);
    response.end();
  }

  protected boolean isFor(String[] mapperUrlSections, String[] urlToMatchSections) {
    if (urlToMatchSections == null) {
      return mapperUrlSections.length == 0;
    }
    if (mapperUrlSections.length != urlToMatchSections.length) {
      return false;
    }
    for (int i = 0; i < mapperUrlSections.length; i++) {
      if (!(mapperUrlSections[i].startsWith(":") || mapperUrlSections[i]
          .equals(urlToMatchSections[i]))) {
        return false;
      }
    }
    return true;
  }

  protected boolean isNewSessionRequest(HttpRequest request) {
    return "POST".equals(request.method()) && "/wd/hub/session".equals(request.uri());
  }

  protected void handleResponse(HttpRequest request, HttpResponse response,
      SelendroidResponse result) {
    if (result != null) {
      String resultString = result.render();
      response.setContentType("application/json");
      response.setEncoding(Charset.forName("UTF-8"));
      response.setContent(resultString);
      response.setStatus(200);
    } else {
      replyWithServerError(response);
    }

    response.end();
  }

  private String[] getRequestUrlSections(String urlToMatch) {
    if (urlToMatch == null) {
      return null;
    }
    int qPos = urlToMatch.indexOf('?');
    if (qPos != -1) {
      urlToMatch = urlToMatch.substring(0, urlToMatch.indexOf("?"));
    }
    return urlToMatch.split("/");
  }

  private String[] getMapperUrlSectionsCached(String mapperUrl) {
    String[] sections = mapperUrlSectionsCache.get(mapperUrl);
    if (sections == null) {
      sections = mapperUrl.split("/");
      for (int i = 0; i < sections.length; i++) {
        String section = sections[i];
        // To work around a but in Selenium Grid 2.31.0.
        int qPos = section.indexOf('?');
        if (qPos != -1) {
          sections[i] = section.substring(0, qPos);
        }
      }
      mapperUrlSectionsCache.put(mapperUrl, sections);
    }
    return sections;
  }
}
