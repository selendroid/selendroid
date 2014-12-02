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
package io.selendroid.standalone.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.netty.handler.codec.http.HttpMethod;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;
import io.selendroid.standalone.server.util.HttpClientUtil;
import io.selendroid.standalone.util.SelendroidAssert;

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * TODO ddary: revisit when looking at buck
 */
public class SelendroidStatusHandlerTest {
  protected static int port = 7777;
  private static SelendroidStandaloneServer server;
  public static final String URL = "http://localhost:" + port + "/wd/hub/status";

  @BeforeClass
  public static void startServer() throws AndroidSdkException {
    SelendroidStandaloneDriver driver = mock(SelendroidStandaloneDriver.class);
    when(driver.getCpuArch()).thenReturn("x86");
    when(driver.getOsVersion()).thenReturn("osx");
    when(driver.getServerVersion()).thenReturn("dev");
    SelendroidConfiguration conf = new SelendroidConfiguration();
    conf.setPort(port);
    server = new SelendroidStandaloneServer(conf, driver);
    server.start();
  }

  // @Test()
  public void assertThatGetStatusHandlerIsRegistered() throws Exception {
    HttpResponse response = HttpClientUtil.executeRequest(URL, HttpMethod.GET);
    SelendroidAssert.assertResponseIsOk(response);
    JSONObject result = HttpClientUtil.parseJsonResponse(response);
    SelendroidAssert.assertResponseIsOk(response);

    Assert.assertFalse(result.has("sessionId"));
    JSONObject value = result.getJSONObject("value");
    Assert.assertEquals("dev", value.getJSONObject("build").getString("version"));
  }

  // @Test
  public void assertThatGetStatusHanlerIsNotRegisteredForPost() throws Exception {
    HttpResponse response = HttpClientUtil.executeRequest(URL, HttpMethod.POST);
    SelendroidAssert.assertResponseIsServerError(response);
  }

  // @Test
  public void assertThatGetStatusHanlerIsNotRegisteredForDelete() throws Exception {
    HttpResponse response = HttpClientUtil.executeRequest(URL, HttpMethod.DELETE);
    SelendroidAssert.assertResponseIsServerError(response);
  }

  @AfterClass()
  public static void stopServers() {
    server.stop();
  }
}
