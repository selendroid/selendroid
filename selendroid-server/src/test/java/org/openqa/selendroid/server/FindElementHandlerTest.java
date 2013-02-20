package org.openqa.selendroid.server;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.openqa.selendroid.server.internal.SelendroidAssert;

import com.google.gson.JsonObject;
/**
 * TODO rethink find element tests without having an emulator running.
 * @author ddary
 *
 */
public class FindElementHandlerTest extends BaseTest {
  //@Test() 
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
    HttpResponse element = executeRequestWithPayload(url, HttpMethod.POST, payload.toString());
    SelendroidAssert.assertResponseIsOk(element);
  }
}
