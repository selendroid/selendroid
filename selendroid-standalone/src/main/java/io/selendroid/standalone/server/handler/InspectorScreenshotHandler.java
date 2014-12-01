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
import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;
import org.json.JSONException;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.UiResponse;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.server.model.ActiveSession;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InspectorScreenshotHandler extends BaseSelendroidStandaloneHandler {
  private static final Logger log = Logger.getLogger(InspectorScreenshotHandler.class.getName());

  public InspectorScreenshotHandler(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handleRequest(HttpRequest request, JSONObject payload) throws JSONException {
    String sessionId = getSessionId(request);
    log.info("inspector screenshot handler, sessionId: " + sessionId);

    if (sessionId == null || sessionId.isEmpty()) {
      ActiveSession firstActiveSession = getFirstActiveSession(getSelendroidDriver(request));
      if (firstActiveSession != null) {
        sessionId = firstActiveSession.getSessionId();
        log.info("Selected first session, id: " + firstActiveSession.getSessionId());
      } else {
        return new UiResponse(
            "",
            "Selendroid inspector can only be used if there is an active test session running. " +
             "To start a test session, add a break point into your test code and run the test in debug mode.");
      }
    }
    byte[] screenshot = null;
    try {
      screenshot = getSelendroidDriver(request).takeScreenshot(sessionId);
    } catch (AndroidDeviceException e) {
      log.log(Level.SEVERE, "Cannot take screenshot for inspector", e);
    }
    return new UiResponse(sessionId != null ? sessionId : "", screenshot);
  }

  private ActiveSession getFirstActiveSession(SelendroidStandaloneDriver driver) {
    if (driver.getActiveSessions() != null && driver.getActiveSessions().size() >= 1) {
      return driver.getActiveSessions().get(0);
    }
    return null;
  }
}
