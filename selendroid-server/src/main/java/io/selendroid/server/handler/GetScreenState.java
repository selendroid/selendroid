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
  public GetScreenState(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    SelendroidLogger.log("Get screen state");

    TouchScreen screen = getSelendroidDriver().getTouch();
    float brightness = screen.getBrightness();

    int percentage = (int) (brightness * 100);

    return new SelendroidResponse(getSessionId(), percentage);
  }
}
