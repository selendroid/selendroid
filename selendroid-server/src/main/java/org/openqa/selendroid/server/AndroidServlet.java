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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selendroid.server.common.BaseServlet;
import org.openqa.selendroid.server.exceptions.StaleElementReferenceException;
import org.openqa.selendroid.server.handler.CaptureScreenshot;
import org.openqa.selendroid.server.handler.ClearElement;
import org.openqa.selendroid.server.handler.ClickElement;
import org.openqa.selendroid.server.handler.DeleteSession;
import org.openqa.selendroid.server.handler.DoubleTapOnElement;
import org.openqa.selendroid.server.handler.Down;
import org.openqa.selendroid.server.handler.ElementLocation;
import org.openqa.selendroid.server.handler.ExecuteScript;
import org.openqa.selendroid.server.handler.FindChildElement;
import org.openqa.selendroid.server.handler.FindChildElements;
import org.openqa.selendroid.server.handler.FindElement;
import org.openqa.selendroid.server.handler.FindElements;
import org.openqa.selendroid.server.handler.Flick;
import org.openqa.selendroid.server.handler.GetCapabilities;
import org.openqa.selendroid.server.handler.GetCurrentUrl;
import org.openqa.selendroid.server.handler.GetElementAttribute;
import org.openqa.selendroid.server.handler.GetElementDisplayed;
import org.openqa.selendroid.server.handler.GetElementEnabled;
import org.openqa.selendroid.server.handler.GetElementSelected;
import org.openqa.selendroid.server.handler.GetElementSize;
import org.openqa.selendroid.server.handler.GetPageTitle;
import org.openqa.selendroid.server.handler.GetText;
import org.openqa.selendroid.server.handler.ListSessions;
import org.openqa.selendroid.server.handler.LogElement;
import org.openqa.selendroid.server.handler.LogElementTree;
import org.openqa.selendroid.server.handler.LongPressOnElement;
import org.openqa.selendroid.server.handler.Move;
import org.openqa.selendroid.server.handler.NewSession;
import org.openqa.selendroid.server.handler.OpenUrl;
import org.openqa.selendroid.server.handler.Scroll;
import org.openqa.selendroid.server.handler.SendKeyToActiveElement;
import org.openqa.selendroid.server.handler.SendKeys;
import org.openqa.selendroid.server.handler.SetImplicitWaitTimeout;
import org.openqa.selendroid.server.handler.SingleTapOnElement;
import org.openqa.selendroid.server.handler.SubmitForm;
import org.openqa.selendroid.server.handler.SwitchWindow;
import org.openqa.selendroid.server.handler.UnknownCommandHandler;
import org.openqa.selendroid.server.handler.Up;
import org.openqa.selendroid.server.model.SelendroidDriver;
import org.openqa.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class AndroidServlet extends BaseServlet implements HttpHandler {
  public static final int INTERNAL_SERVER_ERROR = 500;
  public static final String SESSION_ID_KEY = "SESSION_ID_KEY";
  public static final String ELEMENT_ID_KEY = "ELEMENT_ID_KEY";
  public static final String NAME_ID_KEY = "NAME_ID_KEY";
  public static final String DRIVER_KEY = "DRIVER_KEY";
  protected Map<String, Class<? extends RequestHandler>> getHandler =
      new HashMap<String, Class<? extends RequestHandler>>();
  protected Map<String, Class<? extends RequestHandler>> postHandler =
      new HashMap<String, Class<? extends RequestHandler>>();
  protected Map<String, Class<? extends RequestHandler>> deleteHandler =
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
    postHandler.put("/wd/hub/session/:sessionId/element", FindElement.class);
    postHandler.put("/wd/hub/session/:sessionId/element/:id/element", FindChildElement.class);
    postHandler.put("/wd/hub/session/:sessionId/element/:id/elements", FindChildElements.class);
    postHandler.put("/wd/hub/session/:sessionId/elements", FindElements.class);
    postHandler.put("/wd/hub/session/:sessionId/element/:id/click", ClickElement.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/text", GetText.class);
    getHandler.put("/wd/hub/session/:sessionId/url", GetCurrentUrl.class);
    postHandler.put("/wd/hub/session/:sessionId/url", OpenUrl.class);
    postHandler.put("/wd/hub/session/:sessionId/element/:id/value", SendKeys.class);
    getHandler.put("/wd/hub/session/:sessionId/source", LogElementTree.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/source", LogElement.class);
    postHandler.put("/wd/hub/session/:sessionId/element/:id/clear", ClearElement.class);
    getHandler.put("/wd/hub/sessions", ListSessions.class);
    postHandler.put("/wd/hub/session/:sessionId/timeouts/implicit_wait",
        SetImplicitWaitTimeout.class);
    postHandler.put("/wd/hub/session/:sessionId/window", SwitchWindow.class);
    postHandler.put("/wd/hub/session/:sessionId/element/:id/submit", SubmitForm.class);
    postHandler.put("/wd/hub/session/:sessionId/keys", SendKeyToActiveElement.class);
    getHandler.put("/wd/hub/session/:sessionId/title", GetPageTitle.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/selected", GetElementSelected.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/location", ElementLocation.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/attribute/:name",
        GetElementAttribute.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/size", GetElementSize.class);
    postHandler.put("/wd/hub/session/:sessionId/execute", ExecuteScript.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/enabled", GetElementEnabled.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/displayed", GetElementDisplayed.class);

    // Advanced Touch API
    postHandler.put("/wd/hub/session/:sessionId/touch/click", SingleTapOnElement.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/down", Down.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/up", Up.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/move", Move.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/scroll", Scroll.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/doubleclick", DoubleTapOnElement.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/longclick", LongPressOnElement.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/flick", Flick.class);

    // currently not yet supported
    getHandler.put("/wd/hub/session/:sessionId/orientation", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/orientation", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/timeouts", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/timeouts/async_script", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/window_handle", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/window_handles", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/forward", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/back", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/refresh", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/execute_async", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/ime/available_engines", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/ime/active_engine", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/ime/activated", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/ime/deactivate", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/ime/activate", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/frame", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/window", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/window/:windowHandle/size", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/window/:windowHandle/size", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/window/:windowHandle/position",
        UnknownCommandHandler.class);
    getHandler
        .put("/wd/hub/session/:sessionId/window/:windowHandle/position", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/window/:windowHandle/maximize",
        UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/cookie", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/cookie", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/cookie", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/cookie/:name", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/element/active", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/name", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/equals/:other", UnknownCommandHandler.class);
    getHandler
        .put("/wd/hub/session/:sessionId/element/:id/css/:propertyName", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/alert_text", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/alert_text", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/accept_alert", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/dismiss_alert", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/moveto", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/buttondown", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/buttonup", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/doubleclick", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/location", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/location", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/local_storage", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/local_storage", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/local_storage", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/local_storage/key/:key", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/local_storage/key/:key", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/local_storage/size", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/location", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/location", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/local_storage", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/local_storage", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/local_storage", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/local_storage/key/:key", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/local_storage/key/:key", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/local_storage/size", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/session_storage", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/session_storage", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/session_storage", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/session_storage/key/:key", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/session_storage/key/:key", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/session_storage/size", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/log", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/log/types", UnknownCommandHandler.class);
  }

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
    Response result = null;
    try {
      addHandlerAttributesToRequest(request, handler.getMappedUri());
      result = handler.handle();
    } catch (StaleElementReferenceException se) {
      try {
        String sessionId = getParameter(handler.getMappedUri(), request.uri(), ":sessionId");
        result = new Response(sessionId, 13, se);
      } catch (Exception e) {
        SelendroidLogger.log("Error occured while handling reuqest and got StaleRef.", e);
        replyWithServerError(response);
        return;
      }
    } catch (Exception e) {
      SelendroidLogger.log("Error occured while handling reuqest.", e);
      replyWithServerError(response);
      return;
    }

    response.header("Content-Type", "application/json");
    response.charset(Charset.forName("UTF-8"));

    if (isNewSessionRequest(request)) {
      response.status(301);
      String session = result.getSessionId();

      String newSessionUri = "http://" + request.header("Host") + request.uri() + "/" + session;
      SelendroidLogger.log("new Session URL: " + newSessionUri);
      response.header("location", newSessionUri);
    } else {
      response.status(200);
    }

    if (result != null) {
      String resultString = result.toString();
      response.content(resultString);
    }
    response.end();
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
    String name = getParameter(mappedUri, request.uri(), ":name");
    if (name != null) {
      request.data().put(NAME_ID_KEY, name);
    }

    request.data().put(DRIVER_KEY, driver);
  }
}
