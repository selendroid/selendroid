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
package io.selendroid.standalone.server.handler;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import io.netty.handler.codec.http.HttpMethod;
import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.StatusCode;
import io.selendroid.server.common.exceptions.AppCrashedException;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.standalone.android.AndroidDevice;
import io.selendroid.standalone.android.InstrumentationProcessOutput;
import io.selendroid.standalone.server.BaseSelendroidStandaloneHandler;
import io.selendroid.standalone.server.model.ActiveSession;
import io.selendroid.standalone.server.util.HttpClientUtil;

import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Proxies the request as-is to the device.
 */
public class ProxyToDeviceHandler extends BaseSelendroidStandaloneHandler {
  private static final Logger log = Logger.getLogger(ProxyToDeviceHandler.class.getName());

  private static final long PROXY_REQUEST_ATTEMPT_TIMEOUT_MS = 10000;
  private static final long PROXY_REQUEST_ATTEMPT_INTERVAL_MS = 200;

  public ProxyToDeviceHandler(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handleRequest(HttpRequest request, JSONObject payload) throws JSONException {
    String sessionId = getSessionId(request);

    if (sessionId == null || sessionId.isEmpty()) {
      String dataKeys = Joiner.on(", ").join(request.data().keySet());

      log.warning("Unable to retrieve session id from request with data: [" + dataKeys + "]");

      return respondWithFailure(
          sessionId, new SelendroidException("No session id passed to the request."));
    }

    final ActiveSession session = getActiveSession(request);
    if (session == null) {
      return respondWithFailure(sessionId,
          new SelendroidException("No session found for sessionId: " + sessionId));
    }
    if (session.isInvalid()) {
      return respondWithFailure(sessionId,
          new SelendroidException(
              "The test session has been marked as invalid. " +
                  "This happens if a hardware device was disconnected but a " +
                  "test session was still active on the device."));
    }
    String url = "http://localhost:" + session.getSelendroidServerPort() + request.uri();

    String method = request.method();

    final AndroidDevice device = session.getDevice();

    try {
      Wait<AndroidDevice> wait =
        new FluentWait<AndroidDevice>(device)
          .withTimeout(PROXY_REQUEST_ATTEMPT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
          .pollingEvery(PROXY_REQUEST_ATTEMPT_INTERVAL_MS, TimeUnit.MILLISECONDS);
      return wait.until(new Function<AndroidDevice, Response>() {
        @Override
        public Response apply(AndroidDevice device) {
          try {
            // Check if the instrumentation process died in the middle of the request
            if (session.instrumentationProcessFinished()) {
              return respondWithInstrumentationProcessFinished(
                sessionId,
                session.getInstrumentationProcessOutput(),
                session.getInstrumentationProcessError(),
                device);
            }

            JSONObject response = proxyRequestToDevice(request, session, url, method);
            if (response == null) { // Unknown command
              return new SelendroidResponse(sessionId, StatusCode.UNKNOWN_COMMAND);
            }

            Object value = response.opt("value");
            int statusCode = response.getInt("status");
            log.fine(
              String.format(
                "Response from selendroid-server, status %d:\n%s",
                statusCode,
                value));

            return new SelendroidResponse(sessionId, StatusCode.fromInteger(statusCode), value);
          } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to proxy request to Selendroid Server, retrying.", e);
            return null;
          }
        }
      });
    } catch (TimeoutException e) {
      // Check if we timed out because of the instrumentation process dying
      if (session.instrumentationProcessFinished()) {
        return respondWithInstrumentationProcessFinished(
          sessionId,
          session.getInstrumentationProcessOutput(),
          session.getInstrumentationProcessError(),
          device);
      }

      // Last resort, we really don't know what happened
      return respondWithFailure(
        sessionId,
        new SelendroidException("Selendroid server on the device became unreachable"));
    }
  }

  private SelendroidResponse respondWithInstrumentationProcessFinished(
    String sessionId,
    String output,
    Exception error,
    AndroidDevice device) throws JSONException {
    InstrumentationProcessOutput instrumentationOutput =
      InstrumentationProcessOutput.parse(output);

    if (error != null) {
      return respondWithFailure(
        sessionId,
        new SelendroidException(
          "Failed to execute instrument command, output:\n" +
          instrumentationOutput.getFullOutput(),
          error));
    }

    return respondWithFailure(
      sessionId,
      InstrumentationProcessOutput
        .getInstrumentationProcessError(
          instrumentationOutput,
          device));
  }


  private SelendroidResponse respondWithFailure(String sessionId, Exception e) throws JSONException {
    return new SelendroidResponse(sessionId, StatusCode.UNKNOWN_ERROR, e);
  }

  private JSONObject proxyRequestToDevice(HttpRequest request, ActiveSession session, String url, String method)
      throws Exception {
    HttpResponse r;
    if ("get".equalsIgnoreCase(method)) {
      log.fine("Proxy GET to the device: " + url);
      r = HttpClientUtil.executeRequest(url, HttpMethod.GET);
    } else if ("post".equalsIgnoreCase(method)) {
      JSONObject payload = getPayload(request);
      log.fine("Proxy POST to the device: " + url + ", payload:\n" + payload);
      r = HttpClientUtil.executeRequestWithPayload(
          url, session.getSelendroidServerPort(), HttpMethod.POST, payload.toString());
    } else if ("delete".equalsIgnoreCase(method)) {
      log.fine("Proxy DELETE to the device: " + url);
      r = HttpClientUtil.executeRequest(url, HttpMethod.DELETE);
    } else {
      throw new SelendroidException("HTTP method not supported: " + method);
    }
    if (r.getStatusLine().getStatusCode() == 404) { // Unknown command
      return null;
    }
    return HttpClientUtil.parseJsonResponse(r);
  }
}
