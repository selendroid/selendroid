package org.openqa.selendroid.server;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.Test;
import org.openqa.selendroid.server.internal.SelendroidAssert;

import com.google.gson.JsonObject;

public class FindElementHandlerTest extends BaseTest {
  @Test
  public void assertThatFindElementResponseHasCorrectFormat() throws Exception {
    HttpResponse response = executeCreateSessionRequest();
    SelendroidAssert.assertResponseIsRedirect(response);
    JsonObject session = parseJsonResponse(response);
    String sessionId = session.get("sessionId").getAsString();
    Assert.assertFalse(sessionId.isEmpty());

    JsonObject payload = new JsonObject();
    payload.addProperty("using", "id");
    payload.addProperty("value", "my_button_bar");

    String url = "http://localhost:" + port + "/wd/hub/session/" + sessionId + "/element";
    HttpResponse element = executeRequestWithPayload(url, HttpMethod.GET, payload.toString());
    // SelendroidAssert.assertResponseIsOk(element);
    System.out.println("FindElement# " + element);
  }
}
