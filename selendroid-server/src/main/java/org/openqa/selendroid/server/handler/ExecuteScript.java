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
package org.openqa.selendroid.server.handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.exceptions.UnsupportedOperationException;
import org.openqa.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

public class ExecuteScript extends RequestHandler {

  public ExecuteScript(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    SelendroidLogger.log("execute script command");
    JSONObject payload = getPayload();
    String script = payload.getString("script");
    JSONArray args = payload.optJSONArray("args");
    Object value = null;
    try {
      if (args.length() > 0) {
        value = getSelendroidDriver().executeScript(script, args);
      } else {
        value = getSelendroidDriver().executeScript(script);
      }
    } catch (UnsupportedOperationException e) {
      return new Response(getSessionId(), 13, e);
    }
    if (value instanceof String) {
      String result = (String) value;
      if (result.contains("value")) {
        JSONObject json = new JSONObject(result);
        int status = json.optInt("status");
        if (0 != status) {
          return new Response(getSessionId(), status, new SelendroidException(
              json.optString("value")));
        }
        if (json.isNull("value")) {
          return new Response(getSessionId(), null);
        } else {
          return new Response(getSessionId(), json.get("value"));
        }
      }
    }

    return new Response(getSessionId(), value);
  }
}
