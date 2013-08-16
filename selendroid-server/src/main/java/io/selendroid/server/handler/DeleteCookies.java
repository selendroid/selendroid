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
import io.selendroid.util.SelendroidLogger;
import io.selendroid.server.SelendroidResponse;
import org.json.JSONException;
import org.webbitserver.HttpRequest;

public class DeleteCookies extends RequestHandler {

    public DeleteCookies(HttpRequest request, String mappedUri) {
        super(request, mappedUri);

    }

    @Override
    public Response handle() throws JSONException {
        SelendroidLogger.log("delete session cookies command");
        String url = getSelendroidDriver().getCurrentUrl();
        getSelendroidDriver().deleteCookie(url);
        return new SelendroidResponse(getSessionId(), "");
    }
}
