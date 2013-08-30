/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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

import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

public abstract class BaseRequestHandler {
  protected HttpRequest request = null;
  protected String mappedUri = null;

  public BaseRequestHandler(HttpRequest request, String mappedUri) {
    this.request = request;
    this.mappedUri = mappedUri;
  }

  public HttpRequest getHttpRequest() {
    return request;
  }

  public String getMappedUri() {
    return mappedUri;
  }

  public String getSessionId() {
    if (request.data().containsKey(BaseServlet.SESSION_ID_KEY)) {
      return (String) request.data().get(BaseServlet.SESSION_ID_KEY);
    }
    return null;
  }

  public String getElementId() {
    if (request.data().containsKey(BaseServlet.ELEMENT_ID_KEY)) {
      return (String) request.data().get(BaseServlet.ELEMENT_ID_KEY);
    }
    return null;
  }

  public String getNameAttribute() {
    if (request.data().containsKey(BaseServlet.NAME_ID_KEY)) {
      return (String) request.data().get(BaseServlet.NAME_ID_KEY);
    }
    return null;
  }

  public JSONObject getPayload() throws JSONException {
    String json = request.body();
    if (json != null && !json.isEmpty()) {
      return new JSONObject(json);
    }
    return new JSONObject();
  }
  
  public abstract Response handle() throws JSONException;
}
