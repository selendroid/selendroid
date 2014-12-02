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
package io.selendroid.standalone.server;

import io.selendroid.server.common.StatusServlet;
import io.selendroid.server.common.http.HttpServer;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.standalone.server.grid.SelfRegisteringRemote;
import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.selendroid.standalone.server.util.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;

public class SelendroidStandaloneServer {
  private static final Logger log = Logger.getLogger(SelendroidStandaloneServer.class.getName());
  private HttpServer webServer;
  private SelendroidConfiguration config;
  private SelendroidStandaloneDriver driver = null;

  /**
   * for testing only
   *
   * @throws AndroidSdkException
   */
  protected SelendroidStandaloneServer(SelendroidConfiguration config,
                                       SelendroidStandaloneDriver driver) throws AndroidSdkException {
    this.config = config;
    this.driver = driver;
    webServer = new HttpServer(config.getPort());
    init();
  }

  public SelendroidStandaloneServer(SelendroidConfiguration config)
      throws AndroidSdkException, AndroidDeviceException {
    this.config = config;
    webServer = new HttpServer(config.getPort());
    driver = initializeSelendroidServer();
    init();
  }

  protected void init() throws AndroidSdkException {
    webServer.addHandler(new StatusServlet(driver));
    webServer.addHandler(new SelendroidServlet(driver, config));
  }

  protected SelendroidStandaloneDriver initializeSelendroidServer() throws AndroidSdkException,
      AndroidDeviceException {
    return new SelendroidStandaloneDriver(config);
  }

  public void start() {
    webServer.start();
    if (config.isGrid()) {
      try {
        new SelfRegisteringRemote(config, driver).performRegistration();
      } catch (Exception e) {
        log.log(Level.SEVERE, "Error registering selendroid into grid hub.", e);
      }
    }
    log.info("Selendroid standalone server has been started on port: " + config.getPort());
  }

  public void stop() {
    log.info("Stopping selendroid-standalone server");
    driver.quitSelendroid();
    webServer.stop();
  }

  public int getPort() {
    return webServer.getPort();
  }

  public SelendroidStandaloneDriver getDriver() {
    return driver;
  }
}
