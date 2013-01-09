package org.openqa.selendroid.server.internal;

import junit.framework.Assert;

import org.apache.http.HttpResponse;

import com.google.gson.JsonObject;

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

  public static void assertResponseValueHasSessionId(JsonObject responseValue) {
    Assert.assertTrue(responseValue.has("SESSION_ID_KEY"));
  }

  public static void assertResponseValueHasNoSessionId(JsonObject responseValue) {
    Assert.assertFalse(responseValue.has("SESSION_ID_KEY"));
  }
}
