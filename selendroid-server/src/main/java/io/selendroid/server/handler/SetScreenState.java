package io.selendroid.server.handler;

import io.selendroid.server.SafeRequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.TouchScreen;
import org.json.JSONException;
import org.json.JSONObject;
import io.selendroid.server.http.HttpRequest;

/**
 * Allow the device's screen to be turned on or off.
 */
public class SetScreenState extends SafeRequestHandler {

  public SetScreenState(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    JSONObject payload = getPayload(request);
    int percentage = payload.getInt("brightness");

    float brightness = percentage / 100f;
    TouchScreen touch = getSelendroidDriver(request).getTouch();
    touch.setBrightness(brightness);

    return new SelendroidResponse(getSessionId(request), "");
  }
}
