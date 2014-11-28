package io.selendroid.server.handler.script;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.StatusCode;
import io.selendroid.server.common.exceptions.*;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.handler.SafeRequestHandler;
import io.selendroid.server.util.SelendroidLogger;

public class ExecuteAsyncScript extends SafeRequestHandler {
  public ExecuteAsyncScript(String mappedUri) {
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
        value = getSelendroidDriver(request).executeAsyncScript(script, args);
    } catch (io.selendroid.server.common.exceptions.UnsupportedOperationException e) {
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
