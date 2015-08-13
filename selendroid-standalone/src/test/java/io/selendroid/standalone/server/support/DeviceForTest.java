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
package io.selendroid.standalone.server.support;

import com.android.ddmlib.IDevice;

import io.netty.handler.codec.http.HttpMethod;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.common.device.DeviceTargetPlatform;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.impl.DefaultAndroidEmulator;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.exceptions.AndroidSdkException;
import io.selendroid.standalone.server.util.HttpClientUtil;

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.logging.LogEntry;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeviceForTest extends DefaultAndroidEmulator {
  @Override
  public String runAdbCommand(String parameter) {
    return "";
  }

  @Override
  public Locale getLocale() {
    return null;
  }

  public boolean deviceReady = false;
  public SelendroidDeviceServerStub selendroidDeviceServerStub = null;
  private final Dimension screenSize;
  private final DeviceTargetPlatform platform;
  public TestSessionListener testSessionListener = null;

  public DeviceForTest(DeviceTargetPlatform platform) {
    screenSize = new Dimension(320, 480);
    this.platform = platform;
  }

  public DeviceForTest(DeviceTargetPlatform platform, Dimension screenSize) {
    this.screenSize = screenSize;
    this.platform = platform;
  }

  @Override
  public boolean isDeviceReady() {
    return deviceReady;
  }

  @Override
  public void install(AndroidApp app) throws AndroidSdkException {
    // do nothing
  }

  @Override
  public void uninstall(AndroidApp app) throws AndroidSdkException {
    // do nothing
  }

  @Override
  public void clearUserData(AndroidApp app) throws AndroidSdkException {
    if (selendroidDeviceServerStub != null) {
      selendroidDeviceServerStub.stop();
    }
  }

  public void startSelendroid(AndroidApp aut, int port, SelendroidCapabilities caps, String hostname) throws AndroidSdkException {
    try {
      selendroidDeviceServerStub = new SelendroidDeviceServerStub(port, testSessionListener);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isSelendroidRunning() {
    if (selendroidDeviceServerStub == null) {
      return false;
    }
    int status;

    try {
      HttpResponse r =
          HttpClientUtil.executeRequest("http://localhost:" + selendroidDeviceServerStub.getPort()
              + "/wd/hub/status", HttpMethod.GET);
      JSONObject response = HttpClientUtil.parseJsonResponse(r);
      status = response.getInt("status");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return 0 == status ? true : false;
  }

  @Override
  public int getSelendroidsPort() {
    if (selendroidDeviceServerStub != null) {
      return selendroidDeviceServerStub.getPort();
    }
    return 0;
  }

  @Override
  public boolean isEmulatorAlreadyExistent() {
    return true;
  }

  @Override
  public boolean isEmulatorStarted() {
    return false;
  }

  @Override
  public String getAvdName() {
    return "emulatorStub";
  }

  @Override
  public File getAvdRootFolder() {
    return null;
  }

  @Override
  public Dimension getScreenSize() {
    return screenSize;
  }

  @Override
  public DeviceTargetPlatform getTargetPlatform() {
    return platform;
  }

  @Override
  public void start(Locale locale, int number, Map<String, Object> timeout) {
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    deviceReady = true;
  }

  @Override
  public void stop() throws AndroidDeviceException {
    deviceReady = false;
    if (selendroidDeviceServerStub != null) {
      selendroidDeviceServerStub.stop();
      selendroidDeviceServerStub = null;
    }
  }

  @Override
  public void kill(AndroidApp app) throws AndroidDeviceException, AndroidSdkException {}

  public boolean screenSizeMatches(String requestedScreenSize) {
    // if screen size is not requested, just ignore it
    if (requestedScreenSize == null || requestedScreenSize.isEmpty()) {
      return true;
    }

    return getScreenSize().equals(requestedScreenSize);
  }

  @Override
  public Integer getPort() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isInstalled(AndroidApp app) throws AndroidSdkException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public List<LogEntry> getLogs() {
    return null;
  }

  @Override
  public byte[] takeScreenshot() throws AndroidDeviceException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setIDevice(IDevice iDevice) {
    // TODO Auto-generated method stub

  }

  @Override
  public String getSerial() {
    return "emulator-5554";
  }

  @Override
  public void setVerbose() {

  }
}
