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
import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.openqa.selendroid.server.exceptions.NoSuchElementAttributeException;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.webbitserver.HttpRequest;

public class GetElementAttribute extends RequestHandler {

  public GetElementAttribute(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    System.out.println("get text command");
    Long id = getElementId();
    String attributeName = getNameAttribute();
    AndroidElement element = getElementFromCache(id);
    if (element == null) {
      return new Response(getSessionId(), 7, new SelendroidException("Element with id '" + id
          + "' was not found."));
    }
    String text = null;
    try {
      text = element.getAttribute(attributeName);
    } catch (NoSuchElementAttributeException e) {
      // attribute not found
    }
    return new Response(getSessionId(), text);
  }
}
