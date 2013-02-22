package org.openqa.selendroid.server;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.Test;
import org.openqa.selendroid.server.internal.SelendroidAssert;
import org.openqa.selendroid.server.model.Capabilities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CreateSessionHandlerTest extends BaseTest {

  @Test
  public void assertNewTestSessionCreationIsReturningNewURI() throws Exception {
    HttpResponse response = executeCreateSessionRequest();
    SelendroidAssert.assertResponseIsRedirect(response);
    JsonObject json = parseJsonResponse(response);
    Assert.assertEquals("/wd/hub/session/" + json.get("sessionId").getAsString(), response
        .getFirstHeader("location").getValue());
  }

  @Test
  public void assertNewTestSessionCreationAndGetCapabilties() throws Exception {
    HttpResponse sessionResponse = executeCreateSessionRequest();
    JsonObject json = parseJsonResponse(sessionResponse);
    String sessionId = json.get("sessionId").getAsString();

    // Get capabilities of session
    HttpResponse getCapaResp =
        executeRequest("http://localhost:" + port + "/wd/hub/session/" + sessionId, HttpMethod.GET);
    SelendroidAssert.assertResponseIsOk(getCapaResp);
    JsonObject capa = parseJsonResponse(getCapaResp);
    Assert.assertEquals(sessionId, capa.get("sessionId").getAsString());
    Assert.assertEquals(0, capa.get("status").getAsInt());
    Capabilities getCapa = Capabilities.fromJSON(capa.get("value").getAsJsonObject());
    Assert.assertEquals("selendroid", getCapa.asMap().get(Capabilities.NAME));
  }
}
