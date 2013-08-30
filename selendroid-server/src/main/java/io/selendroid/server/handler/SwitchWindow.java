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

import io.selendroid.android.WindowType;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.util.SelendroidLogger;

import org.json.JSONException;
import org.webbitserver.HttpRequest;

public class SwitchWindow extends RequestHandler {

  public SwitchWindow(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    SelendroidLogger.log("Switch Window command");
    String windowName = getPayload().getString("name");
    if (windowName == null || windowName.isEmpty()) {
      return new SelendroidResponse(getSessionId(), 13, new SelendroidException(
          "Window name is missing."));
    }
    SelendroidDriver driver = getSelendroidDriver();
    if (windowName.startsWith(WindowType.NATIVE_APP.name())
        || windowName.startsWith(WindowType.WEBVIEW.name())) {
      driver.switchDriverMode(windowName);
    } else {
      return new SelendroidResponse(getSessionId(), 23, new SelendroidException(
          "Invalid window handle was used: only 'NATIVE_APP' and 'WEBVIEW' are supported."));
    }
    return new SelendroidResponse(getSessionId(), "");
  }
}
