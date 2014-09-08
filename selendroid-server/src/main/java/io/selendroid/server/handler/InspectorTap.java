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
package io.selendroid.server.handler;

import io.selendroid.android.internal.Point;
import io.selendroid.server.SafeRequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.server.model.interactions.AndroidCoordinates;
import io.selendroid.util.SelendroidLogger;

import org.json.JSONException;
import org.json.JSONObject;
import io.selendroid.server.http.HttpRequest;

public class InspectorTap extends SafeRequestHandler {

  public InspectorTap(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    SelendroidLogger.info("Inspector click on position command");

    JSONObject payload = getPayload(request);
    int x = payload.getInt("x");
    int y = payload.getInt("y");

    SelendroidDriver driver = getSelendroidDriver(request);
    driver.getTouch().singleTap(new AndroidCoordinates(null, new Point(x, y)));

    return new SelendroidResponse(getSessionId(request), "");
  }
}
