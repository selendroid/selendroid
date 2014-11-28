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
package io.selendroid.standalone.util;

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.junit.Assert;

public class SelendroidAssert {
  public static final String SESSION_ID_KEY = "sessionId";

  public static void assertResponseIsServerError(HttpResponse response) {
    assertResponseStatusCode(response, 500);
  }
  public static void assertResponseIsRedirect(HttpResponse response) {
    assertResponseStatusCode(response, 301);
  }

  public static void assertResponseStatusCode(HttpResponse response, int statusCode) {
    Assert.assertEquals(statusCode, response.getStatusLine().getStatusCode());
  }

  public static void assertResponseIsOk(HttpResponse response) {
    assertResponseStatusCode(response, 200);
  }

  public static void assertResponseValueHasSessionId(JSONObject responseValue) {
    Assert.assertTrue(responseValue.has("SESSION_ID_KEY"));
  }

  public static void assertResponseValueHasNoSessionId(JSONObject responseValue) {
    Assert.assertFalse(responseValue.has("SESSION_ID_KEY"));
  }
}
