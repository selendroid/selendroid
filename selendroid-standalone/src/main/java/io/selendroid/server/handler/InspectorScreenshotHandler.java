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

import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.server.BaseSelendroidServerHandler;
import io.selendroid.server.Response;
import io.selendroid.server.UiResponse;
import io.selendroid.server.model.ActiveSession;

import java.util.logging.Logger;

import org.json.JSONException;
import org.webbitserver.HttpRequest;

public class InspectorScreenshotHandler extends BaseSelendroidServerHandler {
  private static final Logger log = Logger.getLogger(InspectorScreenshotHandler.class.getName());
  private ActiveSession session = null;

  public InspectorScreenshotHandler(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    String sessionId = getSessionId();
    log.info("inspector screenshot handler, sessionId: " + sessionId);

    if (sessionId == null || sessionId.isEmpty() == true) {
      if (getSelendroidDriver().getActiceSessions() != null
          && getSelendroidDriver().getActiceSessions().size() >= 1) {
        session = getSelendroidDriver().getActiceSessions().get(0);
        log.info("Selected sessionId: " + session.getSessionKey());
      } else {
        return new UiResponse(
            "",
            "Selendroid inspector can only be used if there is an active test session running. "
                + "To start a test session, add a break point into your test code and run the test in debug mode.");
      }
    } else {
      session = getSelendroidDriver().getActiveSession(sessionId);
    }
    byte[] screenshot = null;
    try {
      screenshot = getSelendroidDriver().takeScreenshot(sessionId);
    } catch (AndroidDeviceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return new UiResponse(sessionId != null ? sessionId : "", screenshot);
  }
}
