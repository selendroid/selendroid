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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import io.selendroid.exceptions.SelendroidException;

import com.beust.jcommander.internal.Lists;

public class DefaultAndroidDevice implements AndroidDevice {
  public static final String WD_STATUS_ENDPOINT = "http://127.0.0.1:8080/wd/hub/status";
  protected String serial = null;
  protected Integer port = null;

  public DefaultAndroidDevice(String serial) {
    this.serial = serial;
  }

  protected DefaultAndroidDevice() {}

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

  private String executeCommand(List<String> command) {
    try {
      return ShellCommand.exec(command);
    } catch (ShellCommandException e) {
      e.printStackTrace();
      return null;
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
    command.add("org.openqa.selendroid/.ServerInstrumentation");
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
    HttpRequestBase request = new HttpGet(WD_STATUS_ENDPOINT.replace("8080", String.valueOf(port)));
    HttpResponse response = null;
    try {
      response = httpClient.execute(request);
    } catch (ClientProtocolException e) {
      throw new SelendroidException(e);
    } catch (IOException e) {
      throw new SelendroidException(e);
    }
    int statusCode = response.getStatusLine().getStatusCode();
    String responseValue;
    try {
      responseValue = IOUtils.toString(response.getEntity().getContent());
    } catch (IllegalStateException e) {
      throw new SelendroidException(e);
    } catch (IOException e) {
      throw new SelendroidException(e);
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

  public Integer getDeviceTargetPlatform() {
    List<String> command = Lists.newArrayList();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.add("shell");
    command.add("getprop");
    command.add("ro.build.version.sdk");

    String output = executeCommand(command);
    return Integer.parseInt(output);
  }

  protected static String extractValue(String regex, String output) {
    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(output);
    if (matcher.find()) {
      return matcher.group(1);
    }

    return null;
  }

  @Override
  public String getScreenSize() {
    throw new RuntimeException("NOT YET IMPLEMENTED");
  }

  public boolean screenSizeMatches(String requestedScreenSize) {
    // if screen size is not requested, just ignore it
    if (requestedScreenSize == null || requestedScreenSize.isEmpty()) {
      return true;
    }

    return getScreenSize().equals(requestedScreenSize);
  }
}
