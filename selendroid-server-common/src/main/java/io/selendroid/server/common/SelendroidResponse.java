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
package io.selendroid.server.common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SelendroidResponse implements Response {
  /**
   * This is prepended to messages for failures caused by:
   * <ul>
   * <li>Selendroid itself (usually this is bug that must be fixed)
   * <li>Unexpected conditions in the environment we're running in
   * </ul>
   * If a client sees this they know the error is not their fault.
   */
  private static final String CATCH_ALL_ERROR_MESSAGE_PREFIX = "CATCH_ALL: ";
  private String sessionId;
  private int status;
  private Object value;

  private SelendroidResponse(String sessionId, int status, Throwable e) throws JSONException {
    this.sessionId = sessionId;
    this.status = status;
    this.value = buildErrorValue(e, status);
  }

  private SelendroidResponse(String sessionId, int status, Throwable e, String messagePrefix) throws JSONException {
    this.sessionId = sessionId;
    this.status = status;
    this.value = buildErrorValue(e, status, messagePrefix);
  }

  private SelendroidResponse(String sessionId, int status, Object value) {
    this.sessionId = sessionId;
    this.status = status;
    this.value = value;
  }

  public SelendroidResponse(String sessionId, Object value) {
    this(sessionId, 0, value);
  }

  public SelendroidResponse(String sessionId, StatusCode status, JSONObject value) {
    this(sessionId, status.getCode(), value);
  }

  public SelendroidResponse(String sessionId, StatusCode status, Object value) {
    this(sessionId, status.getCode(), value);
  }

  public SelendroidResponse(String sessionId, StatusCode status, Throwable e) throws JSONException {
    this(sessionId, status.getCode(), e);
  }

  /**
   * It is currently hard to detect whether a test failed because of a legitimate
   * error by a developer or because something is going wrong in selendroid
   * internals. This response marks error responses from the server that indicate
   * something has gone wrong in the internals of selendroid.
   */
  public static SelendroidResponse forCatchAllError(String sessionId, Throwable e) {
    try {
      return new SelendroidResponse(sessionId, StatusCode.UNKNOWN_ERROR.getCode(), e, CATCH_ALL_ERROR_MESSAGE_PREFIX);
    } catch (JSONException err) {
      return new SelendroidResponse(sessionId, StatusCode.UNKNOWN_ERROR.getCode());
    }
  }

  @Override
  public String getSessionId() {
    return sessionId;
  }

  public int getStatus() {
    return status;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public String render() {
    JSONObject o = new JSONObject();
    try {
      if (sessionId != null) {
        o.put("sessionId", sessionId);
      }
      o.put("status", status);
      if (value != null) {
        o.put("value", value);
      }
    } catch (JSONException e) {
      System.out.println("Cannot render response: " + e.getMessage());
    }
    return o.toString();
  }

  private JSONObject buildErrorValue(Throwable e, int status) throws JSONException {
    return buildErrorValue(e, status, null);
  }

  private JSONObject buildErrorValue(Throwable e, int status, String messagePrefix) throws JSONException {
    JSONObject errorValue = new JSONObject();
    errorValue.put("class", e.getClass().getCanonicalName());

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);

    if (messagePrefix != null) {
      printWriter.append(messagePrefix);
    }

    // Also include the Selendroid stack trace. Only do this in case of unknown errors for easier debugging.
    // In case of an expected error the stack trace is unnecessary and users often find it confusing.
    if (status == StatusCode.UNKNOWN_ERROR.getCode()) {
      e.printStackTrace(printWriter);
    } else {
      printWriter.append(e.getMessage());
    }

    errorValue.put("message", stringWriter.toString());

    /*
      The WebDriver protocol does not define a way to add exception stack traces to responses.
      The workaround above puts the stack trace in the response message.
      Apparently Selenium's BeanToJsonConverter would also work.

      JSONArray stackTrace = new JSONArray();
      for (StackTraceElement el : t.getStackTrace()) {
          JSONObject frame = new JSONObject();
          frame.put("lineNumber", el.getLineNumber());
          frame.put("className", el.getClassName());
          frame.put("methodName", el.getMethodName());
          frame.put("fileName", el.getFileName());
          stackTrace.put(frame);
      }
      errorValue.put("stackTrace", stackTrace);
    */
    return errorValue;
  }
}
