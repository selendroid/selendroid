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
import org.openqa.selendroid.server.exceptions.ElementNotVisibleException;
import org.openqa.selendroid.server.exceptions.NoSuchElementException;
import org.openqa.selendroid.server.exceptions.StaleElementReferenceException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

public class ClickElement extends RequestHandler {

  public ClickElement(HttpRequest request,String mappedUri) {
    super(request,mappedUri);
  }

  @Override
  public Response handle() throws JSONException{
    SelendroidLogger.log("Click element command");
    String id = getElementId();
    AndroidElement element = getElementFromCache(id);
    if (element == null) {
      return new Response(getSessionId(), 10, new NoSuchElementException("The element with id '"
          + id + "' was not found."));
    }
    try {
      element.click();
    } catch (ElementNotVisibleException ev) {
      return new Response(getSessionId(), 11, ev);
    } catch (StaleElementReferenceException se) {
      return new Response(getSessionId(), 10, se);
    } catch (Exception e) {
      SelendroidLogger.log("error while clicking the element: ", e);
      return new Response(getSessionId(), 13, e);
    }
    return new Response(getSessionId(), "");
  }
}
