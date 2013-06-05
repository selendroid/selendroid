/*
 * Copyright 2013 selendroid committers.
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

/*
 * Copyright 2013 selendroid committers.
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
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.BaseSelendroidServerHandler;
import io.selendroid.server.Response;
import io.selendroid.server.UiResponse;
import io.selendroid.server.model.ActiveSession;

import java.util.logging.Logger;

import org.json.JSONException;
import org.webbitserver.HttpRequest;

public class InspectorUiHandler extends BaseSelendroidServerHandler {
  private static final Logger log = Logger.getLogger(InspectorUiHandler.class.getName());

  public InspectorUiHandler(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    String sessionId = getSessionId();
    log.info("inspector command, sessionId: " + sessionId);

    ActiveSession session = null;
    if (sessionId == null || sessionId.isEmpty() == true) {
      if (getSelendroidDriver().getActiceSessions() != null
          && getSelendroidDriver().getActiceSessions().size() >= 1) {
        session = getSelendroidDriver().getActiceSessions().get(0);
        log.info("Selected sessionId: " + session.getSessionKey());
      }
    } else {
      session = getSelendroidDriver().getActiveSession(sessionId);
    }
    if (session == null) {
      throw new SelendroidException("No active test session was found.");
    }
    String url = "http://localhost:" + session.getSelendroidServerPort() + "/inspector";

    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">");
    html.append("<html><head><title>Selendroid Inspector</title></head>");
    html.append("<frameset rows='100%, *' frameborder=no framespacing=0 border=0>");
    html.append("<frame src='");
    html.append(url);
    html.append("' name=mainwindow frameborder=no framespacing=0 marginheight=0 marginwidth=0>");
    html.append("</frame></frameset><noframes>");
    html.append("<p>Frames are not supported by your browser. <a href='" + url
        + "'>Click here</a></p>");
    html.append("</noframes></html>");
    return new UiResponse(session.getSessionKey(), html.toString());
  }
}
