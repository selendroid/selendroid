/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Response {
  private String sessionId;
  private int status;
  private Object value;

  protected Response() {}

  public Response(String sessionId, int status, JSONObject value) {
    this.sessionId = sessionId;
    this.status = status;
    this.value = value;
  }

  public Response(String sessionId, int status, Exception e) throws JSONException {
    JSONObject errorValue = new JSONObject();
    errorValue.put("message", e.getMessage());
    errorValue.put("class", e.getClass().getCanonicalName());


    JSONArray stacktace = new JSONArray();
    for (StackTraceElement el : e.getStackTrace()) {
      stacktace.put(el.toString());
    }
    errorValue.put("stacktrace", stacktace);
    this.value = errorValue;
    this.sessionId = sessionId;
    this.status = status;
  }

  public Response(String sessionId, Object value) {
    this.sessionId = sessionId;
    this.status = 0;
    this.value = value;
  }

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
  public String toString() {
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
      e.printStackTrace();
    }
    return o.toString();
  }
}
