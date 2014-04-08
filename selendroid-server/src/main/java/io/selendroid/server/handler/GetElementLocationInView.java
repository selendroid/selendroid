/*
 * Copyright 2014 eBay Software Foundation and selendroid committers.
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
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.StaleElementReferenceException;
import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.util.SelendroidLogger;

import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

public class GetElementLocationInView extends RequestHandler {

  public GetElementLocationInView(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handle(HttpRequest request) throws JSONException {
    SelendroidLogger.info("get element location in view");
    String id = getElementId(request);

    AndroidElement element = getElementFromCache(request, id);
    if (element == null) {
      return new SelendroidResponse(getSessionId(request), 10, new SelendroidException(
          "Element with id '" + id + "' was not found."));
    }
    Point location = element.getLocation();
    JSONObject result = new JSONObject();
    result.put("x", location.x);
    result.put("y", location.y);

    try {
      return new SelendroidResponse(getSessionId(request), result);
    } catch (StaleElementReferenceException se) {
      return new SelendroidResponse(getSessionId(request), 10, se);
    }
  }
}
