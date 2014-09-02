package io.selendroid.server.handler.network;

import io.selendroid.server.SafeRequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import org.json.JSONException;
import io.selendroid.server.http.HttpRequest;

public class GetNetworkConnectionType extends SafeRequestHandler {
  public GetNetworkConnectionType(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    return new SelendroidResponse(getSessionId(request), getSelendroidDriver(request).isAirplaneMode() ? 1 : 6);
  }
}
