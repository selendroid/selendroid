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
package io.selendroid.server.util;

import static java.util.concurrent.TimeUnit.MINUTES;

import io.netty.handler.codec.http.HttpMethod;
import io.selendroid.SelendroidCapabilities;

import java.util.logging.Logger;

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
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Throwables;

public class HttpClientUtil {
  private static final Logger log = Logger.getLogger(HttpClientUtil.class.getName());

  public static HttpClient getHttpClient() {
    return new DefaultHttpClient();
  }

  public static HttpResponse executeRequestWithPayload(String uri, int port, HttpMethod method,
      String payload) throws Exception {
    BasicHttpEntityEnclosingRequest request =
        new BasicHttpEntityEnclosingRequest(method.name(), uri);
    request.setEntity(new StringEntity(payload, "UTF-8"));

    return getHttpClient().execute(new HttpHost("localhost", port), request);
  }

  public static JSONObject parseJsonResponse(HttpResponse response) throws Exception {
    String r = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
    try {
      return new JSONObject(r);
    } catch (JSONException e) {
      log.severe("Failed to parse json response: " + r);
      throw e;
    }
  }

  public static HttpResponse executeRequest(String url, HttpMethod method) throws Exception {
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

  public static HttpResponse executeCreateSessionRequest(int port,
      SelendroidCapabilities desiredCapabilities) throws Exception {
    String url = "http://localhost:" + port + "/wd/hub/session";
    log.info("creating session by using url: " + url);
    JSONObject payload = new JSONObject();
    payload.put("desiredCapabilities", new JSONObject(desiredCapabilities.asMap()));
    HttpResponse response =
        executeRequestWithPayload(url, port, HttpMethod.POST, payload.toString());
    return response;
  }

  public static boolean isServerStarted(int port) {
    HttpResponse response = null;
    try {
      response = executeRequest("http://localhost:" + port + "/wd/hub/sessions", HttpMethod.GET);
    } catch (Exception e) {
      return false;
    }
    try {
      JSONObject result = parseJsonResponse(response);
      return result.getInt("status") == 0;
    } catch (Exception e) {
      return false;
    }
  }

  public static void waitForServer(int port) {
    long end = System.currentTimeMillis() + MINUTES.toMillis(3);

    while (!isServerStarted(port) && System.currentTimeMillis() < end) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        throw Throwables.propagate(e);
      }
    }
  }
}
