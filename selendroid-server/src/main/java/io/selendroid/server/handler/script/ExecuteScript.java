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
package io.selendroid.server.handler.script;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.StatusCode;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.common.exceptions.UnsupportedOperationException;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.handler.SafeRequestHandler;
import io.selendroid.server.util.SelendroidLogger;

public class ExecuteScript extends SafeRequestHandler {

  public ExecuteScript(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    SelendroidLogger.info("execute script command");
    JSONObject payload = getPayload(request);
    String script = payload.getString("script");
    JSONArray args = payload.optJSONArray("args");
    Object value = null;
    try {
      if (args.length() > 0) {
        value = getSelendroidDriver(request).executeScript(script, args);
      } else {
        value = getSelendroidDriver(request).executeScript(script);
      }
    } catch (UnsupportedOperationException e) {
      return new SelendroidResponse(getSessionId(request), StatusCode.UNKNOWN_ERROR, e);
    }
    if (value instanceof String) {
      String result = (String) value;
      if (result.contains("value")) {
        JSONObject json = new JSONObject(result);
        int status = json.optInt("status");
        if (0 != status) {
          return new SelendroidResponse(getSessionId(request),
              StatusCode.fromInteger(status),
              new SelendroidException(json.optString("value")));
        }
        if (json.isNull("value")) {
          return new SelendroidResponse(getSessionId(request), null);
        } else {
          return new SelendroidResponse(getSessionId(request), json.get("value"));
        }
      }
    }

    return new SelendroidResponse(getSessionId(request), value);
  }
}
