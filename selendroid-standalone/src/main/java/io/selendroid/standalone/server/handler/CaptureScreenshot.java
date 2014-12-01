/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.standalone.server.handler;

import io.selendroid.standalone.server.BaseSelendroidStandaloneHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.internal.Base64Encoder;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.StatusCode;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.standalone.exceptions.AndroidDeviceException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CaptureScreenshot extends BaseSelendroidStandaloneHandler {
  private static final Logger log = Logger.getLogger(CaptureScreenshot.class.getName());

  public CaptureScreenshot(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handleRequest(HttpRequest request, JSONObject payload) throws JSONException {
    byte[] rawPng;
    try {
      rawPng = getSelendroidDriver(request).takeScreenshot(getSessionId(request));
    } catch (AndroidDeviceException e) {
      log.log(Level.SEVERE, "Cannot take screenshot", e);
      return new SelendroidResponse(getSessionId(request), StatusCode.UNKNOWN_ERROR, e);
    }
    String base64Png = new Base64Encoder().encode(rawPng);

    return new SelendroidResponse(getSessionId(request), base64Png);
  }
}
