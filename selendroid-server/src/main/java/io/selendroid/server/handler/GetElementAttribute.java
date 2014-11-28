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

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.exceptions.NoSuchElementAttributeException;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.server.util.SelendroidLogger;

import org.json.JSONException;
import org.json.JSONObject;

public class GetElementAttribute extends SafeRequestHandler {

  public GetElementAttribute(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    SelendroidLogger.info("get attribute of element command");
    String id = getElementId(request);
    String attributeName = getNameAttribute(request);
    AndroidElement element = getElementFromCache(request, id);
    Object text = JSONObject.NULL;
    try {
      text = element.getAttribute(attributeName);
    } catch (NoSuchElementAttributeException e) {
      // attribute not found
    }
    return new SelendroidResponse(getSessionId(request), text);
  }
}
