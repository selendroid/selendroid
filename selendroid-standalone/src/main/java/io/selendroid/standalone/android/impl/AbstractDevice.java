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
package io.selendroid.standalone.android.impl;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.common.model.ExternalStorageFile;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.AndroidDevice;
import io.selendroid.standalone.android.AndroidSdk;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.standalone.exceptions.ShellCommandException;
import io.selendroid.standalone.io.ShellCommand;
import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;
import org.apache.commons.exec.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.logging.LogEntry;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractDevice implements AndroidDevice {
  private static final Logger log = Logger.getLogger(AbstractDevice.class.getName());
  protected String serial = null;
  protected String model = null;
  protected String apiTargetType = "android";
  protected Integer port = null;
  protected IDevice device;
  private ByteArrayOutputStream logoutput;
  private ExecuteWatchdog logcatWatchdog;
  private static final Integer COMMAND_TIMEOUT = 20000;
  private boolean loggingEnabled = true;
  private String hostname = null;

  /**
   * Constructor meant to be used with Android Emulators because a reference to the {@link IDevice}
   * will become available if the emulator will be started. Please make sure that #setIDevice is
   * called on the emulator.
   * 
   * @param serial
   */
  public AbstractDevice(String serial) {
    this.serial = serial;
  }

  /**
   * Constructor mean to be used with Android Hardware devices because a reference to the
   * {@link IDevice} will be available immediately after they are connected.
   * 
   * @param device
   */
  public AbstractDevice(IDevice device) {
    this.device = device;
    this.serial = device.getSerialNumber();
  }

  protected AbstractDevice() {}

  protected boolean isSerialConfigured() {
    return serial != null && !serial.isEmpty();
  }

  public void setVerbose() {
    log.setLevel(Level.FINEST);
  }

  @Override
  public boolean isDeviceReady() {
    CommandLine command = adbCommand("shell", "getprop init.svc.bootanim");
    String bootAnimDisplayed = null;
    try {
      bootAnimDisplayed = ShellCommand.exec(command);
    } catch (ShellCommandException e) {
      log.log(Level.INFO, "Could not get property init.svc.bootanim", e);
    }
    return bootAnimDisplayed != null && bootAnimDisplayed.contains("stopped");
  }

  @Override
  public boolean isInstalled(String appBasePackage) throws AndroidSdkException {
    CommandLine command = adbCommand("shell", "pm", "list", "packages");

    command.addArgument(appBasePackage, false);
    String result = null;
    try {
      result = ShellCommand.exec(command);
    } catch (ShellCommandException e) {}

    return result != null && result.contains("package:" + appBasePackage);
  }

  @Override
  public boolean isInstalled(AndroidApp app) throws AndroidSdkException {
    return isInstalled(app.getBasePackage());
  }

  @Override
  public void install(AndroidApp app) throws AndroidSdkException {
    // Reinstall if already installed, Install otherwise
    CommandLine command = adbCommand("install", "-r", app.getAbsolutePath());

    String out = executeCommandQuietly(command, COMMAND_TIMEOUT * 6);
    try {
      // give it a second to recover from the install
      Thread.sleep(1000);
    } catch (InterruptedException ie) {
      throw new RuntimeException(ie);
    }
    if (!out.contains("Success")) {
      throw new AndroidSdkException("APK installation failed. Output:\n" + out);
    }
  }

  public boolean start(AndroidApp app) throws AndroidSdkException {
    if (!isInstalled(app)) {
      install(app);
    }

    String mainActivity = app.getMainActivity().replace(app.getBasePackage(), "");
    CommandLine command =
        adbCommand("shell", "am", "start", "-a", "android.intent.action.MAIN", "-n",
            app.getBasePackage() + "/" + mainActivity);

    String out = executeCommandQuietly(command);
    try {
      // give it a second to recover from the activity start
      Thread.sleep(1000);
    } catch (InterruptedException ie) {
      throw new RuntimeException(ie);
    }
    return out.contains("Starting: Intent");
  }
  
  protected String executeCommandQuietly(CommandLine command) {
    return executeCommandQuietly(command, COMMAND_TIMEOUT);
  }

  protected String executeCommandQuietly(CommandLine command, long timeout) {
    try {
      return ShellCommand.exec(command, timeout);
    } catch (ShellCommandException e) {
      String logMessage = String.format("Could not execute command: %s", command);
      log.log(Level.WARNING, logMessage, e);
      return "";
    }
  }

  @Override
  public void uninstall(AndroidApp app) throws AndroidSdkException {
    CommandLine command = adbCommand("uninstall", app.getBasePackage());

    executeCommandQuietly(command);
    try {
      // give it a second to recover from the uninstall
      Thread.sleep(1000);
    } catch (InterruptedException ie) {
      throw new RuntimeException(ie);
    }
  }

  @Override
  public void clearUserData(AndroidApp app) throws AndroidSdkException {
    CommandLine command = adbCommand("shell", "pm", "clear", app.getBasePackage());
    executeCommandQuietly(command);
  }

  @Override
  public void kill(AndroidApp aut) throws AndroidDeviceException, AndroidSdkException {
    try {
      CommandLine command = adbCommand("shell", "am", "force-stop", aut.getBasePackage());
      executeCommandQuietly(command);
    } finally {
      freeSelendroidPort();
    }

    if (logcatWatchdog != null && logcatWatchdog.isWatching()) {
      logcatWatchdog.destroyProcess();
      logcatWatchdog = null;
    }
  }

  private void freeSelendroidPort() {
    if (this.port == null) {
      // Not set
      return;
    }
    CommandLine command = adbCommand("forward", "--remove", "tcp:" + this.port);
    try {
      ShellCommand.exec(command, 20000);
    } catch (ShellCommandException e) {
      log.log(Level.WARNING, "Could not free Selendroid port", e);
    }
  }


  @Override
  public void startSelendroid(AndroidApp aut, int port, SelendroidCapabilities capabilities,
                              String hostname) throws AndroidSdkException {
    this.port = port;
    this.hostname = hostname;

    List<String> argList = Lists.newArrayList(
        "-e", "main_activity", aut.getMainActivity(),
        "-e", "server_port", Integer.toString(port));
    if (capabilities.getSelendroidExtensions() != null) {
      argList.addAll(Lists.newArrayList("-e", "load_extensions", "true"));
      if (capabilities.getBootstrapClassNames() != null) {
        argList.addAll(Lists.newArrayList("-e", "bootstrap", capabilities.getBootstrapClassNames()));
      }
    }
    argList.add("io.selendroid." + aut.getBasePackage() + "/io.selendroid.server.ServerInstrumentation");

    String[] args = argList.toArray(new String[argList.size()]);
    CommandLine command
        = adbCommand(ObjectArrays.concat(new String[]{"shell", "am", "instrument"}, args, String.class));

    String result = executeCommandQuietly(command);
    if (result.contains("FAILED")) {
      String genericMessage = "Could not start the app under test using instrumentation.";
      String detailedMessage;
      try {
        // Try again, waiting for instrumentation to finish. This way we'll get more error output.
        String[] instrumentCmd =
            ObjectArrays.concat(new String[]{"shell", "am", "instrument", "-w"}, args, String.class);
        CommandLine getDetailedErrorCommand = adbCommand(instrumentCmd);
        String detailedResult = executeCommandQuietly(getDetailedErrorCommand);
        if (detailedResult.contains("package")) {
          detailedMessage =
              genericMessage + " Is the correct app under test installed? Read the details below:\n" + detailedResult;
        } else {
          detailedMessage = genericMessage + " Read the details below:\n" + detailedResult;
        }
      } catch (Exception e) {
        // Can't get detailed results
        throw new SelendroidException(genericMessage, e);
      }
      throw new SelendroidException(detailedMessage);
    }

    forwardSelendroidPort(port);

    if(isLoggingEnabled()) {
      startLogging();
    }
  }

  public void forwardPort(int local, int remote) {
    CommandLine command = adbCommand("forward", "tcp:" + local, "tcp:" + remote);
    try {
      ShellCommand.exec(command, 20000);
    } catch (ShellCommandException forwardException) {
      String debugForwardList;
      try {
        debugForwardList = ShellCommand.exec(adbCommand("forward", "--list"), 10000);
      } catch (ShellCommandException listException) {
        debugForwardList = "Could not get list of forwarded ports.";
      }

      throw new SelendroidException(
          "Could not forward port: " + command + "\nList of forwarded ports:\n" + debugForwardList,
          forwardException);
    }
  }

  private void forwardSelendroidPort(int port) {
    forwardPort(port, port);
  }

  @Override
  public boolean isSelendroidRunning() {
    HttpClient httpClient = HttpClientBuilder.create().build();
    String url = "http://" + getHostname() + ":" + getSelendroidsPort() + "/wd/hub/status";
    log.info("Checking if the Selendroid server is running: " + url);
    HttpRequestBase request = new HttpGet(url);
    HttpResponse response;
    try {
      response = httpClient.execute(request);
    } catch (Exception e) {
      log.info("Can't connect to Selendroid server, assuming it is not running.");
      return false;
    }

    int statusCode = response.getStatusLine().getStatusCode();
    log.info("Got response status code: " + statusCode);
    String responseValue;
    try {
      responseValue = IOUtils.toString(response.getEntity().getContent());
      log.info("Got response value: " + responseValue);
    } catch (Exception e) {
      log.log(Level.INFO, "Error reading response from selendroid-server", e);
      log.info("Assuming server has not started");
      return false;
    }

    return statusCode == 200 && responseValue.contains("selendroid");
  }

  @Override
  public int getSelendroidsPort() {
    return port;
  }

  @Override
  public String getHostname() {
    return hostname;
  }

  @Override
  public List<LogEntry> getLogs() {
    List<LogEntry> logs = Lists.newArrayList();
    String result = logoutput != null ? logoutput.toString() : "";
    String[] lines = result.split("\\r?\\n");
    log.fine("getting logcat");
    for (String line : lines) {
      Level l;
      if (line.startsWith("I")) {
        l = Level.INFO;
      } else if (line.startsWith("W")) {
        l = Level.WARNING;
      } else if (line.startsWith("S")) {
        l = Level.SEVERE;
      } else {
        l = Level.FINE;
      }
      logs.add(new LogEntry(l, System.currentTimeMillis(), line));
      log.fine(line);
    }
    return logs;
  }

  @Override
  public boolean isLoggingEnabled() {
    return loggingEnabled;
  }

  @Override
  public void setLoggingEnabled(boolean loggingEnabled) {
    this.loggingEnabled = loggingEnabled;
  }

  private void startLogging() {
    logoutput = new ByteArrayOutputStream();
    DefaultExecutor exec = new DefaultExecutor();
    exec.setStreamHandler(new PumpStreamHandler(logoutput));
    CommandLine command = adbCommand("logcat", "ResourceType:S", "dalvikvm:S", "Trace:S", "SurfaceFlinger:S",
        "StrictMode:S", "ExchangeService:S", "SVGAndroid:S", "skia:S", "LoaderManager:S", "ActivityThread:S", "-v", "time");
    log.info("starting logcat:");
    log.fine(command.toString());
    try {
      exec.execute(command, new DefaultExecuteResultHandler());
      logcatWatchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
      exec.setWatchdog(logcatWatchdog);
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  protected String getProp(String key) {
    CommandLine command = adbCommand("shell", "getprop", key);
    String prop = executeCommandQuietly(command);

    return prop == null ? "" : prop.replace("\r", "").replace("\n", "");
  }

  protected static String extractValue(String regex, String output) {
    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(output);
    if (matcher.find()) {
      return matcher.group(1);
    }

    return "";
  }

  public boolean screenSizeMatches(String requestedScreenSize) {
    // if screen size is not requested, just ignore it
    if (requestedScreenSize == null || requestedScreenSize.isEmpty()) {
      return true;
    }

    Pattern dimensionPattern = Pattern.compile("([0-9]+)x([0-9]+)");
    Matcher dimensionMatcher = dimensionPattern.matcher(requestedScreenSize);
    if (dimensionMatcher.matches()) {
      int width = Integer.parseInt(dimensionMatcher.group(1));
      int height = Integer.parseInt(dimensionMatcher.group(2));
      return getScreenSize().equals(new Dimension(width, height));
    } else {
      return false;
    }
  }

  public String runAdbCommand(String parameter) {
    if (parameter == null || parameter.isEmpty()) {
      return null;
    }
    log.fine("running command: adb " + parameter);
    CommandLine command = adbCommand();

    String[] params = parameter.split(" ");
    for (String param : params) {
      command.addArgument(param, false);
    }

    String commandOutput = executeCommandQuietly(command);
    return commandOutput.trim();
  }

  public byte[] takeScreenshot() throws AndroidDeviceException {
    if (device == null) {
      throw new AndroidDeviceException("Device not accessible via ddmlib.");
    }
    RawImage rawImage;
    try {
      rawImage = device.getScreenshot();
    } catch (IOException ioe) {
      throw new AndroidDeviceException("Unable to get frame buffer: " + ioe.getMessage());
    } catch (TimeoutException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new AndroidDeviceException(e.getMessage());
    } catch (AdbCommandRejectedException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new AndroidDeviceException(e.getMessage());
    }

    // device/adb not available?
    if (rawImage == null) return null;

    BufferedImage image =
        new BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_3BYTE_BGR);

    int index = 0;
    int IndexInc = rawImage.bpp >> 3;
    for (int y = 0; y < rawImage.height; y++) {
      for (int x = 0; x < rawImage.width; x++) {
        image.setRGB(x, y, rawImage.getARGB(index));
        index += IndexInc;
      }
    }
    return toByteArray(image);
  }

  protected byte[] toByteArray(BufferedImage image) throws AndroidDeviceException {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    try {
      if (!ImageIO.write(image, "png", stream)) {
        throw new IOException("Failed to find png writer");
      }
    } catch (IOException e) {
      log.log(Level.SEVERE, "Cannot take screenshot", e);
      throw new AndroidDeviceException(e.getMessage());
    }
    byte[] raw = null;
    try {
      stream.flush();
      raw = stream.toByteArray();
      stream.close();
    } catch (IOException e) {
      throw new RuntimeException("I/O Error while capturing screenshot: " + e.getMessage());
    } finally {
      try {
        stream.close();
      } catch (IOException ioe) {
        // ignore
      }
    }
    return raw;
  }

  /**
   * Use adb to send a keyevent to the device.
   *
   * Full list of keys available here:
   * http://developer.android.com/reference/android/view/KeyEvent.html
   *
   * @param value - Key to be sent to 'adb shell input keyevent'
   */
  public void inputKeyevent(int value) {
    executeCommandQuietly(adbCommand("shell", "input", "keyevent", "" + value));
    // need to wait a beat for the UI to respond
    sleep(500);
  }

  public void invokeActivity(String activity) {
    executeCommandQuietly(adbCommand("shell", "am", "start", "-a", activity));
    // need to wait a beat for the UI to respond
    sleep(500);
  }

  public void restartADB() {
    executeCommandQuietly(adbCommand("kill-server"));
    sleep(500);
    // make sure it's backup again
    executeCommandQuietly(adbCommand("devices"));
  }
  
  private CommandLine adbCommand() {
    CommandLine command = new CommandLine(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.addArgument("-s", false);
      command.addArgument(serial, false);
    }
    return command;
  }

  private CommandLine adbCommand(String... args) {
    CommandLine command = adbCommand();
    for (String arg : args) {
      command.addArgument(arg, false);
    }
    return command;
  }

  public String getExternalStoragePath() {
    return runAdbCommand("shell echo $EXTERNAL_STORAGE");
  }

  /** {@inheritdoc} */
  public String getCrashLog() {
    String crashLogFileName = ExternalStorageFile.APP_CRASH_LOG.toString();

    // The "test" utility doesn't exist on all devices so we'll check the output of ls.
    String crashLogDirPath = getExternalStoragePath();
    if (!crashLogDirPath.endsWith("/")) {
      crashLogDirPath += "/";  // Make sure it ends with '/' so we're listing directory contents.
    }
    String directoryList = executeCommandQuietly(adbCommand("shell", "ls", crashLogDirPath));
    if (directoryList.contains(crashLogFileName)) {
      return executeCommandQuietly(adbCommand("shell", "cat", crashLogDirPath + crashLogFileName));
    }

    return "";
  }

  /** {@inheritDoc} */
  public String listRunningThirdPartyProcesses() {
    String psOutput = runAdbCommand("shell ps");
    StringBuilder sb = new StringBuilder();
    boolean isFirstHeaderLine = true;
    for (String line: Splitter.on("\n").split(psOutput)) {
      boolean isThirdPartyProcess = line.contains(".") && !line.contains("com.android");
      if (isFirstHeaderLine || isThirdPartyProcess) {
        sb.append(line + "\n");
      }
      isFirstHeaderLine = false;
    }
    return sb.toString();
  }

  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass() || device == null) return false;

    AbstractDevice that = (AbstractDevice) o;

    return device.equals(that.device);
  }

  @Override
  public int hashCode() {
    return device.hashCode();
  }

  public String getModel() {
    return model;
  }
  
  public String getAPITargetType() {
    return apiTargetType;
  }
}
