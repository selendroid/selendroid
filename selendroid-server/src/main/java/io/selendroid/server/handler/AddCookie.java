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
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.Cookie;
import io.selendroid.util.SelendroidLogger;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

public class AddCookie extends RequestHandler {

  public AddCookie(HttpRequest request, String mappedUri) {
    super(request, mappedUri);

  }

  @Override
  public Response handle() throws JSONException {

    Date expiry = null;
    SelendroidLogger.log("set cookie to a session command");
    String url = getSelendroidDriver().getCurrentUrl();
    JSONObject cookie = getPayload().getJSONObject("cookie");
    String name = cookie.get("name").toString();
    String value = cookie.get("value").toString();
    if (cookie.has("expiry")) {
      expiry = new Date(new Long(cookie.get("expiry").toString()).longValue());
    }

    Cookie cookie1 = new Cookie(name, value, "/", expiry);
    getSelendroidDriver().addCookie(url, cookie1);
    return new SelendroidResponse(getSessionId(), "");
  }
}