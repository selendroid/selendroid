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
package io.selendroid.standalone.android;

import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.common.device.DeviceTargetPlatform;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.exceptions.AndroidSdkException;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.logging.LogEntry;

import com.google.common.base.Predicates;

import java.util.List;
import java.util.Locale;

public interface AndroidDevice {
  public boolean isDeviceReady();

  public void install(AndroidApp app) throws AndroidSdkException;

  public boolean isInstalled(String appBasePackage) throws AndroidSdkException;
  
  public boolean isInstalled(AndroidApp app) throws AndroidSdkException;

  public void uninstall(AndroidApp app) throws AndroidSdkException;

  public boolean start(AndroidApp app) throws AndroidSdkException;

  public void forwardPort(int local, int remote);

  public void clearUserData(AndroidApp app) throws AndroidSdkException;

  public void startSelendroid(AndroidApp aut, int port, SelendroidCapabilities capabilities,
                              String hostname) throws AndroidSdkException;

  public boolean isSelendroidRunning();

  public int getSelendroidsPort();

  public void kill(AndroidApp aut) throws AndroidDeviceException, AndroidSdkException;

  public Dimension getScreenSize();

  public List<LogEntry> getLogs();

  public boolean isLoggingEnabled();

  public void setLoggingEnabled(boolean loggingEnabled);

  public boolean screenSizeMatches(String requestedScreenSize);

  public Locale getLocale();

  public DeviceTargetPlatform getTargetPlatform();

  public String runAdbCommand(String parameter);

  public byte[] takeScreenshot() throws AndroidDeviceException;

  public void setVerbose();

  public String getSerial();

  public String getHostname();

  public void inputKeyevent(int value);

  public void invokeActivity(String activity);

  public void restartADB();

  public String getExternalStoragePath();

  /**
   * Get crash log from AUT
   * @return empty string if there is no crash log on the device, otherwise returns the stack trace
   * caused by the crash of the AUT
   */
  public String getCrashLog();

  /**
   * Returns the output of running 'adb shell ps', filtering out system processes.
   */
  public String listRunningThirdPartyProcesses();

  public String getModel();

  public String getAPITargetType();

  public void unlockScreen() throws AndroidDeviceException;
}
