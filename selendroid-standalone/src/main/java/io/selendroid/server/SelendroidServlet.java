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

import io.selendroid.SelendroidConfiguration;
import io.selendroid.server.handler.CaptureScreenshot;
import io.selendroid.server.handler.CreateSessionHandler;
import io.selendroid.server.handler.DeleteSessionHandler;
import io.selendroid.server.handler.GetCapabilities;
import io.selendroid.server.handler.GetLogTypes;
import io.selendroid.server.handler.GetLogs;
import io.selendroid.server.handler.InspectorScreenshotHandler;
import io.selendroid.server.handler.InspectorTreeHandler;
import io.selendroid.server.handler.InspectorUiHandler;
import io.selendroid.server.handler.ListSessionsHandler;
import io.selendroid.server.handler.RequestRedirectHandler;
import io.selendroid.server.model.SelendroidStandaloneDriver;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class SelendroidServlet extends BaseServlet {
  private static final Logger log = Logger.getLogger(SelendroidServlet.class.getName());
  protected Map<String, Class<? extends BaseRequestHandler>> redirectHandler =
      new HashMap<String, Class<? extends BaseRequestHandler>>();
  private SelendroidStandaloneDriver driver;
  private SelendroidConfiguration conf;

  public SelendroidServlet(SelendroidStandaloneDriver driver, SelendroidConfiguration conf) {
    this.driver = driver;
    this.conf = conf;
    init();
  }

  protected void init() {
    postHandler.put("/wd/hub/session", CreateSessionHandler.class);
    getHandler.put("/wd/hub/sessions", ListSessionsHandler.class);
    getHandler.put("/wd/hub/session/:sessionId", GetCapabilities.class);

    getHandler.put("/wd/hub/session/:sessionId/log/types", GetLogTypes.class);
    postHandler.put("/wd/hub/session/:sessionId/log", GetLogs.class);
    if (conf.isDeviceScreenshot() == false) {
      getHandler.put("/wd/hub/session/:sessionId/screenshot", CaptureScreenshot.class);
    }// otherwise the request will be automatically forwarded to the device


    getHandler.put("/inspector/session/:sessionId/tree", InspectorTreeHandler.class);
    getHandler.put("/inspector/session/:sessionId/screenshot", InspectorScreenshotHandler.class);
    getHandler.put("/inspector/session/:sessionId", InspectorUiHandler.class);
    deleteHandler.put("/wd/hub/session/:sessionId", DeleteSessionHandler.class);
    redirectHandler.put("/wd/hub/session/", RequestRedirectHandler.class);
  }

  @Override
  public void handleRequest(HttpRequest request, HttpResponse response,
      BaseRequestHandler foundHandler) {
    BaseRequestHandler handler = null;
    if ("/favicon.ico".equals(request.uri()) && foundHandler == null) {
      response.status(404);
      response.end();
      return;
    }
    if ("/inspector/".equals(request.uri()) || "/inspector".equals(request.uri())) {
      if (driver.getActiceSessions().isEmpty()) {
        response.status(200);
        response
            .content(
                "Selendroid inspector can only be used if there is an active test session running. "
                    + "To start a test session, add a break point into your test code and run the test in debug mode.")
            .end();
        return;
      } else {
        response.status(302);
        String session = driver.getActiceSessions().get(0).getSessionKey();

        String newSessionUri =
            "http://" + request.header("Host") + "/inspector/session/" + session + "/";
        log.info("new Inspector URL: " + newSessionUri);
        response.header("location", newSessionUri).end();
        return;
      }
    }
    if (foundHandler == null) {
      if (redirectHandler.isEmpty() == false) {
        // trying to find an redirect handler
        for (Map.Entry<String, Class<? extends BaseRequestHandler>> entry : redirectHandler
            .entrySet()) {
          if (request.uri().startsWith(entry.getKey())) {
            String sessionId =
                getParameter("/wd/hub/session/:sessionId", request.uri(), ":sessionId", false);
            handler = instantiateHandler(entry, request);
            if (driver.isValidSession(sessionId)) {
              request.data().put(SESSION_ID_KEY, sessionId);
            }
          }
        }
      }
      if (handler == null) {
        response.status(404).end();
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
      result = handler.handle();
    } catch (Exception e) {
      e.printStackTrace();
      log.severe("Error occurred while handling request: " + e.fillInStackTrace());
      replyWithServerError(response);
      return;
    }
    if (result instanceof SelendroidResponse) {
      handleResponse(request, response, (SelendroidResponse) result);
    } else if (result instanceof JsResult) {
      JsResult js = (JsResult) result;
      response.header("Content-type", "application/x-javascript").charset(Charset.forName("UTF-8"))
          .content(js.render()).end();
    } else {
      UiResponse uiResponse = (UiResponse) result;
      response.header("Content-Type", "text/html");
      response.charset(Charset.forName("UTF-8"));

      response.status(200);

      if (uiResponse != null) {
        if (uiResponse.getObject() instanceof byte[]) {
          byte[] data = (byte[]) uiResponse.getObject();
          response.header("Content-Length", data.length).content(data);
        } else {
          String resultString = uiResponse.render();
          response.content(resultString);

        }
      }
      response.end();
    }
  }

}
