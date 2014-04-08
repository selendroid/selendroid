package io.selendroid.server.handler;

import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.TouchScreen;
import io.selendroid.util.SelendroidLogger;
import org.json.JSONException;
import org.webbitserver.HttpRequest;

/**
 * Determine whether the screen of the device is on or off.
 */
public class GetScreenState extends RequestHandler {

  public GetScreenState(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handle(HttpRequest request) throws JSONException {
    SelendroidLogger.info("Get screen state");

    TouchScreen screen = getSelendroidDriver(request).getTouch();
    float brightness = screen.getBrightness();

    int percentage = (int) (brightness * 100);

    return new SelendroidResponse(getSessionId(request), percentage);
  }
}
