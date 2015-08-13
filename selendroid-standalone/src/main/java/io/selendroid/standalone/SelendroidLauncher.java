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
package io.selendroid.standalone;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Throwables;

import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.standalone.log.LogLevelEnum;
import io.selendroid.standalone.server.SelendroidStandaloneServer;
import io.selendroid.standalone.server.model.impl.DefaultInitAndroidDevicesStrategy;
import io.selendroid.standalone.server.model.impl.InitAndroidDevicesStrategy;
import io.selendroid.standalone.server.util.HttpClientUtil;

import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public class SelendroidLauncher {

  public static final String LOGGER_NAME = "io.selendroid";
  private static final Logger log = Logger.getLogger(SelendroidLauncher.class.getName());
  private SelendroidStandaloneServer server = null;
  private SelendroidConfiguration config = null;

  public SelendroidLauncher(SelendroidConfiguration config) {
    this.config = config;
  }

  public static SelendroidConfiguration parseConfig(String[] args) {
    SelendroidConfiguration config = new SelendroidConfiguration();
    JCommander jCommander;
    try {
      jCommander = new JCommander(config, args);
      jCommander.setProgramName("Selendroid Standalone Server");
    } catch (ParameterException e) {
      log.log(Level.SEVERE, "An error occurred while starting selendroid");
      throw Throwables.propagate(e);
    }

    if(config.isPrintHelp()) {
      jCommander.usage();
      System.exit(0);
    }

    return config;
  }

  /**
   * Starts the Selendroid standalone server and exits immediately.
   * This method might return before the server is ready to receive requests.
   */
  private void launchServer(InitAndroidDevicesStrategy initAndroidDevicesStrategy) {
    try {
      log.info("Starting Selendroid standalone on port " + config.getPort());
      server = new SelendroidStandaloneServer(config, initAndroidDevicesStrategy);
      server.start();
    } catch (AndroidSdkException e) {
      log.severe("Selendroid standalone was not able to interact with the Android SDK: " + e.getMessage());
      log.severe(
          "Please make sure you have the latest version with the latest updates installed: ");
      log.severe("http://developer.android.com/sdk/index.html");
      throw Throwables.propagate(e);
    } catch (Exception e) {
      log.severe("Error building server: " + e.getMessage());
      throw Throwables.propagate(e);
    }
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        log.info("Shutting down Selendroid standalone");
        stopSelendroid();
      }
    });
  }

  /**
   * Starts the Selendroid standalone server and waits until it's ready to accept requests.
   * @throws io.selendroid.server.common.exceptions.SelendroidException if the server didn't come up
   */
  public void launchSelendroid(InitAndroidDevicesStrategy initAndroidDevicesStrategy) {
    launchServer(initAndroidDevicesStrategy);
    if (config.isGrid()) {
      // Longer timeout to allow for grid registration
      HttpClientUtil.waitForServer(config.getPort(), 3, TimeUnit.MINUTES);
    } else {
      HttpClientUtil.waitForServer(config.getPort(), 20, TimeUnit.SECONDS);
    }
  }

  public static void main(String[] args) {
    try {
      configureLogging();
    } catch (Exception e1) {
      log.severe("Error occurred while registering logging file handler.");
    }
    log.info("################# Selendroid #################");
    SelendroidConfiguration config = parseConfig(args);
    // Log the loaded configuration
    log.info("################# Configuration in use #################");
    log.info(config.toString());

    //to be backward compatible
    if (LogLevelEnum.ERROR.equals(config.getLogLevel())) {
      Logger.getLogger(LOGGER_NAME).setLevel(LogLevelEnum.VERBOSE.level);
    } else {
      Logger.getLogger(LOGGER_NAME).setLevel(config.getLogLevel().level);
    }
    new SelendroidLauncher(config).launchServer(new DefaultInitAndroidDevicesStrategy());
  }

  private static void configureLogging() throws Exception {
    Handler fh = new FileHandler("%h/selendroid.log", 2097152, 1);

    fh.setFormatter(new SimpleFormatter());
    Logger.getLogger(LOGGER_NAME).addHandler(fh);
  }

  public void stopSelendroid() {
    if (server != null) {
      server.stop();
    }
  }

  public SelendroidStandaloneServer getServer() {
    return server;
  }
}
