package io.selendroid.server.handler.network;

import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import org.json.JSONException;
import org.webbitserver.HttpRequest;

public class GetNetworkConnectionType extends RequestHandler {
  public GetNetworkConnectionType(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handle(HttpRequest request) throws JSONException {
    return new SelendroidResponse(getSessionId(request), getSelendroidDriver(request).isAirplaneMode() ? 1 : 6);
  }
}
