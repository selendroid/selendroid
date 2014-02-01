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

import io.selendroid.exceptions.AppCrashedException;
import io.selendroid.exceptions.StaleElementReferenceException;
import io.selendroid.server.handler.AddCookie;
import io.selendroid.server.handler.CaptureScreenshot;
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
import io.selendroid.server.handler.FrameSwitchHandler;
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
import io.selendroid.server.handler.GetScreenState;
import io.selendroid.server.handler.GetText;
import io.selendroid.server.handler.GetWindowHandle;
import io.selendroid.server.handler.GetWindowHandles;
import io.selendroid.server.handler.GetWindowSize;
import io.selendroid.server.handler.GoBack;
import io.selendroid.server.handler.GoForward;
import io.selendroid.server.handler.InspectorTap;
import io.selendroid.server.handler.ListSessions;
import io.selendroid.server.handler.LogElement;
import io.selendroid.server.handler.LogElementTree;
import io.selendroid.server.handler.LongPressOnElement;
import io.selendroid.server.handler.Move;
import io.selendroid.server.handler.NewSession;
import io.selendroid.server.handler.OpenUrl;
import io.selendroid.server.handler.Refresh;
import io.selendroid.server.handler.Scroll;
import io.selendroid.server.handler.SendKeyToActiveElement;
import io.selendroid.server.handler.SendKeys;
import io.selendroid.server.handler.SetImplicitWaitTimeout;
import io.selendroid.server.handler.SetScreenState;
import io.selendroid.server.handler.SingleTapOnElement;
import io.selendroid.server.handler.SubmitForm;
import io.selendroid.server.handler.SwitchWindow;
import io.selendroid.server.handler.UnknownCommandHandler;
import io.selendroid.server.handler.Up;
import io.selendroid.server.handler.alert.Alert;
import io.selendroid.server.handler.alert.AlertAccept;
import io.selendroid.server.handler.alert.AlertDismiss;
import io.selendroid.server.handler.alert.AlertSendKeys;
import io.selendroid.server.model.DefaultSelendroidDriver;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

public class AndroidServlet extends BaseServlet {
  private SelendroidDriver driver = null;

  public AndroidServlet(SelendroidDriver driver) {
    this.driver = driver;
    init();
  }

  protected void init() {
    register(postHandler, new NewSession("/wd/hub/session"));
    register(getHandler, new ListSessions("/wd/hub/sessions"));
    register(getHandler, new GetCapabilities("/wd/hub/session/:sessionId"));
    register(deleteHandler, new DeleteSession("/wd/hub/session/:sessionId"));
    register(getHandler, new Alert("/wd/hub/session/:sessionId/alert_text"));
    register(postHandler, new AlertSendKeys("/wd/hub/session/:sessionId/alert_text"));
    register(postHandler, new AlertAccept("/wd/hub/session/:sessionId/accept_alert"));
    register(postHandler, new GoBack("/wd/hub/session/:sessionId/back"));
    register(getHandler, new GetCookies("/wd/hub/session/:sessionId/cookie"));
    register(postHandler, new AddCookie("/wd/hub/session/:sessionId/cookie"));
    register(deleteHandler, new DeleteCookies("/wd/hub/session/:sessionId/cookie"));
    register(deleteHandler, new DeleteNamedCookie("/wd/hub/session/:sessionId/cookie/:name"));
    register(postHandler, new AlertDismiss("/wd/hub/session/:sessionId/dismiss_alert"));
    register(postHandler, new FindElement("/wd/hub/session/:sessionId/element"));
    register(postHandler, new FindElements("/wd/hub/session/:sessionId/elements"));
    register(getHandler, new GetElementAttribute("/wd/hub/session/:sessionId/element/:id/attribute/:name"));
    register(postHandler, new ClearElement("/wd/hub/session/:sessionId/element/:id/clear"));
    register(postHandler, new ClickElement("/wd/hub/session/:sessionId/element/:id/click"));
    register(getHandler, new GetElementDisplayed("/wd/hub/session/:sessionId/element/:id/displayed"));
    register(postHandler, new FindChildElement("/wd/hub/session/:sessionId/element/:id/element"));
    register(postHandler, new FindChildElements("/wd/hub/session/:sessionId/element/:id/elements"));
    register(getHandler, new GetElementEnabled("/wd/hub/session/:sessionId/element/:id/enabled"));
    register(getHandler, new ElementLocation("/wd/hub/session/:sessionId/element/:id/location"));
    register(getHandler, new GetElementTagName("/wd/hub/session/:sessionId/element/:id/name"));
    register(getHandler, new GetElementSelected("/wd/hub/session/:sessionId/element/:id/selected"));
    register(getHandler, new LogElement("/wd/hub/session/:sessionId/element/:id/source"));
    register(postHandler, new SubmitForm("/wd/hub/session/:sessionId/element/:id/submit"));
    register(getHandler, new GetText("/wd/hub/session/:sessionId/element/:id/text"));
    register(postHandler, new SendKeys("/wd/hub/session/:sessionId/element/:id/value"));
    register(getHandler, new GetElementSize("/wd/hub/session/:sessionId/element/:id/size"));
    register(postHandler, new ExecuteScript("/wd/hub/session/:sessionId/execute"));
    register(postHandler, new GoForward("/wd/hub/session/:sessionId/forward"));
    register(postHandler, new FrameSwitchHandler("/wd/hub/session/:sessionId/frame"));
    register(postHandler, new SendKeyToActiveElement("/wd/hub/session/:sessionId/keys"));
    register(postHandler, new Refresh("/wd/hub/session/:sessionId/refresh"));
    register(getHandler, new CaptureScreenshot("/wd/hub/session/:sessionId/screenshot"));
    register(getHandler, new LogElementTree("/wd/hub/session/:sessionId/source"));
    register(postHandler, new SetImplicitWaitTimeout("/wd/hub/session/:sessionId/timeouts/implicit_wait"));
    register(getHandler, new GetPageTitle("/wd/hub/session/:sessionId/title"));
    register(getHandler, new GetCurrentUrl("/wd/hub/session/:sessionId/url"));
    register(postHandler, new OpenUrl("/wd/hub/session/:sessionId/url"));
    register(postHandler, new SwitchWindow("/wd/hub/session/:sessionId/window"));
    register(getHandler, new GetWindowSize("/wd/hub/session/:sessionId/window/:windowHandle/size"));
    register(getHandler, new GetWindowHandle("/wd/hub/session/:sessionId/window_handle"));
    register(getHandler, new GetWindowHandles("/wd/hub/session/:sessionId/window_handles"));
    
    // Advanced Touch API
    register(postHandler, new SingleTapOnElement("/wd/hub/session/:sessionId/touch/click"));
    register(postHandler, new Down("/wd/hub/session/:sessionId/touch/down"));
    register(postHandler, new Up("/wd/hub/session/:sessionId/touch/up"));
    register(postHandler, new Move("/wd/hub/session/:sessionId/touch/move"));
    register(postHandler, new Scroll("/wd/hub/session/:sessionId/touch/scroll"));
    register(postHandler, new DoubleTapOnElement("/wd/hub/session/:sessionId/touch/doubleclick"));
    register(postHandler, new LongPressOnElement("/wd/hub/session/:sessionId/touch/longclick"));
    register(postHandler, new Flick("/wd/hub/session/:sessionId/touch/flick"));

    // Custom extensions to wire protocol
    register(getHandler, new GetScreenState("/wd/hub/-selendroid/:sessionId/screen/brightness"));
    register(postHandler, new SetScreenState("/wd/hub/-selendroid/:sessionId/screen/brightness"));
    register(postHandler, new InspectorTap("/wd/hub/session/:sessionId/tap/2"));

    // currently not yet supported
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/orientation"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/orientation"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/timeouts"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/timeouts/async_script"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/execute_async"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/ime/available_engines"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/ime/active_engine"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/ime/activated"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/ime/deactivate"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/ime/activate"));
    register(deleteHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/window"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/window/:windowHandle/size"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/window/:windowHandle/position"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/window/:windowHandle/position"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/window/:windowHandle/maximize"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/element/:id"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/element/active"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/element/:id/equals/:other"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/element/:id/css/:propertyName"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/moveto"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/buttondown"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/buttonup"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/doubleclick"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/location"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/location"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage"));
    register(deleteHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage/key/:key"));
    register(deleteHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage/key/:key"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage/size"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/location"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/location"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage"));
    register(deleteHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage/key/:key"));
    register(deleteHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage/key/:key"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage/size"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/session_storage"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/session_storage"));
    register(deleteHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/session_storage"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/session_storage/key/:key"));
    register(deleteHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/session_storage/key/:key"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/session_storage/size"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/log"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/log/types"));
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
      if (!handler.commandAllowedWithAlertPresentInWebViewMode()) {
        DefaultSelendroidDriver driver =
            (DefaultSelendroidDriver) request.data().get(AndroidServlet.DRIVER_KEY);
        if (driver != null && driver.isAlertPresent()) {
          result = new SelendroidResponse(handler.getSessionId(request), 26, "Unhandled Alert present");
          handleResponse(request, response, (SelendroidResponse) result);
          return;
        }
      }
      result = handler.handle(request);
    } catch (StaleElementReferenceException se) {
      try {
        String sessionId = getParameter(handler.getMappedUri(), request.uri(), ":sessionId");
        result = new SelendroidResponse(sessionId, 13, se);
      } catch (Exception e) {
        SelendroidLogger.log("Error occurred while handling request and got StaleRef.", e);
        replyWithServerError(response);
        return;
      }
    } catch (AppCrashedException ae) {
      try {
        String sessionId = getParameter(handler.getMappedUri(), request.uri(), ":sessionId");
        result = new SelendroidResponse(sessionId, 13, ae);
      } catch (Exception e) {
        SelendroidLogger.log("Error occurred while handling request and got AppCrashedException.", e);
        replyWithServerError(response);
        return;
      }
    } catch (Exception e) {
      SelendroidLogger.log("Error occurred while handling request.", e);
      replyWithServerError(response);
      return;
    }
    handleResponse(request, response, (SelendroidResponse) result);
  }
}
