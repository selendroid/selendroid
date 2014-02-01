/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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
package io.selendroid.server.handler;

import io.selendroid.server.BaseSelendroidServerHandler;
import io.selendroid.server.Response;
import io.selendroid.server.UiResponse;
import io.selendroid.server.inspector.BaseInspectorViewRenderer;
import io.selendroid.server.model.ActiveSession;
import org.json.JSONException;
import org.webbitserver.HttpRequest;

import java.util.logging.Logger;

public class InspectorUiHandler extends BaseSelendroidServerHandler {
  private static final Logger log = Logger.getLogger(InspectorUiHandler.class.getName());

  public InspectorUiHandler(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handle(HttpRequest request) throws JSONException {
    String sessionId = getSessionId(request);
    log.info("inspector command, sessionId: " + sessionId);
    ActiveSession session = null;
    if (sessionId == null || sessionId.isEmpty()) {
      if (getSelendroidDriver(request).getActiveSessions() != null
          && getSelendroidDriver(request).getActiveSessions().size() >= 1) {
        session = getSelendroidDriver(request).getActiveSessions().get(0);
        log.info("Selected sessionId: " + session.getSessionKey());
      } else {
        return new UiResponse(
            "",
            "Selendroid inspector can only be used if there is an active test session running. "
                + "To start a test session, add a break point into your test code and run the test in debug mode.");
      }
    } else {
      if (getSelendroidDriver(request).isValidSession(sessionId)) {
        session = getSelendroidDriver(request).getActiveSession(sessionId);
      } else {
        return new UiResponse(
            "",
            "You are using an invalid session key. Please open the inspector with the base uri: <IpAddress>:<Port>/inspector");
      }
    }
    return new UiResponse(sessionId != null ? sessionId : "",
        new MyInspectorViewRenderer(session).buildHtml(request));
  }

  public class MyInspectorViewRenderer extends BaseInspectorViewRenderer {
    private ActiveSession session;

    public MyInspectorViewRenderer(ActiveSession session) {
      this.session = session;
    }

    public String getResource(String name) {
      return "http://localhost:" + session.getSelendroidServerPort() + "/inspector/resources/"
          + name;
    }

    public String getScreen(HttpRequest request) {
      return "http://" + request.header("Host") + "/inspector/session/" + session.getSessionKey()
          + "/screenshot";
    }
  }
}
