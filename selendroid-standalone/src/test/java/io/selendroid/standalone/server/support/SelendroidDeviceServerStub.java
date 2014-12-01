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
package io.selendroid.standalone.server.support;

import io.selendroid.server.common.SelendroidResponse;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class SelendroidDeviceServerStub extends NanoHTTPD {
  private final TestSessionListener testSessionListener;
  private int port;

  public SelendroidDeviceServerStub(int port, TestSessionListener testSessionListener)
      throws IOException {
    super(port, new File("."));
    this.port = port;
    System.out.println("SelendroidDeviceServerStub is started on the following port: " + port);
    this.testSessionListener = testSessionListener;
  }

  public int getPort() {
    return port;
  }

  public Response serve(String uri, String method, Properties header, Properties params,
      Properties files) {
    if (this.testSessionListener == null) {
      throw new IllegalStateException("Server must have one test session listener registered.");
    }
    try {
      if (uri.endsWith("/wd/hub/status") && isGet(method)) {
        return respond(testSessionListener.status(params));
      } else if (uri.endsWith("/wd/hub/session") && isPost(method)) {
        return respond(testSessionListener.createSession(params));
      } else if (uri.endsWith("/wd/hub/session/:sessionId") && isDelete(method)) {
        return respond(testSessionListener.deleteSession(params));
      } else if (uri.endsWith(testSessionListener.uriMapping)) {
        return respond(testSessionListener.executeSelendroidRequest(params));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, "ERROR OCCURRED");
  }

  private boolean isGet(String method) {
    return "GET".equals(method);
  }

  private boolean isPost(String method) {
    return "POST".equals(method);
  }

  private boolean isDelete(String method) {
    return "DELETE".equals(method);
  }

  private Response respond(SelendroidResponse response) {
    return new Response(HTTP_OK, "application/json", response.render());
  }
}
