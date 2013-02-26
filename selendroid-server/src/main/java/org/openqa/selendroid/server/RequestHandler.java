/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.server;

import org.openqa.selendroid.android.WindowType;
import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.server.model.KnownElements;
import org.openqa.selendroid.server.model.SelendroidDriver;
import org.openqa.selendroid.server.model.SelendroidNativeDriver;
import org.webbitserver.HttpRequest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class RequestHandler {
  private HttpRequest request = null;
  private String mappedUri = null;

  public RequestHandler(HttpRequest request, String mappedUri) {
    this.request = request;
    this.mappedUri = mappedUri;
  }

  public String getMappedUri() {
    return mappedUri;
  }

  public String getSessionId() {
    if (request.data().containsKey(AndroidServlet.SESSION_ID_KEY)) {
      return (String) request.data().get(AndroidServlet.SESSION_ID_KEY);
    }
    return null;
  }

  public Long getElementId() {
    if (request.data().containsKey(AndroidServlet.ELEMENT_ID_KEY)) {
      return (Long) request.data().get(AndroidServlet.ELEMENT_ID_KEY);
    }
    return null;
  }

  public JsonObject getPayload() {
    String json = request.body();
    if (json != null && !json.isEmpty()) {
      return (JsonObject) new JsonParser().parse(json);
    }
    return null;
  }

  protected SelendroidDriver getAndroidDriver() {
    SelendroidNativeDriver driver =
        (SelendroidNativeDriver) request.data().get(AndroidServlet.DRIVER_KEY);
    if (isWebviewWindow(driver)) {
      return driver.getSession().getWebviewDriver(driver.getServerInstrumentation());
    } else {
      return driver;
    }
  }

  protected WindowType getCurrentWindowType() {
    return getAndroidDriver().getSession().getActiveWindowType();
  }

  protected boolean isWebviewWindow(SelendroidDriver driver) {
    Session session = driver.getSession();

    if (session != null && WindowType.WEBVIEW.equals(session.getActiveWindowType())) {
      return true;
    }
    return false;
  }

  protected void switchToWebViewDriver() {
    Session session = getAndroidDriver().getSession();
    session.setActiveWindowType(WindowType.WEBVIEW);
    // to initialize the driver already
    getAndroidDriver();
  }

  protected void switchToNativeDriver() {
    getAndroidDriver().getSession().setActiveWindowType(WindowType.NATIVE_APP);
  }

  protected Long getIdOfKnownElement(AndroidElement element) {
    KnownElements knownElements = getKnownElements();
    if (knownElements == null) {
      return null;
    }
    return knownElements.getIdOfElement(element);
  }

  protected AndroidElement getElementFromCache(Long id) {
    KnownElements knownElements = getKnownElements();
    if (knownElements == null) {
      return null;
    }
    return knownElements.get(id);
  }

  protected KnownElements getKnownElements() {
    if (getAndroidDriver().getSession() == null) {
      return null;
    }
    return getAndroidDriver().getSession().getKnownElements();
  }

  public abstract Response handle();
}
