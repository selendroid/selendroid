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
package io.selendroid.server;

import io.selendroid.SelendroidConfiguration;
import io.selendroid.server.model.SelendroidDriver;

import java.util.concurrent.Executors;

import org.openqa.selendroid.server.StatusServlet;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

public class SelendroidServer {
  private int driverPort = 4444;
  private WebServer webServer;
  private SelendroidConfiguration configuration;

  /** for testing only */
  protected SelendroidServer(int port, SelendroidConfiguration configuration) {
    this.driverPort = port;
    this.configuration = configuration;

    init();
  }



  public SelendroidServer(SelendroidConfiguration configuration) {
    init();
  }

  protected void init() {
    webServer = WebServers.createWebServer(Executors.newCachedThreadPool(), driverPort);
    SelendroidDriver driver = new SelendroidDriver();
    webServer.add("/wd/hub/status", new StatusServlet(driver));
    webServer.add(new SelendroidServlet(configuration, driver));
  }

  public void start() {
    webServer.start();
  }

  public void stop() {
    webServer.stop();
  }

  public int getPort() {
    return webServer.getPort();
  }
}
