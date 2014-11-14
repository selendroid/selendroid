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
package io.selendroid.server.handler;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.netty.handler.codec.http.HttpMethod;
import io.selendroid.android.AndroidDevice;
import io.selendroid.exceptions.AppCrashedException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.BaseSelendroidServerHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.StatusCode;
import io.selendroid.server.model.ActiveSession;
import io.selendroid.server.util.HttpClientUtil;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.logging.LogEntry;
import io.selendroid.server.http.HttpRequest;

import java.net.SocketException;
import java.util.logging.Logger;

public class RequestRedirectHandler extends BaseSelendroidServerHandler {
  private static final Logger log = Logger.getLogger(RequestRedirectHandler.class.getName());

  public RequestRedirectHandler(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handle(HttpRequest request) throws JSONException {
    String sessionId = getSessionId(request);
    log.info("forward request command: for session " + sessionId);

    ActiveSession session = getSelendroidDriver(request).getActiveSession(sessionId);
    if (session == null) {
      return respondWithRedirectFailure(sessionId,
          new SelendroidException("No session found for given sessionId: " + sessionId));
    }
    if (session.isInvalid()) {
      return respondWithRedirectFailure(sessionId,
          new SelendroidException(
              "The test session has been marked as invalid. " +
              "This happens if a hardware device was disconnected but a " +
              "test session was still active on the device."));
    }
    String url = "http://localhost:" + session.getSelendroidServerPort() + request.uri();

    String method = request.method();

    JSONObject response = null;

    int retries = 3;
    while (retries-- > 0) {
      try {
        response = redirectRequest(request, session, url, method);
        break;
      } catch (Exception e) {
        if (retries == 0) {
          AndroidDevice device = session.getDevice();

          String crashMessage = device.getCrashLog();
          if (!crashMessage.isEmpty()) {
            return respondWithRedirectFailure(sessionId, new AppCrashedException(crashMessage));
          }

          if (device.isLoggingEnabled()) {
            log.info("getting logs");
            device.setVerbose();
            for (LogEntry le : device.getLogs()) {
              System.out.println(le.getMessage());
            }
          }

          if (e instanceof SocketException) {
            return respondWithSelendroidServerUnreachable(sessionId, session.getDevice());
          } else if (e instanceof NoHttpResponseException) {
            return respondWithSelendroidServerUnreachable(sessionId, session.getDevice());
          } else {
            return respondWithRedirectFailure(sessionId, new SelendroidException(
                "Unexpected error communicating with selendroid server on the device", e));
          }
        } else {
          log.severe("failed to forward request to Selendroid Server: " + e.getMessage());
        }
      }
    }
    if (response == null) {
      return respondWithRedirectFailure(sessionId, new SelendroidException(
          "Selendroid server on the device became unreachable"));
    }
    Object value = response.opt("value");
    if (value != null) {
      String displayed = String.valueOf(value);
      // 2 lines of an 80 column display
      if (displayed.length() > 160) {
        displayed = displayed.substring(0, 157) + "...";
      }
      log.info("return value from selendroid android server: " + displayed);
    }
    int status = response.getInt("status");

    log.fine("return value from selendroid android server: " + value);
    log.fine("return status from selendroid android server: " + status);

    return new SelendroidResponse(sessionId, StatusCode.fromInteger(status), value);
  }

  /**
   * selendroid-server can't be reached and there is no crash log file.
   */
  private SelendroidResponse respondWithSelendroidServerUnreachable(String sessionId, AndroidDevice device)
      throws JSONException {
    String message =
        "The selendroid server on the device became unreachable and there is no crash log from Android's " +
        "uncaught exception handler. This can mean:\n" +
        "- The test is trying to use a driver associated to a process that has finished " +
        "(has the app been killed by the test?)\n" +
        "- The app has been killed by the OS abruptly or there was a native crash (look at logcat)";
    try {
      String psOutput = device.listRunningThirdPartyProcesses();
      if (!Strings.isNullOrEmpty(psOutput)) {
        message += "\nCurrently running processes excluding system processes (via 'adb shell ps'):\n" + psOutput;
      }
    } catch (Exception e) {
      message += "\nCould not get list of running processes: " + e.getMessage();
    }
    return respondWithRedirectFailure(sessionId, new SelendroidException(message));
  }


  private SelendroidResponse respondWithRedirectFailure(String sessionId, Exception e) throws JSONException {
    return new SelendroidResponse(sessionId, StatusCode.UNKNOWN_ERROR, e);
  }

  private JSONObject redirectRequest(HttpRequest request, ActiveSession session, String url, String method)
      throws Exception {

    HttpResponse r;
    if ("get".equalsIgnoreCase(method)) {
      log.info("GET redirect to: " + url);
      r = HttpClientUtil.executeRequest(url, HttpMethod.GET);
    } else if ("post".equalsIgnoreCase(method)) {
      log.info("POST redirect to: " + url);
      JSONObject payload = getPayload(request);
      log.info("Payload? " + payload);
      r = HttpClientUtil.executeRequestWithPayload(
          url, session.getSelendroidServerPort(), HttpMethod.POST, payload.toString());
    } else if ("delete".equalsIgnoreCase(method)) {
      log.info("DELETE redirect to: " + url);
      r = HttpClientUtil.executeRequest(url, HttpMethod.DELETE);
    } else {
      throw new SelendroidException("HTTP method not supported: " + method);
    }
    return HttpClientUtil.parseJsonResponse(r);
  }
}
