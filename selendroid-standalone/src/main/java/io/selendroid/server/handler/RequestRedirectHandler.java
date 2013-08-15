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
package io.selendroid.server.handler;

import io.selendroid.android.AndroidDevice;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.io.ShellCommand;
import io.selendroid.server.BaseSelendroidServerHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.ActiveSession;
import io.selendroid.server.util.HttpClientUtil;

import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.logging.LogEntry;
import org.webbitserver.HttpRequest;

public class RequestRedirectHandler extends BaseSelendroidServerHandler {
  private static final Logger log = Logger.getLogger(RequestRedirectHandler.class.getName());

  public RequestRedirectHandler(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    String sessionId = getSessionId();
    log.info("forward request command: for session " + sessionId);

    ActiveSession session = getSelendroidDriver().getActiveSession(sessionId);
    if (session == null) {
      return new SelendroidResponse(sessionId, 13, new SelendroidException(
          "No session found for given sessionId: " + sessionId));
    }
    if (session.isInvalid()) {
      return new SelendroidResponse(sessionId, 13, new SelendroidException(
          "The test session has been marked as invalid. "
              + "This happens if a hardware device was diconnected but a "
              + "test session was still active on the device."));
    }
    String url = "http://localhost:" + session.getSelendroidServerPort() + request.uri();

    String method = request.method();

    JSONObject response = null;

    int retries = 3;
    while (retries-- > 0) {
      try {
        response = redirectRequest(session, url, method);
        break;
      } catch (Exception e) {
        if (retries == 0) {
          AndroidDevice device = session.getDevice();
          System.out.println("getting logs");
          device.setVerbose();
          for (LogEntry le : device.getLogs()) {
            System.out.println(le.getMessage());
          }
          return new SelendroidResponse(sessionId, 13,
                  new SelendroidException(
                          "Error occured while communicating with selendroid server on the device: ",
                          e));
        } else {
          log.severe("failed to forward request to Selendroid Server");
        }
      }
    }
    Object value = response.opt("value");
    if (value != null) {
      log.info("return value from selendroid android server: " + value);
    }
    int status = response.getInt("status");

    log.fine("return value from selendroid android server: " + value);
    log.fine("return status from selendroid android server: " + status);

    return new SelendroidResponse(sessionId, status, value);
  }

  private JSONObject redirectRequest(ActiveSession session, String url, String method)
      throws Exception, JSONException {

    HttpResponse r = null;
    if ("get".equalsIgnoreCase(method)) {
      log.info("GET redirect to: " + url);
      r = HttpClientUtil.executeRequest(url, HttpMethod.GET);
    } else if ("post".equalsIgnoreCase(method)) {
      log.info("POST redirect to: " + url);
      JSONObject payload = getPayload();
      log.info("Payload? " + payload);
      r =
          HttpClientUtil.executeRequestWithPayload(url, session.getSelendroidServerPort(),
              HttpMethod.POST, payload.toString());

    } else if ("delete".equalsIgnoreCase(method)) {
      log.info("DELETE redirect to: " + url);
      r = HttpClientUtil.executeRequest(url, HttpMethod.DELETE);
    } else {
      throw new SelendroidException("Http method not supported.");
    }
    return HttpClientUtil.parseJsonResponse(r);
  }
}
