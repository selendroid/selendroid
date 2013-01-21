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
import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

public class SubmitForm extends RequestHandler {

  public SubmitForm(HttpRequest request) {
    super(request);
  }

  @Override
  public Response handle() {
    SelendroidLogger.log("Submit element command");
    Long id = getElementId();
    AndroidElement element = getElementFromCache(id);
    String sessionId = getSessionId();
    try {
      element.submit();
    } catch (Exception e) {
      SelendroidLogger.logError("error while submitting the element: ", e);
      return new Response(sessionId, 33, e);
    }
    return new Response(sessionId, "");
  }

}
