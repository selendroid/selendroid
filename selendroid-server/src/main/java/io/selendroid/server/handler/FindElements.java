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

import io.selendroid.exceptions.NoSuchElementException;
import io.selendroid.exceptions.UnsupportedOperationException;
import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.server.model.By;
import io.selendroid.server.model.internal.NativeAndroidBySelector;
import io.selendroid.util.SelendroidLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

import java.util.List;

public class FindElements extends RequestHandler {

  public FindElements(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handle(HttpRequest request) throws JSONException {
    JSONObject payload = getPayload(request);
    String method = payload.getString("using");
    String selector = payload.getString("value");
    SelendroidLogger.info(String.format("find elements command using %s with selector %s.", method,
        selector));

    By by = new NativeAndroidBySelector().pickFrom(method, selector);
    List<AndroidElement> elements = null;
    try {
      elements = getSelendroidDriver(request).findElements(by);
    } catch (NoSuchElementException e) {
      return new SelendroidResponse(getSessionId(request), new JSONArray());
    } catch (UnsupportedOperationException e) {
      return new SelendroidResponse(getSessionId(request), 32, e);
    }
    JSONArray result = new JSONArray();
    for (AndroidElement element : elements) {
      JSONObject jsonElement = new JSONObject();
      String id = getIdOfKnownElement(request, element);
      if (id == null) {
        continue;
      }
      jsonElement.put("ELEMENT", id);
      result.put(jsonElement);
    }
    return new SelendroidResponse(getSessionId(request), result);
  }
}
