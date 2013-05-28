/*
 * Copyright 2013 selendroid committers.
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
package io.selendroid.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.selendroid.SelendroidConfiguration;
import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.server.util.HttpClientUtil;
import io.selendroid.util.SelendroidAssert;

import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SelendroidSatusHandlerTest {
  protected static int port = 7777;
  private SelendroidServer server;

  @Before
  public void startServer() throws AndroidSdkException {
    SelendroidDriver driver = mock(SelendroidDriver.class);
    when(driver.getCpuArch()).thenReturn("x86");
    when(driver.getOsVersion()).thenReturn("osx");
    when(driver.getServerVersion()).thenReturn("dev");
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.setPort(getNextPort());
    server = new SelendroidServer(conf, driver);
    server.start();
  }

  public int getNextPort() {
    return port++;
  }

  @Test
  public void assertThatGetStatusHandlerIsRegistered() throws Exception {
    String url = "http://localhost:" + server.getPort() + "/wd/hub/status";
    HttpResponse response = HttpClientUtil.executeRequest(url, HttpMethod.GET);
    SelendroidAssert.assertResponseIsOk(response);
    JSONObject result = HttpClientUtil.parseJsonResponse(response);
    SelendroidAssert.assertResponseIsOk(response);

    Assert.assertFalse(result.has("sessionId"));
    JSONObject value = result.getJSONObject("value");
    Assert.assertEquals("dev", value.getJSONObject("build").getString("version"));
  }

  @Test
  public void assertThatGetStatusHanlerIsNotRegisteredForPost() throws Exception {
    String url = "http://localhost:" + server.getPort() + "/wd/hub/status";
    HttpResponse response = HttpClientUtil.executeRequest(url, HttpMethod.POST);
    SelendroidAssert.assertResponseIsServerError(response);
  }

  @Test
  public void assertThatGetStatusHanlerIsNotRegisteredForDelete() throws Exception {
    String url = "http://localhost:" + server.getPort() + "/wd/hub/status";
    HttpResponse response = HttpClientUtil.executeRequest(url, HttpMethod.DELETE);
    SelendroidAssert.assertResponseIsServerError(response);
  }

  @After()
  public void stopServers() {
    server.stop();
  }
}
