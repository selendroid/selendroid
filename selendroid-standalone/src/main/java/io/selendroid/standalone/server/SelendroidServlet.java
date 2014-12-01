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
package io.selendroid.standalone.server;

import io.selendroid.server.common.BaseRequestHandler;
import io.selendroid.server.common.BaseServlet;
import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.UiResponse;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.common.http.HttpResponse;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.server.handler.*;
import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SelendroidServlet extends BaseServlet {
  private static final Logger log = Logger.getLogger(SelendroidServlet.class.getName());
  protected Map<String, BaseRequestHandler> redirectHandler = new HashMap<String, BaseRequestHandler>();
  private SelendroidStandaloneDriver driver;
  private SelendroidConfiguration conf;

  public SelendroidServlet(SelendroidStandaloneDriver driver, SelendroidConfiguration conf) {
    this.driver = driver;
    this.conf = conf;
    init();
  }

  protected void init() {
    register(postHandler, new CreateSessionHandler("/wd/hub/session"));
    register(getHandler, new ListSessionsHandler("/wd/hub/sessions"));
    register(getHandler, new GetCapabilities("/wd/hub/session/:sessionId"));

    register(getHandler, new GetLogTypes("/wd/hub/session/:sessionId/log/types"));
    register(postHandler, new GetLogs("/wd/hub/session/:sessionId/log"));
    if (!conf.isDeviceScreenshot()) {
      register(getHandler, new CaptureScreenshot("/wd/hub/session/:sessionId/screenshot"));
    } // otherwise the request will be automatically forwarded to the device

    register(getHandler, new InspectorTreeHandler("/inspector/session/:sessionId/tree"));
    register(getHandler, new InspectorScreenshotHandler("/inspector/session/:sessionId/screenshot"));
    register(getHandler, new InspectorUiHandler("/inspector/session/:sessionId"));
    register(deleteHandler, new DeleteSessionHandler("/wd/hub/session/:sessionId"));
    register(redirectHandler, new ProxyToDeviceHandler("/wd/hub/session/"));

    register(postHandler, new GetLogs("/wd/hub/session/:sessionId/log"));
    register(postHandler, new AdbSendKeyEvent("/wd/hub/session/:sessionId/selendroid/adb/sendKeyEvent"));
    register(postHandler, new AdbSendText("/wd/hub/session/:sessionId/selendroid/adb/sendText"));
    register(postHandler, new AdbTap("/wd/hub/session/:sessionId/selendroid/adb/tap"));
    register(postHandler, new AdbExecuteShellCommand(
        "/wd/hub/session/:sessionId/selendroid/adb/executeShellCommand"));
    register(postHandler, new NetworkConnectionHandler("/wd/hub/session/:sessionId/network_connection"));
  }

  @Override
  public void handleRequest(HttpRequest request, HttpResponse response,
      BaseRequestHandler foundHandler) {
    BaseRequestHandler handler = null;
    if ("/favicon.ico".equals(request.uri()) && foundHandler == null) {
      response.setStatus(404);
      response.end();
      return;
    }
    if ("/inspector/".equals(request.uri()) || "/inspector".equals(request.uri())) {
      if (driver.getActiveSessions().isEmpty()) {
        response.setStatus(200);
        response
            .setContent("Selendroid inspector can only be used if there is an active test session running. "
                + "To start a test session, add a break point into your test code and run the test in debug mode.");
        response.end();
        return;
      } else {
        // response.setStatus(302);
        String session = driver.getActiveSessions().get(0).getSessionId();

        String newSessionUri =
            "http://" + request.header("Host") + "/inspector/session/" + session + "/";
        log.info("new Inspector URL: " + newSessionUri);
        response.sendTemporaryRedirect(newSessionUri);
        response.end();
        return;
      }
    }
    if (foundHandler == null) {
      if (!redirectHandler.isEmpty()) {
        // trying to find an redirect handler
        for (Map.Entry<String, BaseRequestHandler> entry : redirectHandler.entrySet()) {
          if (request.uri().startsWith(entry.getKey())) {
            String sessionId =
                getParameter("/wd/hub/session/:sessionId", request.uri(), ":sessionId", false);
            handler = entry.getValue();
            if (driver.isValidSession(sessionId)) {
              request.data().put(SESSION_ID_KEY, sessionId);
            }
          }
        }
      }
      if (handler == null) {
        response.setStatus(404);
        response.end();
        return;
      }
    } else {
      handler = foundHandler;
    }

    String sessionId = getParameter(handler.getMappedUri(), request.uri(), ":sessionId");
    if (sessionId != null) {
      request.data().put(SESSION_ID_KEY, sessionId);
    }
    request.data().put(DRIVER_KEY, driver);

    Response result;
    try {
      result = handler.handle(request);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Error handling request", e);
      replyWithServerError(response);
      return;
    }
    if (result instanceof SelendroidResponse) {
      handleResponse(request, response, (SelendroidResponse) result);
    } else if (result instanceof JsResult) {
      JsResult js = (JsResult) result;
      response.setContentType("application/x-javascript");
      response.setEncoding(Charset.forName("UTF-8"));
      response.setContent(js.render());
      response.end();
    } else if (result instanceof UiResponse) {
      UiResponse uiResponse = (UiResponse) result;
      response.setEncoding(Charset.forName("UTF-8"));
      response.setStatus(200);

      if (uiResponse != null) {
        if (uiResponse.getObject() instanceof byte[]) {
          response.setContentType("image/png");
          byte[] data = (byte[]) uiResponse.getObject();
          response.setContent(data);
        } else {
          response.setContentType("text/html");
          String resultString = uiResponse.render();
          response.setContent(resultString);
        }
      }
      response.end();
    } else {
      log.log(Level.SEVERE, "Unknown response type: " + result.getClass().getSimpleName());
      replyWithServerError(response);
    }
  }
}
