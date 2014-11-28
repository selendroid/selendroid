package io.selendroid.server.handler.timeouts;

import org.json.JSONException;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.handler.SafeRequestHandler;

public class AsyncTimeoutHandler extends SafeRequestHandler {
  public AsyncTimeoutHandler(String mappedUri) {
    super(mappedUri);
  }
  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    getSelendroidDriver(request).setAsyncTimeout(getPayload(request).getLong("ms"));
    return new SelendroidResponse(getSessionId(request), null);
  }
}
