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

import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.server.model.DefaultSelendroidDriver;
import io.selendroid.server.model.KnownElements;
import io.selendroid.server.model.SelendroidDriver;
import org.json.JSONArray;
import org.json.JSONException;
import org.webbitserver.HttpRequest;

public abstract class RequestHandler extends BaseRequestHandler {

  public RequestHandler(String mappedUri) {
    super(mappedUri);
  }

  
  protected SelendroidDriver getSelendroidDriver(HttpRequest request) {
    return (DefaultSelendroidDriver) request.data().get(AndroidServlet.DRIVER_KEY);
  }

  protected String getIdOfKnownElement(HttpRequest request, AndroidElement element) {
    KnownElements knownElements = getKnownElements(request);
    if (knownElements == null) {
      return null;
    }
    return knownElements.getIdOfElement(element);
  }

  protected AndroidElement getElementFromCache(HttpRequest request, String id) {
    KnownElements knownElements = getKnownElements(request);
    if (knownElements == null) {
      return null;
    }
    return knownElements.get(id);
  }

  protected KnownElements getKnownElements(HttpRequest request) {
    if (getSelendroidDriver(request).getSession() == null) {
      return null;
    }
    return getSelendroidDriver(request).getSession().getKnownElements();
  }

  protected String[] extractKeysToSendFromPayload(HttpRequest request) throws JSONException {
    JSONArray valueArr = getPayload(request).getJSONArray("value");
    if (valueArr == null || valueArr.length() == 0) {
      throw new SelendroidException("No key to send to an element was found.");
    }

    String[] toReturn = new String[valueArr.length()];

    for (int i = 0; i < valueArr.length(); i++) {
      toReturn[i] = valueArr.getString(i);
    }

    return toReturn;
  }

}
