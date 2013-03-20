package org.openqa.selendroid.server;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import static org.mockito.Mockito.*;
import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.handlers.SessionAndIdExtractionTestHandler;
import org.openqa.selendroid.server.handlers.SessionAndPayloadExtractionTestHandler;
import org.openqa.selendroid.server.internal.Capabilities;

public class BaseTest {
  public static final int port = 8055;
  protected AndroidServer server;
  public static final String ANY_STRING = "ANY-STRING";

  @Before
  public void setup() {
    ServerInstrumentation instrumentation = mock(ServerInstrumentation.class);
    when(instrumentation.getSelendroidVersionNumber()).thenReturn("0.2");
    server = new AndroidServer(port, instrumentation);
    server.start();
  }

  public Capabilities getCapabilities() {
    return Capabilities.android(ANY_STRING, ANY_STRING, ANY_STRING);
  }

  public HttpClient getHttpClient() {
    return new DefaultHttpClient();
  }

  public HttpResponse executeRequestWithPayload(String url, HttpMethod method, String payload)
      throws Exception {
    BasicHttpEntityEnclosingRequest request =
        new BasicHttpEntityEnclosingRequest(method.getName(), url);
    request.setEntity(new StringEntity(payload, "UTF-8"));

    return getHttpClient().execute(new HttpHost("localhost", port), request);
  }

  public JSONObject parseJsonResponse(HttpResponse response) throws Exception {
    return new JSONObject(IOUtils.toString(response.getEntity().getContent()));
  }

  public HttpResponse executeRequest(String url, HttpMethod method) throws Exception {
    HttpRequestBase request = null;
    if (HttpMethod.GET.equals(method)) {
      request = new HttpGet(url);
    } else if (HttpMethod.POST.equals(method)) {
      request = new HttpPost(url);
    } else if (HttpMethod.DELETE.equals(method)) {
      request = new HttpDelete(url);
    } else {
      throw new RuntimeException("Provided HttpMethod not supported");
    }
    return getHttpClient().execute(request);
  }

  @After
  public void tearDown() {
    if (server != null) {
      server.stop();
    }
  }

  /** Configuring AndroidServlet to use special test handler. */
  public class AndroidTestServlet extends AndroidServlet {
    public AndroidTestServlet() {
      super(null);
    }

    @Override
    protected void init() {
      getHandler.put("/wd/hub/session/:sessionId/element",
          SessionAndPayloadExtractionTestHandler.class);
      postHandler.put("/wd/hub/session/:sessionId/element/:id/click",
          SessionAndIdExtractionTestHandler.class);
    }
  }

  protected HttpResponse executeCreateSessionRequest() throws Exception {
    String url = "http://localhost:" + port + "/wd/hub/session";
    HttpResponse response =
        executeRequestWithPayload(url, HttpMethod.POST, getCapabilityPayload().toString());
    return response;
  }

  protected JSONObject getCapabilityPayload() {
    Capabilities desiredCapabilities = getCapabilities();
    JSONObject payload = new JSONObject();

    try {
      payload.put("desiredCapabilities", new JSONObject(desiredCapabilities.asMap()));
    } catch (JSONException e) {
      throw new SelendroidException(e);
    }
    return payload;
  }
}
