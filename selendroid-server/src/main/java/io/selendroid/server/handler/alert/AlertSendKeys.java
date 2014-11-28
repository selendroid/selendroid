/*
 * Copyright 2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.server.handler.alert;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.StatusCode;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.handler.SafeRequestHandler;

import org.json.JSONException;

public class AlertSendKeys extends SafeRequestHandler {

  public AlertSendKeys(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    if (!getSelendroidDriver(request).isAlertPresent()) {
      return new SelendroidResponse(getSessionId(request), StatusCode.NO_ALERT_OPEN_ERROR, "no alert open");
    }
    String keysToSend = keysToSend = getPayload(request).getString("text");
    getSelendroidDriver(request).setAlertText(keysToSend);
    return new SelendroidResponse(getSessionId(request), null);
  }

  @Override
  public boolean commandAllowedWithAlertPresentInWebViewMode() {
    return true;
  }
}
