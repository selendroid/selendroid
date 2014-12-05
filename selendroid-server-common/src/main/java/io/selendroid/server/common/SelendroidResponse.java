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
  private static final String CATCH_ALL_ERROR_MESSAGE = "CATCH_ALL: ";
  private String sessionId;
  private int status;
  private Object value;

  protected SelendroidResponse() {
  }

  private SelendroidResponse(String sessionId, int status, Throwable e) throws JSONException {
    this.value = buildErrorValue(e);
    this.sessionId = sessionId;
    this.status = status;
  }

  private SelendroidResponse(String sessionId, int status, Throwable e, String messagePrefix) throws JSONException {
    this.value = buildErrorValue(e, messagePrefix);
    this.sessionId = sessionId;
    this.status = status;
  }

  private SelendroidResponse(String sessionId, int status, Object value) {
    this.sessionId = sessionId;
    this.status = status;
    this.value = value;
  }

  private SelendroidResponse(String sessionId, int status) {
    this.sessionId = sessionId;
    this.status = status;
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
  public static SelendroidResponse forCatchAllError(String sessionId, StatusCode status, Throwable e) {
    try {
      return new SelendroidResponse(sessionId, status.getCode(), e, CATCH_ALL_ERROR_MESSAGE);
    } catch(JSONException err) {
      return new SelendroidResponse(sessionId, status);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see io.selendroid.server.Response#getSessionId()
   */
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

  /*
   * (non-Javadoc)
   *
   * @see io.selendroid.server.Response#render()
   */
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

  private JSONObject buildErrorValue(Throwable t) throws JSONException {
    return buildErrorValue(t, null);
  }

  private JSONObject buildErrorValue(Throwable t, String messagePrefix) throws JSONException {
    JSONObject errorValue = new JSONObject();
    errorValue.put("class", t.getClass().getCanonicalName());

    // TODO: Form exception in a way that will be unpacked nicely on the local end.
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    t.printStackTrace(printWriter);

    StringBuilder messageBuilder = new StringBuilder();

    if (messagePrefix != null) {
      messageBuilder.append(messagePrefix);
    }

    if (t.getMessage() != null) {
      messageBuilder.append(t.getMessage());
      messageBuilder.append("\n");
    }

    messageBuilder.append(stringWriter.toString());

    errorValue.put("message", messageBuilder.toString());
        /*
         * There is no easy way to attach exception 'cause' clauses here.
         * See workaround above which is used instead.
         */
//      JSONArray stackTrace = new JSONArray();
//      for (StackTraceElement el : t.getStackTrace()) {
//          JSONObject frame = new JSONObject();
//          frame.put("lineNumber", el.getLineNumber());
//          frame.put("className", el.getClassName());
//          frame.put("methodName", el.getMethodName());
//          frame.put("fileName", el.getFileName());
//          stackTrace.put(frame);
//      }
//      errorValue.put("stackTrace", stackTrace);

    return errorValue;
  }
}
