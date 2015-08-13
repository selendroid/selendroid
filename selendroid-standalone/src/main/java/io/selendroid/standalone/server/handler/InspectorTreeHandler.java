/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.selendroid.standalone.server.handler;

import io.selendroid.standalone.server.BaseSelendroidStandaloneHandler;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import io.netty.handler.codec.http.HttpMethod;

import org.json.JSONException;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.UiResponse;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.standalone.server.JsResult;
import io.selendroid.standalone.server.model.ActiveSession;
import io.selendroid.standalone.server.util.HttpClientUtil;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InspectorTreeHandler extends BaseSelendroidStandaloneHandler {
  private static final Logger log = Logger.getLogger(InspectorTreeHandler.class.getName());

  public InspectorTreeHandler(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handleRequest(HttpRequest request, JSONObject payload) throws JSONException {
    String sessionId = getSessionId(request);
    log.info("inspector tree handler, sessionId: " + sessionId);

    ActiveSession session;
    if (sessionId == null || sessionId.isEmpty()) {
      if (getSelendroidDriver(request).getActiveSessions() != null
          && getSelendroidDriver(request).getActiveSessions().size() >= 1) {
        session = getSelendroidDriver(request).getActiveSessions().get(0);
        log.info("Selected sessionId: " + session.getSessionId());
      } else {
        return new UiResponse(
            "",
            "Selendroid inspector can only be used if there is an active test session running. "
                + "To start a test session, add a break point into your test code and run the test in debug mode.");
      }
    } else {
      session = getActiveSession(request);
    }

    try {
      HttpResponse r =
          HttpClientUtil.executeRequest(
              "http://" + session.getHostname() + ":" + session.getSelendroidServerPort() + "/inspector/tree", HttpMethod.GET);
      return new JsResult(EntityUtils.toString(r.getEntity(),Charset.forName("UTF-8")));
    } catch (Exception e) {
      log.log(Level.SEVERE, "Cannot get element tree for inspector", e);
      throw new SelendroidException(e);
    }
  }
}
