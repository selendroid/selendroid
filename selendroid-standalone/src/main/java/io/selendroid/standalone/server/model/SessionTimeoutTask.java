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

import io.selendroid.standalone.exceptions.AndroidDeviceException;

import java.util.TimerTask;
import java.util.logging.Logger;

public class SessionTimeoutTask extends TimerTask {
  private static final Logger log = Logger.getLogger(SessionTimeoutTask.class.getName());
  private String sessionId;
  private SelendroidStandaloneDriver driver;

  public SessionTimeoutTask(SelendroidStandaloneDriver driver, String sessionId) {
    this.sessionId = sessionId;
    this.driver = driver;
  }

  @Override
  public void run() {
    int sessionTimeout = driver.getSelendroidConfiguration().getSessionTimeoutMillis();
    log.info("Stopping session after configured session timeout of " + (sessionTimeout / 1000)
        + " seconds.");
    try {
      driver.stopSession(sessionId);
    } catch (AndroidDeviceException e) {

      log.severe("While closing the session " + sessionId
          + " after a session time out an error occurred: " + e.getMessage());
    }
  }
}
