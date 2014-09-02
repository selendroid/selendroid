package io.selendroid.server.handler.timeouts;

import io.selendroid.ServerInstrumentation;
import io.selendroid.server.SafeRequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.StatusCode;
import org.json.JSONException;
import io.selendroid.server.http.HttpRequest;

public class TimeoutsHandler extends SafeRequestHandler {
  public TimeoutsHandler(String uri) {
    super(uri);
  }
  public Response safeHandle(HttpRequest request) throws JSONException {
    long timeout = getPayload(request).getLong("ms");
    String type = getPayload(request).getString("type");
    if (type.equals("script")) {
      getSelendroidDriver(request).setAsyncTimeout(timeout);
    } else if (type.equals("implicit")) {
      ServerInstrumentation.getInstance().setImplicitWait(timeout);
    } else {
      return new SelendroidResponse(getSessionId(request),
          StatusCode.UNKNOWN_COMMAND,
          new Exception("Unsupported timeout type: " + type));
    }
    return new SelendroidResponse(getSessionId(request), null);
  }
}
