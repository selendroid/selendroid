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

import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.webbitserver.HttpRequest;

import com.google.gson.JsonObject;

public class ScrollGesture extends RequestHandler {

  public ScrollGesture(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() {
    Long elementId = getElementId();
    JsonObject payload = getPayload();
    int xoffset = payload.get("xoffset").getAsInt();
    int yoffset = payload.get("yoffset").getAsInt();
    if (elementId == null) {
      getAndroidDriver().getTouch().scroll(xoffset, yoffset);
    } else {
      AndroidElement element = getElementFromCache(elementId);
      if (element == null) {
        return new Response(getSessionId(), 7, new SelendroidException("Element with id '"
            + elementId + "' was not found."));
      }
      getAndroidDriver().getTouch().scroll(element.getLocation(), xoffset, yoffset);
    }
    return new Response(getSessionId(), "");
  }

}
