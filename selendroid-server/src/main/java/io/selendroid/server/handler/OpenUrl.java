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

import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;

import org.json.JSONException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

public class OpenUrl extends RequestHandler {

  public OpenUrl(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handle(HttpRequest request) throws JSONException {
    SelendroidLogger.info("Open URL command");
    String url = getPayload(request).getString("url");
    if (url == null || url.isEmpty()) {
      return new SelendroidResponse(getSessionId(request), 13, new SelendroidException(
          "Not able to open Url because Url is missing."));
    }
    getSelendroidDriver(request).get(url);
    return new SelendroidResponse(getSessionId(request), "");
  }
}
