/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.server.handler;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.openqa.selendroid.server.exceptions.NoSuchElementException;
import org.openqa.selendroid.server.exceptions.UnsupportedOperationException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.server.model.By;
import org.openqa.selendroid.server.model.internal.NativeAndroidBySelector;
import org.openqa.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

public class FindElement extends RequestHandler {

  public FindElement(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    JSONObject payload = getPayload();
    String method = payload.getString("using");
    String selector = payload.getString("value");
    SelendroidLogger.log(String.format("find element command using '%s' with selector '%s'.",
        method, selector));

    By by = new NativeAndroidBySelector().pickFrom(method, selector);
    AndroidElement element = null;
    try {
      element = getSelendroidDriver().findElement(by);
    } catch (NoSuchElementException e) {
      return new Response(getSessionId(), 7, e);
    } catch (UnsupportedOperationException e) {
      return new Response(getSessionId(), 32, e);
    }
    JSONObject result = new JSONObject();

    String id = getIdOfKnownElement(element);

    if (id == null) {
      return new Response(getSessionId(), 7, new NoSuchElementException("Element was not found."));
    }
    result.put("ELEMENT", id);

    return new Response(getSessionId(), 0, result);
  }
}
