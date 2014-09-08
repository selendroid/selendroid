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
package io.selendroid.server.model;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class Session {
  public static final String SEND_KEYS_TO_ELEMENT = "sendKeysToElement";
  public static final String NATIVE_EVENTS_PROPERTY = "nativeEvents";

  private JSONObject capabilities = null;
  private KnownElements knownElements = null;
  private String sessionId = null;
  private Map<String, JSONObject> commandConfiguration;

  public Session(JSONObject capabilities, String sessionId) {
    this.capabilities = capabilities;
    this.sessionId = sessionId;
    this.knownElements = new KnownElements();
    this.commandConfiguration = new HashMap<String, JSONObject>();
    JSONObject configJsonObject = new JSONObject();
    try {
      boolean nativeEvents = true;
      if (capabilities.has(NATIVE_EVENTS_PROPERTY)) {
        nativeEvents = capabilities.getBoolean(NATIVE_EVENTS_PROPERTY);
      }
      configJsonObject.put(NATIVE_EVENTS_PROPERTY, nativeEvents);
    } catch (JSONException e) {
    }
    this.commandConfiguration.put(SEND_KEYS_TO_ELEMENT, configJsonObject);
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

  public void setCommandConfiguration(String command, JSONObject config) {
    if (commandConfiguration.containsKey(command)) {
      commandConfiguration.remove(command);
    }
    commandConfiguration.put(command, config);
  }

  public JSONObject getCommandConfiguration(String command) {
    if (commandConfiguration.containsKey(command)) {
      return commandConfiguration.get(command);
    }
    return null;
  }


}
