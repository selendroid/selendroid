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
package io.selendroid.android.impl;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;
import com.beust.jcommander.internal.Lists;
import com.google.common.base.Throwables;
import com.google.common.collect.ObjectArrays;
import io.selendroid.SelendroidCapabilities;
import io.selendroid.android.AndroidApp;
import io.selendroid.android.AndroidDevice;
import io.selendroid.android.AndroidSdk;
import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.io.ShellCommand;
import io.selendroid.server.model.ExternalStorageFile;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openqa.selenium.logging.LogEntry;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractDevice implements AndroidDevice {
  private static final Logger log = Logger.getLogger(AbstractDevice.class.getName());
  public static final String WD_STATUS_ENDPOINT = "http://localhost:8080/wd/hub/status";
  protected String serial = null;
  protected Integer port = null;
  protected IDevice device;
  private ByteArrayOutputStream logoutput;
  private ExecuteWatchdog logcatWatchdog;
  private static final Integer COMMAND_TIMEOUT = 20000;
  private boolean loggingEnabled = true;

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
    return serial != null && serial.isEmpty() == false;
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
    } catch (ShellCommandException e) {}
    if (bootAnimDisplayed != null && bootAnimDisplayed.contains("stopped")) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isInstalled(String appBasePackage) throws AndroidSdkException {
    CommandLine command = adbCommand("shell", "pm", "list", "packages");

    command.addArgument(appBasePackage, false);
    String result = null;
    try {
      result = ShellCommand.exec(command);
    } catch (ShellCommandException e) {}
    if (result != null && result.contains("package:" + appBasePackage)) {
      return true;
    }

    return false;
  }

  @Override
  public boolean isInstalled(AndroidApp app) throws AndroidSdkException {
    return isInstalled(app.getBasePackage());
  }

  @Override
  public Boolean install(AndroidApp app) {
    // Reinstall if already installed, Install otherwise
    CommandLine command = adbCommand("install", "-r", app.getAbsolutePath());

    String out = executeCommandQuietly(command, COMMAND_TIMEOUT * 6);
    try {
      // give it a second to recover from the install
      Thread.sleep(1000);
    } catch (InterruptedException ie) {
      throw new RuntimeException(ie);
    }
    return out.contains("Success");
  }

  public boolean start(AndroidApp app) throws AndroidSdkException {
    if (isInstalled(app) == false) {
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
  public void startSelendroid(AndroidApp aut, int port, SelendroidCapabilities capabilities) throws AndroidSdkException {
    this.port = port;

    String[] args =  {
        "-e", "main_activity", aut.getMainActivity(),
        "-e", "server_port", Integer.toString(port),
        "io.selendroid." + aut.getBasePackage() + "/io.selendroid.ServerInstrumentation"};
    CommandLine command = adbCommand(
        ObjectArrays.concat(new String[]{"shell", "am", "instrument"}, args, String.class));
    if (capabilities.getSelendroidExtensions() != null) {
      command.addArguments(new String[]{"-e", "load_extensions", "true"});
      if (capabilities.getBootstrapClassNames() != null) {
        command.addArguments(new String[]{"-e", "bootstrap", capabilities.getBootstrapClassNames()});
      }
    }

    String result = executeCommandQuietly(command);
    if (result.contains("FAILED")) {
      String detailedResult;
      try {
        // Try again, waiting for instrumentation to finish. This way we'll get more error output.
        CommandLine getErrorDetailCommand = adbCommand(
            ObjectArrays.concat(new String[]{"shell", "am", "instrument", "-w"}, args, String.class));
        detailedResult = executeCommandQuietly(getErrorDetailCommand);
      } catch (Exception e) {
        detailedResult = "Could not run get detailed result: " +
            e.getMessage() + "\n" + Throwables.getStackTraceAsString(e);
      }
      throw new SelendroidException("Error occurred while starting selendroid-server on the device",
          new Throwable(result + "\nDetails:\n" + detailedResult));
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
    HttpClient httpClient = new DefaultHttpClient();
    String url = WD_STATUS_ENDPOINT.replace("8080", String.valueOf(port));
    log.info("using url: " + url);
    HttpRequestBase request = new HttpGet(url);
    HttpResponse response = null;
    try {
      response = httpClient.execute(request);
    } catch (Exception e) {
      log.severe("Error getting status: " + e);
      return false;
    }
    int statusCode = response.getStatusLine().getStatusCode();
    log.info("got response status code: " + statusCode);
    String responseValue;
    try {
      responseValue = IOUtils.toString(response.getEntity().getContent());
      log.info("got response value: " + responseValue);
    } catch (Exception e) {
      log.severe("Error getting status: " + e);
      return false;
    }

    if (response != null && 200 == statusCode && responseValue.contains("selendroid")) {
      return true;
    }
    return false;
  }

  @Override
  public int getSelendroidsPort() {
    return port;
  }

  @Override
  public List<LogEntry> getLogs() {
    List<LogEntry> logs = Lists.newArrayList();
    String result = logoutput != null ? logoutput.toString() : "";
    String[] lines = result.split("\\r?\\n");
    int num_lines = lines.length;
    log.fine("getting logcat");
    for (int x = 0; x < num_lines; x++) {
      Level l;
      if (lines[x].startsWith("I")) {
        l = Level.INFO;
      } else if (lines[x].startsWith("W")) {
        l = Level.WARNING;
      } else if (lines[x].startsWith("S")) {
        l = Level.SEVERE;
      } else {
        l = Level.FINE;
      }
      logs.add(new LogEntry(l, System.currentTimeMillis(), lines[x]));
      log.fine(lines[x]);
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
      e.printStackTrace();
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

    return getScreenSize().equals(requestedScreenSize);
  }

  public String runAdbCommand(String parameter) {
    if (parameter == null || parameter.isEmpty()) {
      return null;
    }
    System.out.println("running command: adb " + parameter);
    CommandLine command = adbCommand();

    String[] params = parameter.split(" ");
    for (int i = 0; i < params.length; i++) {
      command.addArgument(params[i], false);
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
      e.printStackTrace();
      throw new AndroidDeviceException(e.getMessage());
    } catch (AdbCommandRejectedException e) {
      e.printStackTrace();
      throw new AndroidDeviceException(e.getMessage());
    }

    // device/adb not available?
    if (rawImage == null) return null;

    BufferedImage image =
        new BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_ARGB);

    int index = 0;
    int IndexInc = rawImage.bpp >> 3;
    for (int y = 0; y < rawImage.height; y++) {
      for (int x = 0; x < rawImage.width; x++) {
        int value = rawImage.getARGB(index);
        index += IndexInc;
        image.setRGB(x, y, value);
      }
    }
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    try {
      if (!ImageIO.write(image, "png", stream)) {
        throw new IOException("Failed to find png writer");
      }
    } catch (IOException e) {
      e.printStackTrace();
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
      Closeable closeable = (Closeable) stream;
      try {
        if (closeable != null) {
          closeable.close();
        }
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
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void invokeActivity(String activity) {
    executeCommandQuietly(adbCommand("shell", "am", "start", "-a", activity));
    // need to wait a beat for the UI to respond
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void restartADB() {
    executeCommandQuietly(adbCommand("kill-server"));
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
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

  /**
   * Get crash log from AUT
   * @return empty string if there is no crash log on the device, otherwise returns the stack trace
   * caused by the crash of the AUT
   */
  public String getCrashLog() {
    String crashLogFile = ExternalStorageFile.APP_CRASH_LOG.toString();
    String crashLogPath = new File(getExternalStoragePath(), crashLogFile).getAbsolutePath();
    CommandLine command = adbCommand("shell", "test", "-e", crashLogPath, "&&", "cat", crashLogPath);

    return executeCommandQuietly(command);
  }

}
