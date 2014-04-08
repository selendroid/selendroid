package io.selendroid.server.handler.script;

import io.selendroid.exceptions.*;
import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.util.SelendroidLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

public class ExecuteAsyncScript extends RequestHandler {
  public ExecuteAsyncScript(String mappedUri) {
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
        value = getSelendroidDriver(request).executeAsyncScript(script, args);
    } catch (io.selendroid.exceptions.UnsupportedOperationException e) {
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
