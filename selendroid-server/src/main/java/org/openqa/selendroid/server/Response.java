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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Response {
  private String sessionId;
  private int status;
  private Object value;

  protected Response() {}

  public Response(String sessionId, int status, JsonObject value) {
    this.sessionId = sessionId;
    this.status = status;
    this.value = value;
  }

  public Response(String sessionId, int status, Exception e) {
    JsonObject value = new JsonObject();
    value.addProperty("message", e.getMessage());
    value.addProperty("class", e.getClass().getCanonicalName());

    JsonArray stacktace = new JsonArray();
    for (StackTraceElement el : e.getStackTrace()) {
      stacktace.add(new Gson().toJsonTree(el.toString()));
    }
    value.add("stacktrace", stacktace);
    this.value = value;
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
    JsonObject o = new JsonObject();
    if (sessionId != null) {
      o.addProperty("sessionId", sessionId);
    }
    o.addProperty("status", status);
    if (value != null) {
      if (value instanceof JsonElement) {
        o.add("value", (JsonElement) value);
      } else if (value instanceof String) {
        o.addProperty("value", (String) value);
      } else if (value instanceof Long) {
        o.addProperty("value", (Long) value);
      } else {
        o.addProperty("value", value.toString());
      }
    }
    return o.toString();
  }
}
