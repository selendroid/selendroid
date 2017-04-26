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
import org.openqa.selenium.logging.LogEntry;

import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Proxies the request as-is to the device.
 */
public class ProxyToDeviceHandler extends BaseSelendroidStandaloneHandler {
  private static final Logger log = Logger.getLogger(ProxyToDeviceHandler.class.getName());
  private static final long INSTRUMENTATION_PROCESS_TIMEOUT_MS = 2000;

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

    ActiveSession session = getActiveSession(request);
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

    JSONObject response = null;

    AndroidDevice device = session.getDevice();
    InstrumentationProcessOutput instrumentationProcessOutput;

    int retries = 3;
    while (retries-- > 0) {
      try {
        instrumentationProcessOutput = device.getInstrumentationProcessOutput();
        if (instrumentationProcessOutput.output != null) {
          return respondWithInstrumentationProcessFinished(
            sessionId,
            instrumentationProcessOutput
          );
        }

        response = proxyRequestToDevice(request, session, url, method);
        if (response == null) { // Unknown command
          return new SelendroidResponse(sessionId, StatusCode.UNKNOWN_COMMAND);
        }
        break;
      } catch (Exception e) {
        if (retries == 0) {
          String crashMessage = device.getCrashLog();
          if (!crashMessage.isEmpty()) {
            return respondWithFailure(sessionId, new AppCrashedException(crashMessage));
          }

          if (device.isLoggingEnabled()) {
            log.info("Failed to proxy request to the device, dumping logcat");
            device.setVerbose();
            for (LogEntry le : device.getLogs()) {
              System.out.println(le.getMessage());
            }
          }

          instrumentationProcessOutput = waitForInstrumentationProcess(device);
          if (instrumentationProcessOutput != null) {
            return respondWithInstrumentationProcessFinished(
              sessionId,
              instrumentationProcessOutput
            );
          }

          if (e instanceof SocketException) {
            return respondWithServerOnDeviceUnreachable(sessionId, device);
          } else if (e instanceof NoHttpResponseException) {
            return respondWithServerOnDeviceUnreachable(sessionId, device);
          } else {
            return respondWithFailure(sessionId, new SelendroidException(
                "Unexpected error communicating with selendroid server on the device", e));
          }
        } else {
          log.log(Level.SEVERE, "Failed to proxy request to Selendroid Server, retrying.", e);
        }
      }
    }
    if (response == null) {
      return respondWithFailure(sessionId, new SelendroidException(
          "Selendroid server on the device became unreachable"));
    }

    Object value = response.opt("value");
    int statusCode = response.getInt("status");
    log.fine(String.format("Response from selendroid-server, status %d:\n%s", statusCode, value));
    log.fine("return status from selendroid android server: " + statusCode);

    return new SelendroidResponse(sessionId, StatusCode.fromInteger(statusCode), value);
  }

  private SelendroidResponse respondWithInstrumentationProcessFinished(
    String sessionId,
    InstrumentationProcessOutput instrumentationProcessOutput
  ) throws JSONException {
    String message =
      "The instrumentation process has been terminated. This usually means " +
      "that the app has been killed by the OS or that there was " +
      "a native crash. See the output from the process below and check " +
      "logcat for more details:\n" +
      instrumentationProcessOutput.output;
    Exception exception = instrumentationProcessOutput.exception != null ?
      new SelendroidException(message, instrumentationProcessOutput.exception)
      : new SelendroidException(message);

    return respondWithFailure(sessionId, exception); 
  }

  private InstrumentationProcessOutput waitForInstrumentationProcess(final AndroidDevice device) {
    long start = System.currentTimeMillis();
    InstrumentationProcessOutput ret = device.getInstrumentationProcessOutput();
    while (ret.output == null) {
      ret = device.getInstrumentationProcessOutput();
      if ((System.currentTimeMillis() - start) >= INSTRUMENTATION_PROCESS_TIMEOUT_MS) {
        break;
      }
    }

    return ret;
  }

  /**
   * selendroid-server can't be reached and there is no crash log file.
   */
  private SelendroidResponse respondWithServerOnDeviceUnreachable(String sessionId, AndroidDevice device)
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
    return respondWithFailure(sessionId, new SelendroidException(message));
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
