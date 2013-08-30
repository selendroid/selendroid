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

import org.json.JSONException;
import org.json.JSONObject;
import io.selendroid.exceptions.NoSuchElementException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.StaleElementReferenceException;
import io.selendroid.exceptions.UnsupportedOperationException;
import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.server.model.By;
import io.selendroid.server.model.internal.NativeAndroidBySelector;
import io.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

public class FindChildElement extends RequestHandler {

  public FindChildElement(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException{
    JSONObject payload = getPayload();
    String method = payload.getString("using");
    String selector = payload.getString("value");
    SelendroidLogger.log(String.format("find child element command using '%s' with selector '%s'.",
        method, selector));

    String elementId = getElementId();
    AndroidElement root = getElementFromCache(elementId);
    if (root == null) {
      return new SelendroidResponse(getSessionId(), 10, new SelendroidException("The element with Id: "
          + elementId + " was not found."));
    }
    By by = new NativeAndroidBySelector().pickFrom(method, selector);
    AndroidElement element = null;
    try {
      element = root.findElement(by);
    } catch (StaleElementReferenceException se) {
      return new SelendroidResponse(getSessionId(), 10, se);
    } catch (NoSuchElementException e) {
      return new SelendroidResponse(getSessionId(), 7, e);
    } catch (UnsupportedOperationException e) {
      return new SelendroidResponse(getSessionId(), 32, e);
    }
    JSONObject result = new JSONObject();

    String id = getIdOfKnownElement(element);
    if (id == null) {
      return new SelendroidResponse(getSessionId(), 7, new NoSuchElementException("Element was not found."));
    }
    result.put("ELEMENT", id);

    return new SelendroidResponse(getSessionId(), 0, result);
  }
}
