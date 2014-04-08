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

import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.server.model.Session;
import io.selendroid.util.SelendroidLogger;

import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

public class SendKeys extends RequestHandler {

  public SendKeys(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handle(HttpRequest request) throws JSONException {
    SelendroidLogger.info("send keys command");
    String id = getElementId(request);

    AndroidElement element = getElementFromCache(request, id);
    if (element == null) {
      return new SelendroidResponse(getSessionId(request), 10, new SelendroidException(
          "Element with id '" + id + "' was not found."));
    }
    String[] keysToSend;
    try {
      keysToSend = extractKeysToSendFromPayload(request);
    } catch (SelendroidException e) {
      return new SelendroidResponse(getSessionId(request), 13, e);
    }

    if (isNativeEvents(request)) {
      element.enterText(keysToSend);
    }else{
      element.setText(keysToSend);
    }
    return new SelendroidResponse(getSessionId(request), "");
  }

  boolean isNativeEvents(HttpRequest request) {
    JSONObject config =
        getSelendroidDriver(request).getSession().getCommandConfiguration(
            Session.SEND_KEYS_TO_ELEMENT);
    if (config != null && config.has(Session.NATIVE_EVENTS_PROPERTY)) {
      try {
        return config.getBoolean(Session.NATIVE_EVENTS_PROPERTY);
      } catch (JSONException e) {}
    }
    // default is native events
    return true;
  }

}
