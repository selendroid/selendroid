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
import org.openqa.selendroid.server.model.KnownElements;
import org.openqa.selendroid.server.model.SelendroidDriver;
import org.openqa.selendroid.server.model.SelendroidWebDriver;

import com.google.gson.JsonObject;

public class Session {
  private JsonObject capabilities = null;
  private KnownElements knownElements = null;
  private String sessionId = null;
  private WindowType activeWindowType = null;
  private SelendroidWebDriver webviewDriver = null;

  public Session(JsonObject capabilities, String sessionId, WindowType activeWindowType) {
    super();
    this.capabilities = capabilities;
    this.sessionId = sessionId;
    this.knownElements = new KnownElements();
    this.activeWindowType = activeWindowType;
  }

  public WindowType getActiveWindowType() {
    return activeWindowType;
  }

  /** TODO rethink Driver concept and especially instance sharing */
  public synchronized SelendroidDriver getWebviewDriver() {
    if (webviewDriver == null) {
      System.out.println("creating new instance of webviewdriver");
      webviewDriver = new SelendroidWebDriver(this);
    }
    return webviewDriver;
  }

  public void setActiveWindowType(WindowType activeWindowType) {
    this.activeWindowType = activeWindowType;
  }

  public JsonObject getCapabilities() {
    return capabilities;
  }

  public KnownElements getKnownElements() {
    return knownElements;
  }

  public String getSessionId() {
    return sessionId;
  }
}
