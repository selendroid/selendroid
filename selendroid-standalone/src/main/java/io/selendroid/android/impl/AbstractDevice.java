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
package io.selendroid.android.impl;

import io.selendroid.android.AndroidApp;
import io.selendroid.android.AndroidDevice;
import io.selendroid.android.AndroidSdk;
import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.io.ShellCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import com.beust.jcommander.internal.Lists;
import org.openqa.selenium.logging.LogEntry;

public abstract class AbstractDevice implements AndroidDevice {
  private static final Logger log = Logger.getLogger(AbstractDevice.class.getName());
  public static final String WD_STATUS_ENDPOINT = "http://localhost:8080/wd/hub/status";
  protected String serial = null;
  protected Integer port = null;

  public AbstractDevice(String serial) {
    this.serial = serial;
  }

  protected AbstractDevice() {}

  protected boolean isSerialConfigured() {
    return serial != null && serial.isEmpty() == false;
  }

  @Override
  public boolean isDeviceReady() {
    List<String> command = new ArrayList<String>();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.add("shell");
    command.add("getprop init.svc.bootanim");
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
  public boolean isInstalled(AndroidApp app) throws AndroidSdkException {
    List<String> command = new ArrayList<String>();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.add("shell");
    command.add("pm");
    command.add("list");
    command.add("packages");
    String apkPackage = app.getBasePackage();
    command.add(apkPackage);
    String result = null;
    try {
      result = ShellCommand.exec(command);
    } catch (ShellCommandException e) {}
    if (result != null && result.contains(apkPackage)) {
      return true;
    }
    return false;
  }

  @Override
  public void install(AndroidApp app) {
    List<String> command = new ArrayList<String>();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.add("install");
    command.add("-r");
    command.add(app.getAbsolutePath());

    executeCommand(command);
  }

  protected String executeCommand(List<String> command) {
    try {
      return ShellCommand.exec(command);
    } catch (ShellCommandException e) {
      e.printStackTrace();
      return "";
    }
  }

  @Override
  public void uninstall(AndroidApp app) throws AndroidSdkException {
    List<String> command = new ArrayList<String>();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.add("uninstall");
    command.add(app.getBasePackage());

    executeCommand(command);
  }

  @Override
  public void clearUserData(AndroidApp app) throws AndroidSdkException {
    List<String> command = new ArrayList<String>();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.add("shell");
    command.add("pm");
    command.add("clear");
    command.add(app.getBasePackage());
    executeCommand(command);
  }

  @Override
  public void startSelendroid(AndroidApp aut, int port) throws AndroidSdkException {
    this.port = port;
    List<String> command = Lists.newArrayList();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.add("shell");
    command.add("am");
    command.add("instrument");
    command.add("-e");
    command.add("main_activity");
    command.add(aut.getMainActivity());
    command.add("io.selendroid/.ServerInstrumentation");
    executeCommand(command);

    forwardSelendroidPort(port);
  }

  private void forwardSelendroidPort(int port) {
    List<String> command = Lists.newArrayList();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.add("forward");
    command.add("tcp:" + port);
    command.add("tcp:8080");
    executeCommand(command);
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
    List<String> command = Lists.newArrayList();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.add("logcat");
    command.add("-d"); // Dumps the log to the screen and exits.
    String result = executeCommand(command);
    String[] lines = result.split("\\r?\\n");
    int num_lines = lines.length;
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
    }
    return logs;
  }

  protected String getProp(String key) {
    List<String> command = Lists.newArrayList();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.add("shell");
    command.add("getprop");
    command.add(key);
    String prop = executeCommand(command);

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

  public void runAdbCommand(String parameter) {
    if (parameter == null || parameter.isEmpty() == true) {
      return;
    }
    List<String> command = Lists.newArrayList();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.addAll(Arrays.asList(parameter.split(" ")));
    executeCommand(command);
  }

}
