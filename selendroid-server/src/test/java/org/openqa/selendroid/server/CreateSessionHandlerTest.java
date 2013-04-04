package org.openqa.selendroid.server;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONObject;
import org.junit.Test;
import org.openqa.selendroid.server.internal.Capabilities;
import org.openqa.selendroid.server.internal.SelendroidAssert;

public class CreateSessionHandlerTest extends BaseTest {

  @Test
  public void assertNewTestSessionCreationIsReturningNewURI() throws Exception {
    HttpResponse response = executeCreateSessionRequest();
    SelendroidAssert.assertResponseIsRedirect(response);
    JSONObject json = parseJsonResponse(response);
    Assert.assertEquals("http://localhost:8055/wd/hub/session/" + json.getString("sessionId"),
        response.getFirstHeader("location").getValue());
  }

  @Test
  public void assertNewTestSessionCreationAndGetCapabilties() throws Exception {
    HttpResponse sessionResponse = executeCreateSessionRequest();
    JSONObject json = parseJsonResponse(sessionResponse);
    String sessionId = json.getString("sessionId");

    // Get capabilities of session
    HttpResponse getCapaResp =
        executeRequest("http://localhost:" + port + "/wd/hub/session/" + sessionId, HttpMethod.GET);
    SelendroidAssert.assertResponseIsOk(getCapaResp);
    JSONObject capa = parseJsonResponse(getCapaResp);
    Assert.assertEquals(sessionId, capa.getString("sessionId"));
    Assert.assertEquals(0, capa.getInt("status"));
    
    Assert.assertEquals("selendroid", capa.getJSONObject("value").getString(Capabilities.NAME));
  }
}
