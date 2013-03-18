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

import org.json.JSONObject;
import org.openqa.selendroid.server.model.KnownElements;

public class Session {
  private JSONObject capabilities = null;
  private KnownElements knownElements = null;
  private String sessionId = null;

  public Session(JSONObject capabilities, String sessionId) {
    this.capabilities = capabilities;
    this.sessionId = sessionId;
    this.knownElements = new KnownElements();
  }

  public JSONObject getCapabilities() {
    return capabilities;
  }

  public KnownElements getKnownElements() {
    return knownElements;
  }

  public String getSessionId() {
    return sessionId;
  }
}
