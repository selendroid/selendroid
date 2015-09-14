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
package io.selendroid.standalone.server.model;

import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.AndroidDevice;

import java.util.Timer;

public class ActiveSession {
  private final String sessionId;
  private AndroidApp aut;
  private AndroidDevice device;
  private SelendroidCapabilities desiredCapabilities;
  private final int selendroidServerPort;
  private boolean invalid = false;
  private final Timer stopSessionTimer = new Timer(true);
  private String hostname;

  ActiveSession(String sessionId, SelendroidCapabilities desiredCapabilities, AndroidApp aut,
                AndroidDevice device, int selendroidPort, SelendroidStandaloneDriver driver, String hostname) {
    this.selendroidServerPort = selendroidPort;
    this.sessionId = sessionId;
    this.aut = aut;
    this.device = device;
    this.desiredCapabilities = desiredCapabilities;
    this.hostname = hostname;
    stopSessionTimer.schedule(new SessionTimeoutTask(driver, sessionId), driver
        .getSelendroidConfiguration().getSessionTimeoutMillis());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ActiveSession other = (ActiveSession) obj;
    if (sessionId == null) {
      if (other.sessionId != null) return false;
    } else if (!sessionId.equals(other.sessionId)) return false;
    return true;
  }

  public AndroidApp getAut() {
    return aut;
  }

  public String getHostname() { return hostname; }

  public int getSelendroidServerPort() {
    return selendroidServerPort;
  }

  public SelendroidCapabilities getDesiredCapabilities() {
    return desiredCapabilities;
  }

  public AndroidDevice getDevice() {
    return device;
  }

  public String getSessionId() {
    return sessionId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
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

  public void stopSessionTimer() {
    stopSessionTimer.cancel();
  }

  @Override
  public String toString() {
    return "ActiveSession [sessionId=" + sessionId + ", aut=" + aut + ", device=" + device + "]";
  }
}
