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
package io.selendroid.server.model;

import io.selendroid.SelendroidCapabilities;
import io.selendroid.android.AndroidApp;
import io.selendroid.android.AndroidDevice;

public class ActiveSession {
  private final String sessionKey;
  private AndroidApp aut;
  private AndroidDevice device;
  private SelendroidCapabilities desiredCapabilities;
  private final int selendroidServerPort;
  private boolean invalid = false;

  ActiveSession(String sessionKey, SelendroidCapabilities desiredCapabilities, AndroidApp aut,
      AndroidDevice device, int selendroidPort) {
    this.selendroidServerPort = selendroidPort;
    this.sessionKey = sessionKey;
    this.aut = aut;
    this.device = device;
    this.desiredCapabilities = desiredCapabilities;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ActiveSession other = (ActiveSession) obj;
    if (sessionKey == null) {
      if (other.sessionKey != null) return false;
    } else if (!sessionKey.equals(other.sessionKey)) return false;
    return true;
  }

  public AndroidApp getAut() {
    return aut;
  }

  public int getSelendroidServerPort() {
    return selendroidServerPort;
  }

  public SelendroidCapabilities getDesiredCapabilities() {
    return desiredCapabilities;
  }

  public AndroidDevice getDevice() {
    return device;
  }

  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((sessionKey == null) ? 0 : sessionKey.hashCode());
    return result;
  }

  public boolean isInvalid() {
    return invalid;
  }

  /**
   * marks the session as invalid. This happens when e.g. the hardware device has been disconnected.
   */
  public void invalidate() {
    this.invalid = true;

  }

  @Override
  public String toString() {
    return "ActiveSession [sessionKey=" + sessionKey + ", aut=" + aut + ", device=" + device + "]";
  }
}
