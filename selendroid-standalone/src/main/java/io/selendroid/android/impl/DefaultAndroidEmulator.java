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
import io.selendroid.device.DeviceTargetPlatform;
import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.io.ShellCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.internal.Lists;

public class DefaultAndroidEmulator extends AbstractDevice implements AndroidEmulator {
  private static final String EMULATOR_SERIAL_PREFIX = "emulator-";
  private static final Logger log = Logger.getLogger(DefaultAndroidEmulator.class.getName());
  public static final String ANDROID_EMULATOR_HARDWARE_CONFIG = "hardware-qemu.ini";
  public static final String FILE_LOCKING_SUFIX = ".lock";


  private String screenSize;
  private DeviceTargetPlatform targetPlatform;
  // TODO ddary just use this as default
  private Abi abi = Abi.X86;
  private String avdName;
  private File avdRootFolder;
  private Locale locale = null;

  public DefaultAndroidEmulator(String avdName, String abi, String screenSize, String target,
      File avdFilePath) {
    this.avdName = avdName;
    if (Abi.X86.name().equalsIgnoreCase(abi)) {
      this.abi = Abi.X86;
    } else {
      this.abi = Abi.ARM;
    }

    this.screenSize = screenSize;
    this.avdRootFolder = avdFilePath;
    this.targetPlatform = DeviceTargetPlatform.fromInt(target);
  }

  public File getAvdRootFolder() {
    return avdRootFolder;
  }

  public String getScreenSize() {
    return screenSize;
  }

  public DeviceTargetPlatform getTargetPlatform() {
    return targetPlatform;
  }

  public Abi getAbi() {
    return abi;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.selendroid.android.impl.AndroidEmulator#createEmulator()
   */
  @Override
  public String createEmulator() throws AndroidDeviceException {
    if (isAndroidApiInstalled() == false) {
      throw new SelendroidException("Android SDK target version '' not installed."
          + " Please update sdk with the missing version.");
    }
    String avdName = createAvdName();
    if (!isEmulatorAlreadyExistent()) {
      List<String> createEmulator = Lists.newArrayList();

      createEmulator.add("echo no |");
      createEmulator.add(AndroidSdk.android());
      createEmulator.add("create avd -n");
      createEmulator.add(avdName);
      createEmulator.add("-t android");
      createEmulator.add(String.valueOf(targetPlatform.getVersion()));
      createEmulator.add("--skin");
      createEmulator.add(getScreenSize());
      createEmulator.add("--abi");
      createEmulator.add(abi.name());
      createEmulator.add("--force");
      try {
        ShellCommand.exec(createEmulator);
      } catch (ShellCommandException e) {
        throw new AndroidDeviceException(e);
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
            + File.separator + getAvdName() + ".avd");
    return emulatorFolder.exists();
  }

  public String getAvdName() {
    return avdName;
  }

  private String createAvdName() {
    return "selendroid" + targetPlatform.getVersion() + "_" + getScreenSize() + "_" + getAbi();
  }

  private boolean isAndroidApiInstalled() {
    File emulatorFolder =
        new File(AndroidSdk.androidHome() + File.separator + "platforms" + File.separator
            + targetPlatform.getSdkFolderName());

    return emulatorFolder.exists();
  }

  public static List<AndroidEmulator> listAvailableAvds() throws AndroidDeviceException {
    List<AndroidEmulator> avds = Lists.newArrayList();
    List<String> cmd = new ArrayList<String>();
    cmd.add(AndroidSdk.android());
    cmd.add("list");
    cmd.add("avds");
    String output = null;
    try {
      output = ShellCommand.exec(cmd);
    } catch (ShellCommandException e) {
      throw new AndroidDeviceException(e);
    }
    String[] avdsOutput = StringUtils.splitByWholeSeparator(output, "---------");
    if (avdsOutput != null && avdsOutput.length > 0) {
      for (int i = 0; i < avdsOutput.length; i++) {
        if(avdsOutput[i].contains("Name:")==false){
          continue;
        }
        String element = avdsOutput[i];
        String avdName = extractValue("Name: (.*?)$", element);
        String abi = extractValue("ABI: (.*?)$", element);
        String screenSize = extractValue("Skin: (.*?)$", element);
        String target = extractValue("\\(API level (.*?)\\)", element);
        File avdFilePath = new File(extractValue("Path: (.*?)$", element));
        avds.add(new DefaultAndroidEmulator(avdName, abi, screenSize, target, avdFilePath));
      }
    }
    return avds;
  }



  @Override
  public boolean isEmulatorStarted() {
    File lockedEmulatorHardwareConfig =
        new File(avdRootFolder, ANDROID_EMULATOR_HARDWARE_CONFIG + FILE_LOCKING_SUFIX);
    return lockedEmulatorHardwareConfig.exists();
  }

  @Override
  public String toString() {
    return "AndroidEmulator [screenSize=" + screenSize + ", targetPlatform=" + targetPlatform
        + ", avdName=" + avdName + "]";
  }

  private void setSerial(int port) {
    serial = EMULATOR_SERIAL_PREFIX + port;
  }

  public Integer getPort() {
    if (isSerialConfigured()) {
      return Integer.parseInt(serial.replace(EMULATOR_SERIAL_PREFIX, ""));
    }
    return null;
  }

  @Override
  public void start(Locale locale, int emulatorPort, Map<String, Object> options)
      throws AndroidDeviceException {
    if (isEmulatorStarted()) {
      throw new SelendroidException("Error - Android emulator is already started " + this);
    }
    Long timeout = null;
    String display = null;
    if (options != null) {
      if (options.containsKey(TIMEOUT_OPTION)) {
        timeout = (Long) options.get(TIMEOUT_OPTION);
      }
      if (options.containsKey(DISPLAY_OPTION)) {
        display = (String) options.get(DISPLAY_OPTION);
      }
    }
    if (timeout == null) {
      timeout = 120000L;
    }
    log.info("Using timeout of '" + timeout / 1000 + "' seconds to start the emulator.");
    this.locale = locale;
    List<String> cmd = Lists.newArrayList();
    cmd.add(AndroidSdk.emulator());
    cmd.add("-avd");
    cmd.add(avdName);
    cmd.add("-ports");
    cmd.add(emulatorPort + "," + (emulatorPort + 1));
    if (locale != null) {
      cmd.add("-prop");
      cmd.add("persist.sys.language=" + locale.getLanguage());
      cmd.add("-prop");
      cmd.add("persist.sys.country=" + locale.getCountry());
    }
    long start = System.currentTimeMillis();
    long timemoutEnd = start + timeout;
    try {
      ShellCommand.execAsync(display, cmd);
    } catch (ShellCommandException e) {
      throw new SelendroidException("unable to start the emulator: " + this);
    }
    setSerial(emulatorPort);
    while (isDeviceReady() == false) {
      if (timemoutEnd >= System.currentTimeMillis()) {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {}
      } else {
        throw new AndroidDeviceException("The emulator with avd '" + getAvdName()
            + "' was not started after " + (System.currentTimeMillis() - start) / 1000
            + " seconds.");
      }
    }

    log.info("Emulator start took: " + (System.currentTimeMillis() - start) / 1000 + " seconds");
    log.info("Please have in mind, starting an emulator takes usually about 45 seconds.");
    unlockEmulatorScreen();
  }

  private void unlockEmulatorScreen() throws AndroidDeviceException {
    List<String> event82 = new ArrayList<String>();
    event82.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      event82.add("-s");
      event82.add(serial);
    }
    event82.add("shell");
    event82.add("input");
    event82.add("keyevent");
    event82.add("82");

    try {
      ShellCommand.exec(event82);
    } catch (ShellCommandException e) {
      throw new AndroidDeviceException(e);
    }

    List<String> event4 = new ArrayList<String>();
    event4.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      event4.add("-s");
      event4.add(serial);
    }
    event4.add("shell");
    event4.add("input");
    event82.add("keyevent");
    event4.add("4");

    try {
      ShellCommand.exec(event4);
    } catch (ShellCommandException e) {
      throw new AndroidDeviceException(e);
    }
  }

  @Override
  public void stop() throws AndroidDeviceException {
    List<String> command = new ArrayList<String>();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.add("emu");
    command.add("kill");

    try {
      ShellCommand.exec(command);
    } catch (ShellCommandException e) {
      throw new AndroidDeviceException(e);
    }
  }

  @Override
  public Locale getLocale() {
    return locale;
  }
}
