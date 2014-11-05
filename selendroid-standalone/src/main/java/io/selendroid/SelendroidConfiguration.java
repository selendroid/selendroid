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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import io.selendroid.log.LogLevelConverter;
import io.selendroid.log.LogLevelEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

public class SelendroidConfiguration {

  @Parameter(description = "port the server will listen on.", names = "-port")
  private int port = 4444;

  @Parameter(description = "timeout that will be used to start Android emulators",
             names = "-timeoutEmulatorStart")
  private long timeoutEmulatorStart = 300000;

  @Parameter(description = "location of the application under test. Absolute path to the apk",
             names = {
                 "-app", "-aut"})
  private List<String> supportedApps = new ArrayList<String>();

  @Deprecated
  @Parameter(names = "-verbose", description = "Debug mode")
  private boolean verbose = false;

  @Parameter(names = "-emulatorPort", description = "port number to start running emulators on")
  private int emulatorPort = 5560;

  @Parameter(names = "-deviceScreenshot",
             description = "if true, screenshots will be taken on the device instead of using the ddmlib libary.")
  private boolean deviceScreenshot = false;

  @Parameter(
      description = "the port the selendroid-standalone is using to communicate with instrumentation server",
      names = {"-selendroidServerPort"})
  private int selendroidServerPort = 8080;

  @Parameter(description = "The file of the keystore to be used", names = {"-keystore"})
  private String keystore = null;

  @Parameter(description = "The password for the keystore to be used", names = {"-keystorePassword"})
  private String keystorePassword = null;

  @Parameter(description = "The alias of the keystore to be used", names = {"-keystoreAlias"})
  private String keystoreAlias = null;

  @Parameter(description = "The emulator options used for starting emulators: e.g. -no-audio",
             names = {"-emulatorOptions"})
  private String emulatorOptions = null;

  @Parameter(
      description = "if specified, will send a registration request to the given url. Example : http://localhost:4444/grid/register",
      names = "-hub")
  private String registrationUrl = null;

  @Parameter(
      description = "if specified, will specify the remote proxy to use on the grid. Example : io.selendroid.grid.SelendroidSessionProxy",
      names = "-proxy")
  private String proxy = null;

  @Parameter(
      description = "host of the node. Ip address needs to be specified for registering to a grid hub (guessing can be wrong complex).",
      names = "-host")
  private String serverHost;

  @Parameter(names = "-keepAdbAlive",
             description = "If true, adb will not be terminated on server shutdown.")
  private boolean keepAdbAlive = false;

  @Parameter(names = "-noWebviewApp",
             description = "If you don't want selendroid to auto-extract and have 'AndroidDriver' (webview only app) available.")
  private boolean noWebViewApp = false;

  @Parameter(names = "-noClearData",
             description = "When you quit the app, shell pm clear will not be called with this option specified.")
  private boolean noClearData = false;

  @Parameter(
      description = "maximum session duration in seconds. Session will be forcefully terminated if it takes longer.",
      names = "-sessionTimeout")
  private int sessionTimeoutSeconds = 30 * 60; // 30 minutes

  @Parameter(names = "-forceReinstall",
             description = "Forces Selendroid Server and the app under test to be reinstalled (for Selendroid developers)")
  private boolean forceReinstall = false;

  @Parameter(names = "-logLevel", converter = LogLevelConverter.class,
             description = "Specifies the log level of selendroid. Available values are: ERROR, WARNING, INFO, DEBUG and VERBOSE.")
  private LogLevelEnum logLevel = LogLevelEnum.ERROR;

  @Parameter(names = "-deviceLog",
             description = "Specifies whether or not adb logging should be enabled for the device running the test",
             arity = 1)
  private boolean deviceLog = true;

  @Parameter(description = "Maximum time in milliseconds to wait for the selendroid-server to come up on the device",
      names = "-serverStartTimeout")
  private long serverStartTimeout = 20000;

  @Parameter(names = {"-h", "--help"}, description = "Prints usage instructions to the terminal")
  private boolean printHelp = false;

  @Parameter(names="-serverStartRetries",
             description="Maximum amount of times the starting of the selendroid-server on the device will be retried")
  private int serverStartRetries = 5;

  public void setKeystore(String keystore) {
    this.keystore = keystore;
  }

  public String getKeystore() {
    return keystore;
  }

  public void setKeystorePassword(String keystorePassword) {
	    this.keystorePassword = keystorePassword;
	  }

  public String getKeystorePassword() {
	    return keystorePassword;
	  }

  public void setKeystoreAlias(String keystoreAlias) {
	    this.keystoreAlias = keystoreAlias;
	  }

  public String getKeystoreAlias() {
	    return keystoreAlias;
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

  public int getSessionTimeoutMillis() {
    return sessionTimeoutSeconds * 1000;
  }

  public void setSessionTimeoutSeconds(int sessionTimeoutSeconds) {
    this.sessionTimeoutSeconds = sessionTimeoutSeconds;
  }

  public boolean isForceReinstall() {
    return forceReinstall;
  }

  public void setForceReinstall(boolean forceReinstall) {
    this.forceReinstall = forceReinstall;
  }

  public LogLevelEnum getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(LogLevelEnum logLevel) {
    this.logLevel = logLevel;
  }

  public boolean isDeviceLog() {
    return deviceLog;
  }

  public void setDeviceLog(boolean deviceLog) {
    this.deviceLog = deviceLog;
  }

  public long getServerStartTimeout() {
    return serverStartTimeout;
  }

  public void setServerStartTimeout(long serverStartTimeout) {
    this.serverStartTimeout = serverStartTimeout;
  }

  public int getServerStartRetries() {
    return serverStartRetries;
  }

  public void setServerStartRetries(int serverStartRetries) {
    this.serverStartRetries = serverStartRetries;
  }

  public boolean isPrintHelp() {
    return printHelp;
  }

  public void setPrintHelp(boolean printHelp) {
    this.printHelp = printHelp;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
