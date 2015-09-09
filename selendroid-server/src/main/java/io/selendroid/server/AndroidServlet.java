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
package io.selendroid.server;

import io.selendroid.server.common.BaseRequestHandler;
import io.selendroid.server.common.BaseServlet;
import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.StatusCode;
import io.selendroid.server.common.exceptions.AppCrashedException;
import io.selendroid.server.common.exceptions.StaleElementReferenceException;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.common.http.HttpResponse;
import io.selendroid.server.common.http.TrafficCounter;
import io.selendroid.server.extension.ExtensionLoader;
import io.selendroid.server.handler.*;
import io.selendroid.server.handler.alert.Alert;
import io.selendroid.server.handler.alert.AlertAccept;
import io.selendroid.server.handler.alert.AlertDismiss;
import io.selendroid.server.handler.alert.AlertSendKeys;
import io.selendroid.server.handler.extension.ExtensionCallHandler;
import io.selendroid.server.handler.network.GetNetworkConnectionType;
import io.selendroid.server.handler.script.ExecuteAsyncScript;
import io.selendroid.server.handler.script.ExecuteScript;
import io.selendroid.server.handler.timeouts.AsyncTimeoutHandler;
import io.selendroid.server.handler.timeouts.SetImplicitWaitTimeout;
import io.selendroid.server.handler.timeouts.TimeoutsHandler;
import io.selendroid.server.model.DefaultSelendroidDriver;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.server.util.SelendroidLogger;

import java.net.URLDecoder;

public class AndroidServlet extends BaseServlet {
  private SelendroidDriver driver = null;
  protected ExtensionLoader extensionLoader = null;

  public AndroidServlet(SelendroidDriver driver, ExtensionLoader extensionLoader) {
    this.driver = driver;
    this.extensionLoader = extensionLoader;
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
    register(getHandler, new GetElementAttribute(
        "/wd/hub/session/:sessionId/element/:id/attribute/:name"));
    register(postHandler, new ClearElement("/wd/hub/session/:sessionId/element/:id/clear"));
    register(postHandler, new ClickElement("/wd/hub/session/:sessionId/element/:id/click"));
    register(getHandler,
        new GetElementDisplayed("/wd/hub/session/:sessionId/element/:id/displayed"));
    register(postHandler, new FindChildElement("/wd/hub/session/:sessionId/element/:id/element"));
    register(postHandler, new FindChildElements("/wd/hub/session/:sessionId/element/:id/elements"));
    register(getHandler, new GetElementEnabled("/wd/hub/session/:sessionId/element/:id/enabled"));
    register(getHandler, new ElementLocation("/wd/hub/session/:sessionId/element/:id/location"));
    register(getHandler, new GetElementLocationInView("/wd/hub/session/:sessionId/element/:id/location_in_view"));
    register(getHandler, new GetElementTagName("/wd/hub/session/:sessionId/element/:id/name"));
    register(getHandler, new GetElementSelected("/wd/hub/session/:sessionId/element/:id/selected"));
    register(getHandler, new LogElement("/wd/hub/session/:sessionId/element/:id/source"));
    register(postHandler, new SubmitForm("/wd/hub/session/:sessionId/element/:id/submit"));
    register(getHandler, new GetText("/wd/hub/session/:sessionId/element/:id/text"));
    register(postHandler, new SendKeysToElement("/wd/hub/session/:sessionId/element/:id/value"));
    register(getHandler, new GetElementSize("/wd/hub/session/:sessionId/element/:id/size"));
    register(postHandler, new ExecuteScript("/wd/hub/session/:sessionId/execute"));
    register(postHandler, new ExecuteAsyncScript("/wd/hub/session/:sessionId/execute_async"));
    register(postHandler, new GoForward("/wd/hub/session/:sessionId/forward"));
    register(postHandler, new FrameSwitchHandler("/wd/hub/session/:sessionId/frame"));
    register(postHandler, new SendKeys("/wd/hub/session/:sessionId/keys"));
    register(postHandler, new Refresh("/wd/hub/session/:sessionId/refresh"));
    register(getHandler, new CaptureScreenshot("/wd/hub/session/:sessionId/screenshot"));
    register(getHandler, new LogElementTree("/wd/hub/session/:sessionId/source"));
    register(postHandler, new TimeoutsHandler("/wd/hub/session/:sessionId/timeouts"));
    register(postHandler, new AsyncTimeoutHandler(
        "/wd/hub/session/:sessionId/timeouts/async_script"));
    register(postHandler, new SetImplicitWaitTimeout(
        "/wd/hub/session/:sessionId/timeouts/implicit_wait"));
    register(getHandler, new GetPageTitle("/wd/hub/session/:sessionId/title"));
    register(getHandler, new GetCurrentUrl("/wd/hub/session/:sessionId/url"));
    register(postHandler, new OpenUrl("/wd/hub/session/:sessionId/url"));
    register(postHandler, new SwitchContext("/wd/hub/session/:sessionId/window"));
    register(getHandler, new GetWindowSize("/wd/hub/session/:sessionId/window/:windowHandle/size"));
    register(getHandler, new GetContext("/wd/hub/session/:sessionId/window_handle"));
    register(getHandler, new GetContexts("/wd/hub/session/:sessionId/window_handles"));
    register(getHandler, new GetScreenOrientation("/wd/hub/session/:sessionId/orientation"));
    register(postHandler, new RotateScreen("/wd/hub/session/:sessionId/orientation"));

    // Advanced Touch API
    register(postHandler, new SingleTapOnElement("/wd/hub/session/:sessionId/touch/click"));
    register(postHandler, new Down("/wd/hub/session/:sessionId/touch/down"));
    register(postHandler, new Up("/wd/hub/session/:sessionId/touch/up"));
    register(postHandler, new Move("/wd/hub/session/:sessionId/touch/move"));
    register(postHandler, new Scroll("/wd/hub/session/:sessionId/touch/scroll"));
    register(postHandler, new DoubleTapOnElement("/wd/hub/session/:sessionId/touch/doubleclick"));
    register(postHandler, new LongPressOnElement("/wd/hub/session/:sessionId/touch/longclick"));
    register(postHandler, new Flick("/wd/hub/session/:sessionId/touch/flick"));
    // Track-ball functionality
    register(postHandler, new Roll("/wd/hub/session/:sessionId/trackball/roll"));
    
    // The new endpoints for context switching coming with Selenium 3.0 & mobile spec
    register(getHandler, new GetNetworkConnectionType("/wd/hub/session/:sessionId/network_connection"));
    register(getHandler, new GetContext("/wd/hub/session/:sessionId/context"));
    register(getHandler, new GetContexts("/wd/hub/session/:sessionId/contexts"));
    register(postHandler, new SwitchContext("/wd/hub/session/:sessionId/context"));

    // Custom extensions to wire protocol
    register(getHandler, new GetScreenState("/wd/hub/session/:sessionId/selendroid/screen/brightness"));
    register(postHandler, new SetScreenState("/wd/hub/session/:sessionId/selendroid/screen/brightness"));
    register(postHandler, new InspectorTap("/wd/hub/session/:sessionId/tap/2"));
    register(getHandler, new GetCommandConfiguration(
        "/wd/hub/session/:sessionId/selendroid/configure/command/:command"));
    register(postHandler, new SetCommandConfiguration(
        "/wd/hub/session/:sessionId/selendroid/configure/command/:command"));
    register(postHandler, new ForceGcExplicitly("/wd/hub/session/:sessionId/selendroid/gc"));
    register(postHandler, new SetSystemProperty("/wd/hub/session/:sessionId/selendroid/systemProperty"));

    // Endpoints to send app to background and resume it
    register(postHandler, new BackgroundApp("/wd/hub/session/:sessionId/selendroid/background"));
    register(postHandler, new ResumeApp("/wd/hub/session/:sessionId/selendroid/resume"));

    // Endpoints to add to and read call logs
    register(postHandler, new AddCallLog("/wd/hub/session/:sessionId/selendroid/addCallLog"));
    register(postHandler, new ReadCallLog("/wd/hub/session/:sessionId/selendroid/readCallLog"));

    // Handle calls to dynamically loaded handlers
    register(postHandler, new ExtensionCallHandler(
        "/wd/hub/session/:sessionId/selendroid/extension", extensionLoader));

    // Actions sequencing endpoint
    register(postHandler, new Actions("/wd/hub/session/:sessionId/actions"));

    // currently not yet supported
    register(getHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/ime/available_engines"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/ime/active_engine"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/ime/activated"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/ime/deactivate"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/ime/activate"));
    register(deleteHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/window"));
    register(postHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/window/:windowHandle/size"));
    register(postHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/window/:windowHandle/position"));
    register(getHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/window/:windowHandle/position"));
    register(postHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/window/:windowHandle/maximize"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/element/:id"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/element/active"));
    register(getHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/element/:id/equals/:other"));
    register(getHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/element/:id/css/:propertyName"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/moveto"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/buttondown"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/buttonup"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/doubleclick"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/location"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/location"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage"));
    register(deleteHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage"));
    register(getHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/local_storage/key/:key"));
    register(deleteHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/local_storage/key/:key"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage/size"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/location"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/location"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage"));
    register(deleteHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage"));
    register(getHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/local_storage/key/:key"));
    register(deleteHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/local_storage/key/:key"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/local_storage/size"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/session_storage"));
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/session_storage"));
    register(deleteHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/session_storage"));
    register(getHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/session_storage/key/:key"));
    register(deleteHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/session_storage/key/:key"));
    register(getHandler, new UnknownCommandHandler(
        "/wd/hub/session/:sessionId/session_storage/size"));

    // handled in the standalone-server
    register(postHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/log"));
    register(getHandler, new UnknownCommandHandler("/wd/hub/session/:sessionId/log/types"));
  }

  private void addHandlerAttributesToRequest(HttpRequest request, String mappedUri) {
    String sessionId = getParameter(mappedUri, request.uri(), ":sessionId");
    if (sessionId != null) {
      request.data().put(SESSION_ID_KEY, sessionId);
    }

    String command = getParameter(mappedUri, request.uri(), ":command");
    if (command != null) {
      request.data().put(COMMAND_NAME_KEY, command);
    }

    String id = getParameter(mappedUri, request.uri(), ":id");
    if (id != null) {
      request.data().put(ELEMENT_ID_KEY, URLDecoder.decode(id));
    }
    String name = getParameter(mappedUri, request.uri(), ":name");
    if (name != null) {
      request.data().put(NAME_ID_KEY, name);
    }

    request.data().put(DRIVER_KEY, driver);
  }

  @Override
  public void handleRequest(HttpRequest request, HttpResponse response, BaseRequestHandler handler) {
    if ("/favicon.ico".equals(request.uri()) && handler == null) {
      response.setStatus(404).end();
      return;
    } else if (handler == null) {
      response.setStatus(404).end();
      return;
    }
    Response result;
    try {
      addHandlerAttributesToRequest(request, handler.getMappedUri());
      if (!handler.commandAllowedWithAlertPresentInWebViewMode()) {
        SelendroidDriver driver =
            (SelendroidDriver) request.data().get(AndroidServlet.DRIVER_KEY);
        if (driver != null && driver.isAlertPresent()) {
          result =
              new SelendroidResponse(handler.getSessionId(request),
                  StatusCode.UNEXPECTED_ALERT_OPEN,
                  "Unhandled Alert present");
          handleResponse(request, response, (SelendroidResponse) result);
          return;
        }
      }
      result = handler.handle(request);
    } catch (StaleElementReferenceException se) {
      try {
        SelendroidLogger.error("StaleElementReferenceException", se);
        String sessionId = getParameter(handler.getMappedUri(), request.uri(), ":sessionId");
        result = new SelendroidResponse(sessionId, StatusCode.STALE_ELEMENT_REFERENCE, se);
      } catch (Exception e) {
        SelendroidLogger.error("Error responding to StaleElementReferenceException", e);
        replyWithServerError(response);
        return;
      }
    } catch (AppCrashedException ae) {
      try {
        SelendroidLogger.error("App crashed when handling request", ae);
        String sessionId = getParameter(handler.getMappedUri(), request.uri(), ":sessionId");
        result = new SelendroidResponse(sessionId, StatusCode.UNKNOWN_ERROR, ae);
      } catch (Exception e) {
        SelendroidLogger.error("Error responding to app crash", e);
        replyWithServerError(response);
        return;
      }
    } catch (Exception e) {
      SelendroidLogger.error("Error handling request.", e);
      replyWithServerError(response);
      return;
    }
    handleResponse(request, response, (SelendroidResponse) result);
    String trafficStatistics = String.format(
        "traffic_stats: rx_bytes %d tx_bytes %d",
        TrafficCounter.readBytes(),
        TrafficCounter.writtenBytes());
    SelendroidLogger.info(trafficStatistics);
  }
}
