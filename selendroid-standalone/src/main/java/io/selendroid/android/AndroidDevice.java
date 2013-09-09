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
package io.selendroid.android;

import io.selendroid.device.DeviceTargetPlatform;
import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.exceptions.AndroidSdkException;

import org.openqa.selenium.logging.LogEntry;

import java.util.List;
import java.util.Locale;

public interface AndroidDevice {
  public boolean isDeviceReady();

  public Boolean install(AndroidApp app);

  public boolean isInstalled(AndroidApp app) throws AndroidSdkException;

  public void uninstall(AndroidApp app) throws AndroidSdkException;

  public void clearUserData(AndroidApp app) throws AndroidSdkException;

  public void startSelendroid(AndroidApp aut, int port) throws AndroidSdkException;

  public boolean isSelendroidRunning();

  public int getSelendroidsPort();

  public String getScreenSize();

  public List<LogEntry> getLogs();

  public boolean screenSizeMatches(String requestedScreenSize);

  public Locale getLocale();

  public DeviceTargetPlatform getTargetPlatform();

  public void runAdbCommand(String parameter);
  
  public byte[] takeScreenshot()throws AndroidDeviceException;

  public void setVerbose();
}
