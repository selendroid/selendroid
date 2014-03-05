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
package io.selendroid;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class SelendroidConfiguration {

  @Parameter(description = "port the server will listen on.", names = "-port")
  private int port = 4444;

  @Parameter(description = "timeout that will be used to start Android emulators", names = "-timeoutEmulatorStart")
  private long timeoutEmulatorStart = 300000;

  @Parameter(description = "location of the application under test. Absolute path to the apk", names = {
      "-app", "-aut" })
  private List<String> supportedApps = new ArrayList<String>();

  @Parameter(names = "-verbose", description = "Debug mode")
  private boolean verbose = false;

  @Parameter(names = "-emulatorPort", description = "port number to start running emulators on")
  private int emulatorPort = 5560;

  @Parameter(names = "-deviceScreenshot", description = "if true, screenshots will be taken on the device instead of using the ddmlib libary.")
  private boolean deviceScreenshot = false;

  @Parameter(description = "the port the selendroid-standalone is using to communicate with instrumentation server", names = { "-selendroidServerPort" })
  private int selendroidServerPort = 8080;

  @Parameter(description = "The file of the keystore to be used", names = { "-keystore" })
  private String keystore = null;
  
  @Parameter(description = "The emulator options used for starting emulators: e.g. -no-audio", names = { "-emulatorOptions" })
  private String emulatorOptions = null;
  
  @Parameter(description = "if specified, will send a registration request to the given url. Example : http://localhost:4444/grid/register", names = "-hub")
  private String registrationUrl = null;

  @Parameter(description = "if specified, will specify the remote proxy to use on the grid. Example : io.selendroid.grid.SelendroidSessionProxy", names = "-proxy")
  private String proxy = null;

  @Parameter(description = "host of the node. Ip address needs to be specified for registering to a grid hub (guessing can be wrong complex).", names = "-host")
  private String serverHost;

  @Parameter(names = "-keepAdbAlive", description = "If true, adb will not be terminated on server shutdown.")
  private boolean keepAdbAlive = false;

  @Parameter(names = "-noWebviewApp", description = "If you don't want selendroid to auto-extract and have 'AndroidDriver' (webview only app) available.")
  private boolean noWebViewApp = false;

  @Parameter(names = "-noClearData", description = "When you quit the app, shell pm clear will not be called with this option specified.")
  private boolean noClearData = false;
  
  public void setKeystore(String keystore) {
    this.keystore = keystore;
  }
  
  public String getKeystore() {
    return keystore;
  }
  
  public void setSelendroidServerPort(int selendroidServerPort) {
    this.selendroidServerPort = selendroidServerPort;
  }

  public int getSelendroidServerPort() {
    return selendroidServerPort;
  }
  
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

  public void setEmulatorPort(int port) {
    emulatorPort = port;
  }

  public int getEmulatorPort() {
    return emulatorPort;
  }

  public long getTimeoutEmulatorStart() {
    return timeoutEmulatorStart;
  }

  public void setTimeoutEmulatorStart(long timeoutEmulatorStart) {
    this.timeoutEmulatorStart = timeoutEmulatorStart;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public boolean isDeviceScreenshot() {
    return deviceScreenshot;
  }

  public void setDeviceScreenshot(boolean deviceScreenshot) {
    this.deviceScreenshot = deviceScreenshot;
  }
  
  public String getRegistrationUrl() {
   return registrationUrl;
  }
  
  public void setRegistrationUrl(String registrationUrl) {
    this.registrationUrl = registrationUrl;
  }
    
  public String getProxy() {
    return proxy;
  }
  
  public void setProxy(String proxy) {
    this.proxy = proxy;
  }
  
  public String getServerHost() {
    return serverHost;
  }
    
  public void setServerHost(String serverHost) {
    this.serverHost = serverHost;
  }

  public String getEmulatorOptions() {
    return emulatorOptions;
  }

  public void setEmulatorOptions(String qemu) {
    this.emulatorOptions = qemu;
  }

  public boolean shouldKeepAdbAlive() {
    return keepAdbAlive;
  }

  public void setShouldKeepAdbAlive(boolean keepAdbAlive) {
    this.keepAdbAlive = keepAdbAlive;
  }

  public boolean isNoWebViewApp() {
    return noWebViewApp;
  }

  public void setNoWebViewApp(boolean noWebViewApp) {
    this.noWebViewApp = noWebViewApp;
  }

  public boolean isNoClearData() {
    return noClearData;
  }

  public void setNoClearData(boolean noClearData) {
    this.noClearData = noClearData;
  }
}
