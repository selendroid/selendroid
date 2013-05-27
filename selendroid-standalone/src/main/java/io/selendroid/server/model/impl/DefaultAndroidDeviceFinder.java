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
package io.selendroid.server.model.impl;

import io.selendroid.android.AndroidDevice;
import io.selendroid.android.AndroidSdk;
import io.selendroid.android.impl.DefaultAndroidDevice;
import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.io.ShellCommand;
import io.selendroid.server.model.DeviceFinder;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class DefaultAndroidDeviceFinder implements DeviceFinder {
  public static final String EMULATOR_PREFIX = "emulator-";

  @Override
  public List<AndroidDevice> findConnectedDevices() throws AndroidDeviceException {
    List<AndroidDevice> foundDevices = new ArrayList<AndroidDevice>();

    List<String> lines = listDevices();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i).trim().replaceAll(" +", " ");
      if (line.startsWith(EMULATOR_PREFIX) || 0 == i || line.isEmpty()) {
        continue;
      }
      String[] lineArgs = line.split(" ");
      String serial = lineArgs[0];

      AndroidDevice device = new DefaultAndroidDevice(serial);
      foundDevices.add(device);
    }
    return foundDevices;
  }

  /* package */List<String> listDevices() throws AndroidDeviceException {
    String output = null;
    try {
      output = ShellCommand.exec(Arrays.asList(new String[] {AndroidSdk.adb(), "devices", "-l"}));
    } catch (ShellCommandException e) {
      throw new AndroidDeviceException("Error occured while searching for devices.", e);
    }
    List<String> lines = null;
    try {
      lines = IOUtils.readLines(new StringReader(output));
    } catch (IOException e) {
      throw new AndroidDeviceException("Error occured while reading devices list.", e);
    }
    return lines;
  }

}
