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
import org.openqa.selendroid.android.WindowType;
import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.SelendroidDriver;
import org.openqa.selendroid.util.SelendroidLogger;
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
      return new Response(getSessionId(), 13, new SelendroidException("Window name is missing."));
    }
    SelendroidDriver driver = getSelendroidDriver();
    if (WindowType.NATIVE_APP.name().equals(windowName)) {
      driver.switchDriverMode(WindowType.NATIVE_APP);
    } else if (WindowType.WEBVIEW.name().equals(windowName)) {
      driver.switchDriverMode(WindowType.WEBVIEW);
    } else {
      return new Response(getSessionId(), 23, new SelendroidException(
          "Invalid window handle was used: only 'NATIVE_APP' and 'WEBVIEW' are supported."));
    }
    return new Response(getSessionId(), "");
  }
}
