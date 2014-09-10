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
package io.selendroid.server;

import io.selendroid.SelendroidConfiguration;
import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.server.grid.SelfRegisteringRemote;
import io.selendroid.server.http.HttpServer;
import io.selendroid.server.model.SelendroidStandaloneDriver;

import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

public class SelendroidStandaloneServer {
  private static final Logger log = Logger.getLogger(SelendroidStandaloneServer.class.getName());
  private HttpServer webServer;
  private SelendroidConfiguration configuration;
  private SelendroidStandaloneDriver driver = null;

  /**
   * for testing only
   *
   * @throws AndroidSdkException
   */
  protected SelendroidStandaloneServer(SelendroidConfiguration configuration,
                                       SelendroidStandaloneDriver driver) throws AndroidSdkException {
    this.configuration = configuration;
    this.driver = driver;
    webServer = new HttpServer(configuration.getPort());
    init();
  }

  public SelendroidStandaloneServer(SelendroidConfiguration configuration)
      throws AndroidSdkException, AndroidDeviceException {
    this.configuration = configuration;
    webServer = new HttpServer(configuration.getPort());
    driver = initializeSelendroidServer();
    init();
  }

  protected void init() throws AndroidSdkException {
    webServer.addHandler(new StatusServlet(driver));
    webServer.addHandler(new SelendroidServlet(driver, configuration));
  }

  protected SelendroidStandaloneDriver initializeSelendroidServer() throws AndroidSdkException,
      AndroidDeviceException {
    return new SelendroidStandaloneDriver(configuration);
  }

  public void start() {
    webServer.start();
    if (!StringUtils.isBlank(configuration.getRegistrationUrl())
        && !StringUtils.isBlank(configuration.getServerHost())) {
      try {
        new SelfRegisteringRemote(configuration, driver).performRegistration();
      } catch (Exception e) {
        log.severe("An error occured while registering selendroid into grid hub.");
        e.printStackTrace();
      }
    }
    System.out.println("selendroid-standalone server has been started on port: " + configuration.getPort());
  }

  public void stop() {
    log.info("About to stop selendroid-standalone server");
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
