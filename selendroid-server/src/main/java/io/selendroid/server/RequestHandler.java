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

import org.json.JSONArray;
import org.json.JSONException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.server.model.DefaultSelendroidDriver;
import io.selendroid.server.model.KnownElements;
import io.selendroid.server.model.SelendroidDriver;
import org.webbitserver.HttpRequest;

import java.util.ArrayList;
import java.util.List;

public abstract class RequestHandler extends BaseRequestHandler {
  
  public RequestHandler(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  
  protected SelendroidDriver getSelendroidDriver() {
    DefaultSelendroidDriver driver =
        (DefaultSelendroidDriver) request.data().get(AndroidServlet.DRIVER_KEY);
    return driver;
  }

  protected String getIdOfKnownElement(AndroidElement element) {
    KnownElements knownElements = getKnownElements();
    if (knownElements == null) {
      return null;
    }
    return knownElements.getIdOfElement(element);
  }

  protected AndroidElement getElementFromCache(String id) {
    KnownElements knownElements = getKnownElements();
    if (knownElements == null) {
      return null;
    }
    return knownElements.get(id);
  }

  protected KnownElements getKnownElements() {
    if (getSelendroidDriver().getSession() == null) {
      return null;
    }
    return getSelendroidDriver().getSession().getKnownElements();
  }

  protected String[] extractKeysToSendFromPayload() throws JSONException {
    JSONArray valueArr = getPayload().getJSONArray("value");
    if (valueArr == null || valueArr.length() == 0) {
      throw new SelendroidException("No key to send to an element was found.");
    }
    List<CharSequence> temp = new ArrayList<CharSequence>();

    for (int i = 0; i < valueArr.length(); i++) {
      temp.add(valueArr.getString(i));
    }
    String[] keysToSend = temp.toArray(new String[0]);
    return keysToSend;
  }

  
}
