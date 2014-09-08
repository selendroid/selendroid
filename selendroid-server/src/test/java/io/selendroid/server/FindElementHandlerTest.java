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
import io.selendroid.server.internal.SelendroidAssert;

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.junit.Assert;

/**
 * TODO rethink find element tests without having an emulator running.
 * 
 * @author ddary
 * 
 */
public class FindElementHandlerTest extends BaseTest {
  // @Test()
  public void assertThatFindElementResponseHasCorrectFormat() throws Exception {
    HttpResponse response = executeCreateSessionRequest();
    SelendroidAssert.assertResponseIsRedirect(response);
    JSONObject session = parseJsonResponse(response);
    String sessionId = session.getString("sessionId");
    Assert.assertFalse(sessionId.isEmpty());

    JSONObject payload = new JSONObject();
    payload.put("using", "id");
    payload.put("value", "my_button_bar");

    String url = "http://" + host + ":" + port + "/wd/hub/session/" + sessionId + "/element";
    HttpResponse element = executeRequestWithPayload(url, HttpMethod.POST, payload.toString());
    SelendroidAssert.assertResponseIsOk(element);
  }
}
