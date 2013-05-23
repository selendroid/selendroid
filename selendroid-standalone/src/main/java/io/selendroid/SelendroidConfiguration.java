/*
 * Copyright 2013 selendroid committers.
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
package io.selendroid;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class SelendroidConfiguration {
  @Parameter(description = "port the server will listen on.", names = "-port")
  private int port = 5555;

  @Parameter(description = "timeout that will be used to start Android emulators", names = "-timeoutEmulatorStart")
  private long timeoutEmulatorStart = 120000;


  @Parameter(description = "location of the application under test. Absolute path to the apk", names = {
      "-app", "-aut"}, required = true)
  private List<String> supportedApps = new ArrayList<String>();

  public void addSupportedApp(String appAbsolutPath) {
    supportedApps.add(appAbsolutPath);
  }

  public List<String> getSupportedApps() {
    return supportedApps;
  }

  public static SelendroidConfiguration create(String[] args) {
    SelendroidConfiguration res = new SelendroidConfiguration();
    new JCommander(res).parse(args);
    return res;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getPort() {
    return this.port;
  }

  public long getTimeoutEmulatorStart() {
    return timeoutEmulatorStart;
  }

  public void setTimeoutEmulatorStart(long timeoutEmulatorStart) {
    this.timeoutEmulatorStart = timeoutEmulatorStart;
  }

}
