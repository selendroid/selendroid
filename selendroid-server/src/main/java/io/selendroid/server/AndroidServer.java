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
package io.selendroid.server;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

import io.selendroid.ServerInstrumentation;
import io.selendroid.server.inspector.InspectorServlet;
import io.selendroid.server.model.DefaultSelendroidDriver;
import io.selendroid.server.model.SelendroidDriver;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

public class AndroidServer {
  private int driverPort = 8080;
  private WebServer webServer;

  /** for testing only */
  protected AndroidServer(int port, ServerInstrumentation androidInstrumentation)
      throws UnknownHostException {
    this.driverPort = port;

    URI remoteUri =
        URI.create("http://127.0.0.1" + (driverPort == 80 ? "" : (":" + driverPort)) + "/");

    webServer =
        WebServers.createWebServer(Executors.newCachedThreadPool(), new InetSocketAddress(
            driverPort), remoteUri);
    init(androidInstrumentation);
  }

  public AndroidServer(ServerInstrumentation androidInstrumentation, int port) {
    driverPort = port;
    webServer = WebServers.createWebServer(Executors.newCachedThreadPool(), driverPort);
    init(androidInstrumentation);
  }

  protected void init(ServerInstrumentation androidInstrumentation) {
    SelendroidDriver driver = new DefaultSelendroidDriver(androidInstrumentation);
    // seems like this must be set first
    webServer.staleConnectionTimeout(604800000); // 1 week.
    // If the stale connection cleanup is called a ConcurrentModification exception will be thrown.
    // Thus the significantly high timeout.
    webServer.add("/wd/hub/status", new StatusServlet(androidInstrumentation));
    webServer.add(new InspectorServlet(driver, androidInstrumentation));
    webServer.add(new AndroidServlet(driver));
  }

  /**
   * just make sure if the server timeout is set that this method is called as well.
   * 
   * @param millies
   */
  public void setConnectionTimeout(long millies) {
    System.out.println("using staleConnectionTimeout: " + millies);
    webServer.staleConnectionTimeout(millies);
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
