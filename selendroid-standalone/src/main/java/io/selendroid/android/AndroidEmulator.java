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

import java.io.File;

public class AndroidEmulator {
  private String screenSize;
  private String api;
  private Abi abi;

  public AndroidEmulator(String screenSize, String api, Abi abi) {
    super();
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
    return new AndroidEmulator("720x1280", "16", Abi.X86);
  }

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

  public boolean isEmulatorAlreadyExistent() {
    File emulatorFolder =
        new File("~" + File.separator + ".android" + File.separator + "avd" + File.separator
            + getAvdName());
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
