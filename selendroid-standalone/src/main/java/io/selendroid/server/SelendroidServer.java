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
import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.server.model.SelendroidDriver;

import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.openqa.selendroid.server.StatusServlet;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

public class SelendroidServer {
  private static final Logger log = Logger.getLogger(SelendroidServer.class.getName());
  private int driverPort = 4444;
  private WebServer webServer;
  private SelendroidConfiguration configuration;
  private SelendroidDriver driver = null;

  /**
   * for testing only
   * 
   * @throws AndroidSdkException
   */
  protected SelendroidServer(int port, SelendroidConfiguration configuration,
      SelendroidDriver driver) throws AndroidSdkException {
    this.driverPort = port;
    this.configuration = configuration;
    this.driver = driver;
    init();
  }

  public SelendroidServer(SelendroidConfiguration configuration) throws AndroidSdkException {
    this.configuration = configuration;
    driver = initializeSelendroidServer();
    init();
  }

  protected void init() throws AndroidSdkException {
    webServer = WebServers.createWebServer(Executors.newCachedThreadPool(), driverPort);
    webServer.add("/wd/hub/status", new StatusServlet(driver));
    webServer.add(new SelendroidServlet(driver));
  }

  protected SelendroidDriver initializeSelendroidServer() throws AndroidSdkException {
    return new SelendroidDriver(configuration);
  }

  public void start() {
    webServer.start();
    log.info("selendroid-standalone server has been started on port: " + driverPort);
  }

  public void stop() {
    log.info("About to stop selendroid-standalone server");
    webServer.stop();
  }

  public int getPort() {
    return webServer.getPort();
  }
}
