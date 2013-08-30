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
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.ActiveSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.openqa.selenium.logging.LogEntry;
import org.webbitserver.HttpRequest;

public class GetLogs extends BaseSelendroidServerHandler {

  public GetLogs(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    // TODO probably should look at the payload for what type of logs ('driver')
    // but really we only support getting the adb logcat
    ActiveSession session = getSelendroidDriver().getActiveSession(getSessionId());
    JSONArray logs = new JSONArray();
    for (LogEntry l : session.getDevice().getLogs()) {
      logs.put(l.toJson());
    }
    return new SelendroidResponse(getSessionId(), logs);
  }
}
