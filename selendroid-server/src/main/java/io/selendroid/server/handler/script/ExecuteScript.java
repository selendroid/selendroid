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
package io.selendroid.server.handler.script;

import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.UnsupportedOperationException;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

public class ExecuteScript extends RequestHandler {

  public ExecuteScript(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handle(HttpRequest request) throws JSONException {
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
      return new SelendroidResponse(getSessionId(request), 13, e);
    }
    if (value instanceof String) {
      String result = (String) value;
      if (result.contains("value")) {
        JSONObject json = new JSONObject(result);
        int status = json.optInt("status");
        if (0 != status) {
          return new SelendroidResponse(getSessionId(request), status, new SelendroidException(
              json.optString("value")));
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
