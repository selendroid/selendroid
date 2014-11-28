package io.selendroid.server.handler;

import io.selendroid.server.model.TouchScreen;
import io.selendroid.server.util.SelendroidLogger;

import org.json.JSONException;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.http.HttpRequest;

/**
 * Determine whether the screen of the device is on or off.
 */
public class GetScreenState extends SafeRequestHandler {

  public GetScreenState(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    SelendroidLogger.info("Get screen state");

    TouchScreen screen = getSelendroidDriver(request).getTouch();
    float brightness = screen.getBrightness();

    int percentage = (int) (brightness * 100);

    return new SelendroidResponse(getSessionId(request), percentage);
  }
}
