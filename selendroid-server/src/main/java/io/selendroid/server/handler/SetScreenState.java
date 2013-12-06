package io.selendroid.server.handler;

import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.TouchScreen;
import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

/**
 * Allow the device's screen to be turned on or off.
 */
public class SetScreenState extends RequestHandler {

  public SetScreenState(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    JSONObject payload = getPayload();
    int percentage = payload.getInt("brightness");

    float brightness = percentage / 100f;
    TouchScreen touch = getSelendroidDriver().getTouch();
    touch.setBrightness(brightness);

    return new SelendroidResponse(getSessionId(), "");
  }
}
