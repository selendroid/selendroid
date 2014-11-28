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
package io.selendroid.server;

import io.netty.handler.codec.http.HttpMethod;
import io.selendroid.server.common.http.HttpServer;
import io.selendroid.server.internal.SelendroidAssert;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class HandlerRegisteredTest extends BaseTest {
  private HttpServer server = null;

  @Override
  public void setup() throws Exception {
    server = new HttpServer(port);
    server.addHandler(new AndroidTestServlet());
    server.start();
  }

  @Test
  public void getFindElementHandlerRegistered() throws Exception {
    JSONObject payload = new JSONObject();
    payload.put("using", "id");
    payload.put("value", "my_button_bar");

    String url = "http://" + host + ":" + port + "/wd/hub/session/1234567890/element";
    HttpResponse response = executeRequestWithPayload(url, HttpMethod.GET, payload.toString());
    SelendroidAssert.assertResponseIsOk(response);

    JSONObject responseJSON = parseJsonResponse(response);
    assertEquals("0", responseJSON.getString("status"));
    assertEquals("sessionId#1234567890 using#id value#my_button_bar",
            responseJSON.getString("value"));
  }

  @Test
  public void postClickHandlerRegistered() throws Exception {
    String url = "http://" + host + ":" + port + "/wd/hub/session/12345/element/815/click";
    HttpResponse response = executeRequest(url, HttpMethod.POST);
    SelendroidAssert.assertResponseIsOk(response);
    JSONObject responseJSON = parseJsonResponse(response);
    assertEquals("0", responseJSON.getString("status"));
    assertEquals("sessionId#12345 elementId#815", responseJSON.getString("value"));
  }

  @Test
  public void postStatusHandlerNotRegistered() throws Exception {
    String url = "http://" + host + ":" + port + "/wd/hub/session/1234567890/element";
    HttpResponse response = executeRequest(url, HttpMethod.POST);
    SelendroidAssert.assertResponseIsResourceNotFound(response);
  }

  @Override
  public void tearDown() {
    server.stop();
  }
}
