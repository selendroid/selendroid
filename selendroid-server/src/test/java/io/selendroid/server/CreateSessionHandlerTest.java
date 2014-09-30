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

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import io.netty.handler.codec.http.HttpMethod;
import io.selendroid.server.internal.Capabilities;
import io.selendroid.server.internal.SelendroidAssert;


public class CreateSessionHandlerTest extends BaseTest {
  @Test
  public void assertNewTestSessionCreationAndGetCapabilties() throws Exception {
    HttpResponse sessionResponse = executeCreateSessionRequest();
    JSONObject json = parseJsonResponse(sessionResponse);
    String sessionId = json.getString("sessionId");

    // Get capabilities of session
    HttpResponse getCapaResp =
        executeRequest("http://" + host + ":" + port + "/wd/hub/session/" + sessionId,
            HttpMethod.GET);
    SelendroidAssert.assertResponseIsOk(getCapaResp);
    JSONObject capa = parseJsonResponse(getCapaResp);
    Assert.assertEquals(sessionId, capa.getString("sessionId"));
    Assert.assertEquals(0, capa.getInt("status"));

    Assert.assertEquals("selendroid", capa.getJSONObject("value").getString(Capabilities.NAME));
  }
}
