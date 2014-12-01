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
package io.selendroid.standalone.server;

import io.selendroid.server.common.BaseRequestHandler;
import io.selendroid.server.common.BaseServlet;
import io.selendroid.server.common.Response;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.standalone.server.model.ActiveSession;
import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseSelendroidStandaloneHandler extends BaseRequestHandler {

  private final Logger log = Logger.getLogger(this.getClass().getName());

  public BaseSelendroidStandaloneHandler(String mappedUri) {
    super(mappedUri);
  }

  /**
   * Implement this in subclasses to handle the request. Don't override {@link #handle(HttpRequest)} directly.
   */
  protected abstract Response handleRequest(HttpRequest request, JSONObject payload) throws JSONException;

  @Override
  public Response handle(HttpRequest request) throws JSONException {
    JSONObject payload = getPayload(request);
    logHandlerCalled(payload);
    return handleRequest(request, payload);
  }

  protected SelendroidStandaloneDriver getSelendroidDriver(HttpRequest request) {
    return (SelendroidStandaloneDriver) request.data().get(BaseServlet.DRIVER_KEY);
  }

  protected ActiveSession getActiveSession(HttpRequest request) {
    SelendroidStandaloneDriver driver = getSelendroidDriver(request);
    if (driver == null) {
      log.warning("Cannot get session, no selendroid driver.");
      return null;
    }
    return driver.getActiveSession(getSessionId(request));
  }

  private void logHandlerCalled(JSONObject payload) {
    String message = "Selendroid standalone handler: " + this.getClass().getSimpleName();
    if (payload != null) {
      try {
        message += ", payload:\n" + payload.toString(2);
      } catch (JSONException e) {
        log.log(Level.WARNING, "Cannot debug-print request payload", e);
      }
    }
    log.log(Level.FINE, message);
  }
}
