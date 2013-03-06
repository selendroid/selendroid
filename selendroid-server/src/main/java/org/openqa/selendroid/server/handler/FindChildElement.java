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

import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.openqa.selendroid.server.exceptions.NoSuchElementException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.server.model.By;
import org.openqa.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

import com.google.gson.JsonObject;

public class FindChildElement extends RequestHandler {

  public FindChildElement(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() {
    JsonObject payload = getPayload();
    String method = payload.get("using").getAsString();
    String selector = payload.get("value").getAsString();
    SelendroidLogger.log(String.format("find element command using '%s' with selector '%s'.",
        method, selector));
    AndroidElement root = getElementFromCache(getElementId());

    By by = new NativeAndroidBySelector().pickFrom(method, selector, getCurrentWindowType());
    AndroidElement element = null;
    try {
      element = root.findElement(by);
    } catch (NoSuchElementException e) {
      return new Response(getSessionId(), 7, e);
    } catch (UnsupportedOperationException e) {
      return new Response(getSessionId(), 13, e);
    }
    JsonObject result = new JsonObject();

    Long id = getIdOfKnownElement(element);
    if (id == null) {
      return new Response(getSessionId(), 7, new NoSuchElementException("Element was not found."));
    }
    result.addProperty("ELEMENT", id);

    return new Response(getSessionId(), 0, result);
  }
}
