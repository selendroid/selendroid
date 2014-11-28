package io.selendroid.server.handler.network;

import org.json.JSONException;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.handler.SafeRequestHandler;

public class GetNetworkConnectionType extends SafeRequestHandler {
  public GetNetworkConnectionType(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    return new SelendroidResponse(getSessionId(request), getSelendroidDriver(request).isAirplaneMode() ? 1 : 6);
  }
}
