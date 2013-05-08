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

import java.io.File;

import org.apache.commons.io.FileUtils;

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
    if (!isEmulatorAlreadyExistent()) {
      StringBuffer createEmulator = new StringBuffer();
      createEmulator.append("echo no | ");
      createEmulator.append(AndroidSdk.android());
      createEmulator.append(" create avd -n ");
      createEmulator.append(getAvdName());
      createEmulator.append("-t android");
      createEmulator.append(getApi());
      createEmulator.append("--skin");
      createEmulator.append(getScreenSize());
      createEmulator.append("--abi");
      createEmulator.append(getApi());
      createEmulator.append("--force");
      return createEmulator.toString();
    }
    return null;
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
