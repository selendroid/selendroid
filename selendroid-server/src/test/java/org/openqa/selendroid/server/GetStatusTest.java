package org.openqa.selendroid.server;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONObject;
import org.junit.Test;
import org.openqa.selendroid.server.internal.SelendroidAssert;

public class GetStatusTest extends BaseTest {
  @Test
  public void assertThatGetStatusHandlerIsRegistered() throws Exception {
    String url = "http://localhost:" + server.getPort() + "/wd/hub/status";
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
    String url = "http://localhost:" + server.getPort() + "/wd/hub/status";
    HttpResponse response = executeRequest(url, HttpMethod.POST);
    SelendroidAssert.assertResponseIsServerError(response);
  }

  @Test
  public void assertThatGetStatusHanlerIsNotRegisteredForDelete() throws Exception {
    String url = "http://localhost:" + server.getPort() + "/wd/hub/status";
    HttpResponse response = executeRequest(url, HttpMethod.DELETE);
    SelendroidAssert.assertResponseIsServerError(response);
  }
}
