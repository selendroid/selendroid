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

import io.selendroid.exceptions.StaleElementReferenceException;
import io.selendroid.server.handler.CaptureScreenshot;
import io.selendroid.server.handler.AddCookie;
import io.selendroid.server.handler.ClearElement;
import io.selendroid.server.handler.ClickElement;
import io.selendroid.server.handler.DeleteCookies;
import io.selendroid.server.handler.DeleteNamedCookie;
import io.selendroid.server.handler.DeleteSession;
import io.selendroid.server.handler.DoubleTapOnElement;
import io.selendroid.server.handler.Down;
import io.selendroid.server.handler.ElementLocation;
import io.selendroid.server.handler.ExecuteScript;
import io.selendroid.server.handler.FindChildElement;
import io.selendroid.server.handler.FindChildElements;
import io.selendroid.server.handler.FindElement;
import io.selendroid.server.handler.FindElements;
import io.selendroid.server.handler.Flick;
import io.selendroid.server.handler.GetCapabilities;
import io.selendroid.server.handler.GetCookies;
import io.selendroid.server.handler.GetCurrentUrl;
import io.selendroid.server.handler.GetElementAttribute;
import io.selendroid.server.handler.GetElementDisplayed;
import io.selendroid.server.handler.GetElementEnabled;
import io.selendroid.server.handler.GetElementSelected;
import io.selendroid.server.handler.GetElementSize;
import io.selendroid.server.handler.GetElementTagName;
import io.selendroid.server.handler.GetPageTitle;
import io.selendroid.server.handler.GetText;
import io.selendroid.server.handler.GetWindowHandle;
import io.selendroid.server.handler.GetWindowHandles;
import io.selendroid.server.handler.GetWindowSize;
import io.selendroid.server.handler.GoBack;
import io.selendroid.server.handler.InspectorTap;
import io.selendroid.server.handler.ListSessions;
import io.selendroid.server.handler.LogElement;
import io.selendroid.server.handler.LogElementTree;
import io.selendroid.server.handler.LongPressOnElement;
import io.selendroid.server.handler.Move;
import io.selendroid.server.handler.NewSession;
import io.selendroid.server.handler.OpenUrl;
import io.selendroid.server.handler.Scroll;
import io.selendroid.server.handler.SendKeyToActiveElement;
import io.selendroid.server.handler.SendKeys;
import io.selendroid.server.handler.SetImplicitWaitTimeout;
import io.selendroid.server.handler.SingleTapOnElement;
import io.selendroid.server.handler.SubmitForm;
import io.selendroid.server.handler.SwitchWindow;
import io.selendroid.server.handler.UnknownCommandHandler;
import io.selendroid.server.handler.Up;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class AndroidServlet extends BaseServlet {
  private SelendroidDriver driver = null;

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
    getHandler.put("/wd/hub/session/:sessionId/window/:windowHandle/size",
        GetWindowSize.class);
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
    getHandler.put("/wd/hub/session/:sessionId/cookie", GetCookies.class);
    postHandler.put("/wd/hub/session/:sessionId/cookie", AddCookie.class);
    deleteHandler.put("/wd/hub/session/:sessionId/cookie", DeleteCookies.class);
    deleteHandler.put("/wd/hub/session/:sessionId/cookie/:name", DeleteNamedCookie.class);

    // Advanced Touch API
    postHandler.put("/wd/hub/session/:sessionId/touch/click", SingleTapOnElement.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/down", Down.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/up", Up.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/move", Move.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/scroll", Scroll.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/doubleclick", DoubleTapOnElement.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/longclick", LongPressOnElement.class);
    postHandler.put("/wd/hub/session/:sessionId/touch/flick", Flick.class);
    getHandler.put("/wd/hub/session/:sessionId/window_handle", GetWindowHandle.class);
    getHandler.put("/wd/hub/session/:sessionId/window_handles", GetWindowHandles.class);

    // currently not yet supported
    getHandler.put("/wd/hub/session/:sessionId/orientation", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/orientation", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/timeouts", UnknownCommandHandler.class);
    postHandler
        .put("/wd/hub/session/:sessionId/timeouts/async_script", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/forward", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/back", GoBack.class);
    postHandler.put("/wd/hub/session/:sessionId/refresh", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/execute_async", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/ime/available_engines", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/ime/active_engine", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/ime/activated", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/ime/deactivate", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/ime/activate", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/frame", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/window", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/window/:windowHandle/size",
        UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/window/:windowHandle/position",
        UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/window/:windowHandle/position",
        UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/window/:windowHandle/maximize",
        UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/element/active", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/name", GetElementTagName.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/equals/:other",
        UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/element/:id/css/:propertyName",
        UnknownCommandHandler.class);
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
    getHandler
        .put("/wd/hub/session/:sessionId/local_storage/key/:key", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/local_storage/key/:key",
        UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/local_storage/size", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/location", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/location", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/local_storage", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/local_storage", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/local_storage", UnknownCommandHandler.class);
    getHandler
        .put("/wd/hub/session/:sessionId/local_storage/key/:key", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/local_storage/key/:key",
        UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/local_storage/size", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/session_storage", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/session_storage", UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/session_storage", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/session_storage/key/:key",
        UnknownCommandHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId/session_storage/key/:key",
        UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/session_storage/size", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/log", UnknownCommandHandler.class);
    getHandler.put("/wd/hub/session/:sessionId/log/types", UnknownCommandHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/tap/2", InspectorTap.class);
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

  @Override
  public void handleRequest(HttpRequest request, HttpResponse response, BaseRequestHandler handler) {
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
        result = new SelendroidResponse(sessionId, 13, se);
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

    handleResponse(request, response, (SelendroidResponse) result);
  }
}
