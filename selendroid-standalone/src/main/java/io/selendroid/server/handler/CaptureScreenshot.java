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

import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.server.BaseSelendroidServerHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;

import java.util.logging.Logger;

import org.json.JSONException;
import org.openqa.selenium.internal.Base64Encoder;
import org.webbitserver.HttpRequest;

public class CaptureScreenshot extends BaseSelendroidServerHandler {
  private static final Logger log = Logger.getLogger(CaptureScreenshot.class.getName());

  public CaptureScreenshot(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    log.info("take screenshot command");
    byte[] rawPng;
    try {
      rawPng = getSelendroidDriver().takeScreenshot(getSessionId());
    } catch (AndroidDeviceException e) {
      e.printStackTrace();
      return new SelendroidResponse(getSessionId(), 33, e);
    }
    String base64Png = new Base64Encoder().encode(rawPng);

    return new SelendroidResponse(getSessionId(), base64Png);
  }
}
