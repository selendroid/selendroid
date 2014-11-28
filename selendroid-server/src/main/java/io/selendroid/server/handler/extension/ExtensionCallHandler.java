package io.selendroid.server.handler.extension;

import io.selendroid.server.common.BaseRequestHandler;
import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.StatusCode;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.extension.ExtensionLoader;
import io.selendroid.server.handler.SafeRequestHandler;
import io.selendroid.server.util.SelendroidLogger;

import org.json.JSONException;
import org.json.JSONObject;

public class ExtensionCallHandler extends SafeRequestHandler {
  private final ExtensionLoader loader;

  public ExtensionCallHandler(
      String uri,
      ExtensionLoader loader) {
    super(uri);
    this.loader = loader;
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    JSONObject payload = getPayload(request);
    String handlerClassName = payload.getString("handlerName");
    BaseRequestHandler handler;
    try {
      handler = loader.loadHandler(handlerClassName, request.uri());
    } catch (Exception e) {
      return new SelendroidResponse(getSessionId(request), StatusCode.UNKNOWN_ERROR, e);
    }
    SelendroidLogger.info("forwarding to extension handler " + handlerClassName);
    return handler.handle(request);
  }

}
