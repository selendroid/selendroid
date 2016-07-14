package io.selendroid.server.common.exceptions;

import org.json.JSONObject;

public abstract class BaseAccessibilityExtension {
    public BaseAccessibilityExtension() {

    }
    public abstract JSONObject execute(JSONObject payload);
}
