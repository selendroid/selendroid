package io.selendroid.standalone.server.handler;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.standalone.server.BaseSelendroidStandaloneHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class CurrentContextHandler extends BaseSelendroidStandaloneHandler {
  public CurrentContextHandler(String mappedUri, BaseSelendroidStandaloneHandler proxyHanlder) {
    super(mappedUri);
  }

  @Override
  protected Response handleRequest(HttpRequest request, JSONObject payload) throws JSONException {
    return null;
  }
}
