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
import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.server.model.TouchScreen;
import org.openqa.selendroid.server.model.interactions.Coordinates;
import org.openqa.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

public class DoubleTapOnElement extends RequestHandler {

  public DoubleTapOnElement(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    SelendroidLogger.log("double tap on element gesture");
    String elementId = getElementId();
    TouchScreen touchScreen = getSelendroidDriver().getTouch();
    AndroidElement element = getElementFromCache(elementId);
    if (element == null) {
      return new Response(getSessionId(), 10, new SelendroidException("Element with id '"
          + elementId + "' was not found."));
    }
    Coordinates elementLocation = element.getCoordinates();

    touchScreen.doubleTap(elementLocation);
    return new Response(getSessionId(), "");
  }

}
