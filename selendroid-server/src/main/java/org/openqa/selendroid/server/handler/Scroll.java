/*
 * Copyright 2013 selendroid committers.
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
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.webbitserver.HttpRequest;

public class Scroll extends RequestHandler {

  public Scroll(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    String elementId = getElementId();
    JSONObject payload = getPayload();
    int xoffset = payload.getInt("xoffset");
    int yoffset = payload.getInt("yoffset");
    if (elementId == null) {
      getSelendroidDriver().getTouch().scroll(xoffset, yoffset);
    } else {
      AndroidElement element = getElementFromCache(elementId);
      if (element == null) {
        return new Response(getSessionId(), 10, new SelendroidException("Element with id '"
            + elementId + "' was not found."));
      }
      getSelendroidDriver().getTouch().scroll(element.getCoordinates(), xoffset, yoffset);
    }
    return new Response(getSessionId(), "");
  }

}
