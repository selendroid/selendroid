package io.selendroid.server.handler.extension;

import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.StatusCode;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.handler.SafeRequestHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class AccessibilityExtensionCallHandler extends SafeRequestHandler {
    public AccessibilityExtensionCallHandler(String mappedUri) {
        super(mappedUri);
    }

    @Override
    public Response safeHandle(HttpRequest request) throws JSONException {
        JSONObject payload = getPayload(request);
        String extensionName = payload.getString("extensionName");
        JSONObject result = ServerInstrumentation.getInstance().executeAccessibilityExtension(extensionName, payload);

        if (result.has("payload")) {
            return new SelendroidResponse(getSessionId(request), StatusCode.SUCCESS, result.getJSONObject("payload"));
        } else {
            return new SelendroidResponse(getSessionId(request),
                    StatusCode.UNKNOWN_ERROR,
                    new Exception("Call to " + extensionName + " failed"));
        }
    }
}
