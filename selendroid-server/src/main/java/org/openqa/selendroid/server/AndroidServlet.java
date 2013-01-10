/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.server;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;

import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.handler.CaptureScreenshot;
import org.openqa.selendroid.server.handler.ClearElement;
import org.openqa.selendroid.server.handler.ClickElement;
import org.openqa.selendroid.server.handler.DeleteSession;
import org.openqa.selendroid.server.handler.FindElement;
import org.openqa.selendroid.server.handler.FindElements;
import org.openqa.selendroid.server.handler.GetCapabilities;
import org.openqa.selendroid.server.handler.GetCurrentUrl;
import org.openqa.selendroid.server.handler.GetStatus;
import org.openqa.selendroid.server.handler.GetText;
import org.openqa.selendroid.server.handler.ListSessions;
import org.openqa.selendroid.server.handler.LogElement;
import org.openqa.selendroid.server.handler.LogElementTree;
import org.openqa.selendroid.server.handler.NewSession;
import org.openqa.selendroid.server.handler.SendKeys;
import org.openqa.selendroid.server.handler.SetImplicitWaitTimeout;
import org.openqa.selendroid.server.handler.SubmitForm;
import org.openqa.selendroid.server.handler.SwitchWindow;
import org.openqa.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import com.google.common.base.Charsets;

public class AndroidServlet implements HttpHandler {
  public static final int INTERNAL_SERVER_ERROR = 500;
  public static final String SESSION_ID_KEY = "SESSION_ID_KEY";
  public static final String ELEMENT_ID_KEY = "ELEMENT_ID_KEY";
  public static final String DRIVER_KEY = "DRIVER_KEY";
  protected HashMap<String, Class<? extends RequestHandler>> getHandler =
      new HashMap<String, Class<? extends RequestHandler>>();
  protected HashMap<String, Class<? extends RequestHandler>> postHandler =
      new HashMap<String, Class<? extends RequestHandler>>();
  protected HashMap<String, Class<? extends RequestHandler>> deleteHandler =
      new HashMap<String, Class<? extends RequestHandler>>();
  protected SelendroidDriver driver = null;

  public AndroidServlet(SelendroidDriver driver) {
    this.driver = driver;
    init();
  }

  protected void init() {

    postHandler.put("/wd/hub/session", NewSession.class);
    getHandler.put("/wd/hub/session/:sessionId", GetCapabilities.class);
    deleteHandler.put("/wd/hub/session/:sessionId", DeleteSession.class);

    getHandler.put("/wd/hub/session/:sessionId/screenshot", CaptureScreenshot.class);
    getHandler.put("/wd/hub/status", GetStatus.class);

    postHandler.put("/wd/hub/session/:sessionId/element", FindElement.class);
    postHandler.put("/wd/hub/session/:sessionId/elements", FindElements.class);
    postHandler.put("/wd/hub/session/:sessionId/element/:id/click", ClickElement.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/text", GetText.class);
    getHandler.put("/wd/hub/session/:sessionId/url", GetCurrentUrl.class);
    postHandler.put("/wd/hub/session/:sessionId/element/:id/value", SendKeys.class);
    getHandler.put("/wd/hub/session/:sessionId/source", LogElementTree.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/source", LogElement.class);
    postHandler.put("/wd/hub/session/:sessionId/element/:id/clear", ClearElement.class);
    getHandler.put("/wd/hub/sessions", ListSessions.class);
    postHandler.put("/wd/hub/session/:sessionId/timeouts/implicit_wait",
        SetImplicitWaitTimeout.class);
    postHandler.put("/wd/hub/session/:sessionId/window", SwitchWindow.class);
    postHandler.put("/session/:sessionId/element/:id/submit", SubmitForm.class);
  }

  public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control)
      throws Exception {
    if ("GET".equals(request.method())) {
      findAndInvokeMatcher(request, response, getHandler);
    } else if ("POST".equals(request.method())) {
      findAndInvokeMatcher(request, response, postHandler);
    } else if ("DELETE".equals(request.method())) {
      findAndInvokeMatcher(request, response, deleteHandler);
    }
    replyWithServerError(response);
  }

  private void findAndInvokeMatcher(HttpRequest request, HttpResponse response,
      HashMap<String, Class<? extends RequestHandler>> handler) {
    for (Map.Entry<String, Class<? extends RequestHandler>> entry : handler.entrySet()) {
      if (isFor(entry.getKey(), request.uri())) {
        addHandlerAttributesToRequest(request, entry.getKey());
        Response result = null;
        try {
          result = instantiateHandler(entry.getValue(), request).handle();
        } catch (SelendroidException e) {
          SelendroidLogger.logError("Error occured while handling reuqest.", e);
          replyWithServerError(response);
          return;
        }

        response.header("Content-Type", "application/json");
        response.charset(Charsets.UTF_8);

        if (isNewSessionRequest(request)) {
          response.status(301);
          String session = result.getSessionId();

          SelendroidLogger.log("new URL: " + request.uri() + "/" + session);
          response.header("location", request.uri() + "/" + session);
        } else {
          response.status(200);
        }

        if (result != null) {
          // byte[] data = Charsets.UTF_8.encode(result).array();
          response.content(result.toString());
        }
        response.end();
      }
    }
  }

  protected RequestHandler instantiateHandler(Class<? extends RequestHandler> clazz,
      HttpRequest request) {
    RequestHandler handler = null;
    try {
      Constructor<? extends RequestHandler> handlerConstr = clazz.getConstructor(HttpRequest.class);
      handler = handlerConstr.newInstance(request);
    } catch (Exception e) {
      e.printStackTrace();
      SelendroidLogger.logError("Error occured while creating handler: ", e);
    }

    return handler;
  }

  private boolean isNewSessionRequest(HttpRequest request) {
    if ("POST".equals(request.method()) && "/wd/hub/session".equals(request.uri())) {
      return true;
    }
    return false;
  }

  private void addHandlerAttributesToRequest(HttpRequest request, String mappedUri) {
    String sessionId = getParameter(mappedUri, request.uri(), ":sessionId");
    if (sessionId != null) {
      request.data().put(SESSION_ID_KEY, sessionId);
    }

    String id = getParameter(mappedUri, request.uri(), ":id");
    if (id != null) {
      request.data().put(ELEMENT_ID_KEY, id);
    }

    request.data().put(DRIVER_KEY, driver);
  }

  private String getParameter(String configuredUri, String actualUri, String param) {
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

  private void replyWithServerError(HttpResponse response) {
    response.status(INTERNAL_SERVER_ERROR);
    response.end();
  }

  public boolean isFor(String mapperUrl, String urlToMatch) {
    String[] sections = mapperUrl.split("/");
    if (urlToMatch == null) {
      return sections.length == 0;
    }
    String[] allParts = urlToMatch.split("/");
    if (sections.length != allParts.length) {
      return false;
    }
    for (int i = 0; i < sections.length; i++) {
      if (!(sections[i].startsWith(":") || sections[i].equals(allParts[i]))) {
        return false;
      }
    }

    return true;
  }
}
