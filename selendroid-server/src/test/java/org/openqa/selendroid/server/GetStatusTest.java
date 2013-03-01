package org.openqa.selendroid.server;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.Test;
import org.openqa.selendroid.server.internal.SelendroidAssert;

import com.google.gson.JsonObject;

public class GetStatusTest extends BaseTest {
  @Test
  public void assertThatGetStatusHandlerIsRegistered() throws Exception {
    String url = "http://localhost:" + server.getPort() + "/wd/hub/status";
    HttpResponse response = executeRequest(url, HttpMethod.GET);
    SelendroidAssert.assertResponseIsOk(response);
    JsonObject result = parseJsonResponse(response);
    SelendroidAssert.assertResponseIsOk(response);

    Assert.assertFalse(result.has("sessionId"));
    JsonObject value = result.get("value").getAsJsonObject();
    Assert.assertEquals("0.2", value.get("build").getAsJsonObject().get("version")
        .getAsString());
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
