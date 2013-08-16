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

package io.selendroid.server.handler;

import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.util.SelendroidLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

import java.util.Set;
import io.selendroid.server.model.Cookie;

public class GetCookies extends RequestHandler {

    public GetCookies(HttpRequest request, String mappedUri) {
        super(request, mappedUri);

    }

    @Override
    public Response handle() throws JSONException {

        SelendroidLogger.log("get cookies of a session command");
        String url = getSelendroidDriver().getCurrentUrl();
        Set<Cookie> cookies = getSelendroidDriver().getCookies(url);
        JSONArray jsonArray = new JSONArray();
        for (Cookie cookie : cookies)
        {
            JSONObject json = new JSONObject();
            json.put("name", cookie.getName());
            json.put("value", cookie.getValue());
            json.put("path", cookie.getPath());
            json.put("domain", cookie.getDomain());
            json.put("expiry", cookie.getExpiry());
            json.put("secure", cookie.isSecure());
            jsonArray.put(json);
        }
        return new SelendroidResponse(getSessionId(), jsonArray);
    }

}