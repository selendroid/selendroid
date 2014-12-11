package io.selendroid.server.handler;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.model.TrackBall;
import io.selendroid.server.util.SelendroidLogger;

import org.json.JSONException;
import org.json.JSONObject;

public class Roll extends SafeRequestHandler {

	public Roll(String mappedUri) {
		super(mappedUri);
	}

	@Override
	public Response safeHandle(HttpRequest request) throws JSONException {
		SelendroidLogger.info("roll event");

		JSONObject payload = getPayload(request);
		TrackBall trackBall = getSelendroidDriver(request).getTrackBall();
		int dx = payload.getInt("dx");
		int dy = payload.getInt("dy");
		trackBall.roll(dx, dy);

		return new SelendroidResponse(getSessionId(request), "");
	}
}
