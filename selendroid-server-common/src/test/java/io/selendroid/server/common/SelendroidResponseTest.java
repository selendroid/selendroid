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
package io.selendroid.server.common;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SelendroidResponseTest {

  @Test
  public void testRenderKnownError() throws JSONException {
    SelendroidResponse response =
        new SelendroidResponse("my-session", StatusCode.INVALID_SELECTOR, new RuntimeException("Invalid selector"));
    JSONObject rendered = new JSONObject(response.render());

    assertEquals("my-session", rendered.getString("sessionId"));
    assertEquals(StatusCode.INVALID_SELECTOR.getCode(), rendered.getLong("status"));
    assertEquals("java.lang.RuntimeException", rendered.getJSONObject("value").getString("class"));
    assertEquals("Invalid selector", rendered.getJSONObject("value").getString("message"));
  }

  @Test
  public void testRenderUnknownError() throws JSONException {
    SelendroidResponse response =
        new SelendroidResponse("my-session", StatusCode.UNKNOWN_ERROR, new RuntimeException());
    JSONObject rendered = new JSONObject(response.render());

    assertEquals("my-session", rendered.getString("sessionId"));
    assertEquals(StatusCode.UNKNOWN_ERROR.getCode(), rendered.getLong("status"));
    assertEquals("java.lang.RuntimeException", rendered.getJSONObject("value").getString("class"));
    Assert.assertTrue(rendered.getJSONObject("value").getString("message").startsWith(
        String.format("java.lang.RuntimeException%n\tat io.selendroid.server.common.SelendroidResponseTest")));
  }

  @Test
  public void testRenderCatchAllError() throws JSONException {
    SelendroidResponse response = SelendroidResponse.forCatchAllError("my-session", new RuntimeException());
    JSONObject rendered = new JSONObject(response.render());

    assertEquals("my-session", rendered.getString("sessionId"));
    assertEquals(StatusCode.UNKNOWN_ERROR.getCode(), rendered.getLong("status"));
    assertEquals("java.lang.RuntimeException", rendered.getJSONObject("value").getString("class"));
    Assert.assertTrue(rendered.getJSONObject("value").getString("message").startsWith(
            String.format("CATCH_ALL: java.lang.RuntimeException%n\tat io.selendroid.server.common.SelendroidResponseTest")));
  }
}
