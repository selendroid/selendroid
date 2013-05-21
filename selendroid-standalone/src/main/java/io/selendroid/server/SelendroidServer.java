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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.openqa.selendroid.server.StatusServlet;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

public class SelendroidServer {
  private static final Logger log = Logger.getLogger(SelendroidServer.class.getName());
  private WebServer webServer;
  private SelendroidConfiguration configuration;
  private SelendroidDriver driver = null;

  /**
   * for testing only
   * 
   * @throws AndroidSdkException
   */
  protected SelendroidServer(SelendroidConfiguration configuration, SelendroidDriver driver)
      throws AndroidSdkException {
    this.configuration = configuration;
    this.driver = driver;
    webServer =
        WebServers.createWebServer(Executors.newCachedThreadPool(), new InetSocketAddress(
            configuration.getPort()), URI.create("http://127.0.0.1"
            + (configuration.getPort() == 80 ? "" : (":" + configuration.getPort())) + "/"));
    init();
  }

  public SelendroidServer(SelendroidConfiguration configuration) throws AndroidSdkException {
    this.configuration = configuration;
    webServer =
        WebServers.createWebServer(Executors.newCachedThreadPool(), new InetSocketAddress(
            configuration.getPort()), remotelUri(configuration.getPort()));
    driver = initializeSelendroidServer();
    init();
  }

  private static URI remotelUri(int port) {
    try {
      URI remoteUri =
          URI.create("http://" + InetAddress.getLocalHost().getHostAddress()
              + (port == 80 ? "" : (":" + port)) + "/");
      return remoteUri;
    } catch (UnknownHostException e) {
      throw new RuntimeException(
          "can not create URI from localhost hostname - use constructor to pass an explicit URI", e);
    }
  }

  protected void init() throws AndroidSdkException {

    webServer.add("/wd/hub/status", new StatusServlet(driver));
    webServer.add(new SelendroidServlet(driver));
    webServer.staleConnectionTimeout(5 * 60 * 1000);
  }

  protected SelendroidDriver initializeSelendroidServer() throws AndroidSdkException {
    return new SelendroidDriver(configuration);
  }

  public void start() {
    webServer.start();
    log.info("selendroid-standalone server has been started on port: " + configuration.getPort());
  }

  public void stop() {
    log.info("About to stop selendroid-standalone server");
    driver.quitSelendroid();
    webServer.stop();
  }

  public int getPort() {
    return webServer.getPort();
  }
}
