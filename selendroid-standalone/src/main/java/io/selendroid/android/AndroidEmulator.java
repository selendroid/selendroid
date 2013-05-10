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
package io.selendroid.android;

import io.selendroid.exceptions.AndroidDeviceException;

import java.io.File;
import java.util.Locale;

import org.openqa.selendroid.device.DeviceTargetPlatform;

public interface AndroidEmulator {

  public String createEmulator() throws AndroidDeviceException;

  public boolean isEmulatorAlreadyExistent() throws AndroidDeviceException;

  public boolean isEmulatorStarted() throws AndroidDeviceException;

  public Abi getAbi();

  public String getAvdName();

  public File getAvdRootFolder();

  public String getScreenSize();

  public DeviceTargetPlatform getTargetPlatform();
  
  public void startEmulator(Locale locale);
}
