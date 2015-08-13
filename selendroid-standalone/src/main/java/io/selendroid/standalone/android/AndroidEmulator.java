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

import io.selendroid.common.device.DeviceTargetPlatform;
import io.selendroid.standalone.exceptions.AndroidDeviceException;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import com.android.ddmlib.IDevice;

import org.openqa.selenium.Dimension;

public interface AndroidEmulator {
  public static final String TIMEOUT_OPTION = "TIMEOUT";
  public static final String DISPLAY_OPTION = "DISPLAY";
  public static final String EMULATOR_OPTIONS = "OPTIONS";

  public boolean isEmulatorAlreadyExistent() throws AndroidDeviceException;

  public boolean isEmulatorStarted() throws AndroidDeviceException;

  public String getAvdName();

  public File getAvdRootFolder();

  public Dimension getScreenSize();

  public DeviceTargetPlatform getTargetPlatform();

  public void start(Locale locale, int port, Map<String, Object> options)
      throws AndroidDeviceException;

  public void stop() throws AndroidDeviceException;

  public Integer getPort();

  public void setIDevice(IDevice iDevice);

  public String getSerial();

  public void setSerial(String serial);

  public void setSerial(int port);

  public void unlockScreen() throws AndroidDeviceException;

  public void setWasStartedBySelendroid(boolean wasStartedBySelendroid);

  public String getModel();

  public String getAPITargetType();
}
