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
package io.selendroid.server;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONObject;
import org.junit.Test;
import io.selendroid.server.internal.SelendroidAssert;

public class GetStatusTest extends BaseTest {
  @Test
  public void assertThatGetStatusHandlerIsRegistered() throws Exception {
    String url = "http://"+host+":" + server.getPort() + "/wd/hub/status";
    HttpResponse response = executeRequest(url, HttpMethod.GET);
    SelendroidAssert.assertResponseIsOk(response);
    JSONObject result = parseJsonResponse(response);
    SelendroidAssert.assertResponseIsOk(response);

    Assert.assertFalse(result.has("sessionId"));
    JSONObject value = result.getJSONObject("value");
    Assert.assertEquals("0.2", value.getJSONObject("build").getString("version")
               );
  }

  @Test
  public void assertThatGetStatusHanlerIsNotRegisteredForPost() throws Exception {
    String url = "http://"+host+":" + server.getPort() + "/wd/hub/status";
    HttpResponse response = executeRequest(url, HttpMethod.POST);
    SelendroidAssert.assertResponseIsServerError(response);
  }

  @Test
  public void assertThatGetStatusHanlerIsNotRegisteredForDelete() throws Exception {
    String url = "http://"+host+":" + server.getPort() + "/wd/hub/status";
    HttpResponse response = executeRequest(url, HttpMethod.DELETE);
    SelendroidAssert.assertResponseIsServerError(response);
  }
}
