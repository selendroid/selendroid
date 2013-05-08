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

import io.selendroid.android.Abi;
import io.selendroid.android.AndroidEmulator;
import io.selendroid.android.AndroidSdk;
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.io.ShellCommand;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openqa.selendroid.exceptions.SelendroidException;

import com.beust.jcommander.internal.Lists;

public class DefaultAndroidEmulator implements AndroidEmulator {
  private String screenSize;
  private String api;
  private Abi abi;

  public DefaultAndroidEmulator(String screenSize, String api, Abi abi) {
    this.screenSize = screenSize;
    this.api = api;
    this.abi = abi;
  }

  public String getScreenSize() {
    return screenSize;
  }

  public String getApi() {
    return api;
  }

  public Abi getAbi() {
    return abi;
  }

  public static AndroidEmulator apiLevel16() {
    return new DefaultAndroidEmulator("720x1280", "16", Abi.X86);
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.selendroid.android.impl.AndroidEmulator#createEmulator()
   */
  @Override
  public String createEmulator() {
    String avdName = getAvdName();
    if (!isEmulatorAlreadyExistent()) {
      List<String> createEmulator = Lists.newArrayList();

      createEmulator.add("echo no |");
      createEmulator.add(AndroidSdk.android());
      createEmulator.add("create avd -n");
      createEmulator.add(avdName);
      createEmulator.add("-t android");
      createEmulator.add(getApi());
      createEmulator.add("--skin");
      createEmulator.add(getScreenSize());
      createEmulator.add("--abi");
      createEmulator.add(getApi());
      createEmulator.add("--force");
      try {
        ShellCommand.exec(createEmulator);
      } catch (ShellCommandException e) {
        throw new SelendroidException(e);
      }
    }
    return avdName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.selendroid.android.impl.AndroidEmulator#isEmulatorAlreadyExistent()
   */
  @Override
  public boolean isEmulatorAlreadyExistent() {
    File emulatorFolder =
        new File(FileUtils.getUserDirectory(), File.separator + ".android" + File.separator + "avd"
            + File.separator + getAvdName());
    return emulatorFolder.exists();
  }

  private String getAvdName() {
    return "selendroid" + getApi() + "_" + getScreenSize() + "_" + getApi();
  }

  private boolean isAndroidApiInstalled() {
    File emulatorFolder =
        new File(AndroidSdk.androidHome() + File.separator + "platforms" + File.separator
            + "android-" + getApi());

    return emulatorFolder.exists();
  }
}
