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
package io.selendroid;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Throwables;
import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.io.ShellCommand;
import io.selendroid.server.SelendroidStandaloneServer;
import io.selendroid.server.util.HttpClientUtil;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.util.concurrent.TimeUnit.MINUTES;

public class SelendroidLauncher {
  public static final String LOGGER_NAME = "io.selendroid";
  private static final Logger log = Logger.getLogger(SelendroidLauncher.class.getName());
  private SelendroidStandaloneServer server = null;
  private SelendroidConfiguration config = null;
  
  public static SelendroidLauncher getInstance(String[] args) {
	try {
      configureLogging();
    } catch (Exception e1) {
      log.severe("Error occurred while registering logging file handler.");
    }
    log.info("################# Selendroid #################");
    SelendroidConfiguration config = new SelendroidConfiguration();
    try {
      new JCommander(config, args);
    } catch (ParameterException e) {
      log.severe("An error occurred while starting selendroid: " + e.getMessage());
      System.exit(0);
    }
    if (config.isVerbose()) {
      log.setLevel(Level.FINE);
      ShellCommand.setVerbose();
    }
    SelendroidLauncher launcher = new SelendroidLauncher(config);  
    return launcher;
  }
  
  public SelendroidLauncher(SelendroidConfiguration config) {
    this.config = config;
  }

  private void lauchServer() {
    try {
      log.info("Starting selendroid-server port " + config.getPort());
      server = new SelendroidStandaloneServer(config);
      server.start();
    } catch (AndroidSdkException e) {
      log.severe("Selendroid was not able to interact with the Android SDK: " + e.getMessage());
      log.severe("Please make sure you have the latest version with the latest updates installed: ");
      log.severe("http://developer.android.com/sdk/index.html");
      throw Throwables.propagate(e);
    } catch (Exception e) {
      log.severe("Error occurred while building server: " + e.getMessage());
      e.printStackTrace();
      throw Throwables.propagate(e);
    }
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        log.info("Shutting down Selendroid standalone");
        if (server != null) {
          server.stop();
        }
      }
    });
  }

  public void lauchSelendroid() {
    lauchServer();
    waitForServer(config.getPort());
  }

  public static void main(String[] args) {
    try {
      configureLogging();
    } catch (Exception e1) {
      log.severe("Error occurred while registering logging file handler.");
    }

    log.info("################# Selendroid #################");
    SelendroidConfiguration config = new SelendroidConfiguration();
    try {
      new JCommander(config, args);
    } catch (ParameterException e) {
      log.severe("An error occurred while starting selendroid: " + e.getMessage());
      System.exit(0);
    }
    if (config.isVerbose()) {
      log.setLevel(Level.FINE);
      ShellCommand.setVerbose();
    }
    SelendroidLauncher laucher = new SelendroidLauncher(config);
    laucher.lauchServer();
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

  private void waitForServer(int port) {
    long end = System.currentTimeMillis() + MINUTES.toMillis(3);

    while (!HttpClientUtil.isServerStarted(port) && System.currentTimeMillis() < end ) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        throw Throwables.propagate(e);
      }
    }
  }
}
