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
package io.selendroid.android.impl;

import io.selendroid.android.Abi;
import io.selendroid.android.AndroidEmulator;
import io.selendroid.android.AndroidSdk;
import io.selendroid.device.DeviceTargetPlatform;
import io.selendroid.exceptions.AndroidDeviceException;
import io.selendroid.exceptions.AndroidSdkException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.io.ShellCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.android.ddmlib.IDevice;
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

  protected DefaultAndroidEmulator(){}
  
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
        if (avdsOutput[i].contains("Name:") == false) {
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

    if (display != null) {
      log.info("Using display " + display + " for running the emulator");
    }
    if (timeout == null) {
      timeout = 120000L;
    }
    log.info("Using timeout of '" + timeout / 1000 + "' seconds to start the emulator.");
    this.locale = locale;
    List<String> cmd = Lists.newArrayList();
    cmd.add(AndroidSdk.emulator());
    cmd.add("-no-snapshot-save");
    cmd.add("-avd");
    cmd.add(avdName);
    cmd.add("-port");
    cmd.add(String.valueOf(emulatorPort));
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
    Boolean adbKillServerAttempted = false;
    while (isDeviceReady() == false) {

      if (!adbKillServerAttempted && System.currentTimeMillis() - start > 10000) {
        CommandLine adbDevicesCmd = new CommandLine(AndroidSdk.adb());
        adbDevicesCmd.addArgument("devices", false);

        String devices = "";
        try {
          devices = ShellCommand.exec(adbDevicesCmd, 20000);
        } catch (ShellCommandException e) {
          // pass
        }
        if (!devices.contains(String.valueOf(emulatorPort))) {
          CommandLine resetAdb = new CommandLine(AndroidSdk.adb());
          resetAdb.addArgument("kill-server", false);

          try {
            ShellCommand.exec(resetAdb, 20000);
          } catch (ShellCommandException e) {
            throw new SelendroidException("unable to kill the adb server");
          }
        }
        adbKillServerAttempted = true;
      }
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

    waitForLauncherToComplete();

    // we observed that emulators can sometimes not be 'fully loaded'
    // if we click on the All Apps button and wait for it to load it is more likely to be in a
    // usable state.
    allAppsGridView();

    waitForLauncherToComplete();
  }

  private void unlockEmulatorScreen() throws AndroidDeviceException {
    CommandLine event82 = new CommandLine(AndroidSdk.adb());

    if (isSerialConfigured()) {
      event82.addArgument("-s", false);
      event82.addArgument(serial, false);
    }
    event82.addArgument("shell", false);
    event82.addArgument("input", false);
    event82.addArgument("keyevent", false);
    event82.addArgument("82", false);

    try {
      ShellCommand.exec(event82, 20000);
    } catch (ShellCommandException e) {
      throw new AndroidDeviceException(e);
    }

    CommandLine event4 = new CommandLine(AndroidSdk.adb());

    if (isSerialConfigured()) {
      event4.addArgument("-s", false);
      event4.addArgument(serial, false);
    }
    event4.addArgument("shell", false);
    event4.addArgument("input", false);
    event4.addArgument("keyevent", false);
    event4.addArgument("4", false);
    try {
      ShellCommand.exec(event4, 20000);
    } catch (ShellCommandException e) {
      throw new AndroidDeviceException(e);
    }
  }

  private void waitForLauncherToComplete() throws AndroidDeviceException {
    waitForLauncherToComplete(true);
  }

  private void waitForLauncherToComplete(Boolean delay) throws AndroidDeviceException {
    CommandLine event = new CommandLine(AndroidSdk.adb());

    if (isSerialConfigured()) {
      event.addArgument("-s", false);
      event.addArgument(serial, false);
    }
    event.addArgument("shell", false);
    event.addArgument("ps", false);
    String homeScreenLaunched = null;
    try {
      homeScreenLaunched = ShellCommand.exec(event, 20000);
    } catch (ShellCommandException e) {
      throw new AndroidDeviceException(e);
    }
    if (homeScreenLaunched != null && homeScreenLaunched.contains("S com.android.launcher")) {
      if (!delay) return;
    } else {
      // it's still running, sleep for a bit
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      waitForLauncherToComplete(true);
    }

    // it's done right? ... well, maybe... check again after waiting a second
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    waitForLauncherToComplete(false);

  }

  private void allAppsGridView() throws AndroidDeviceException {
    String[] dimensions = screenSize.split("x");
    int x = Integer.parseInt(dimensions[0]);
    int y = Integer.parseInt(dimensions[1]);
    if (x > y) {
      y = y / 2;
      x = x - 30;
    } else {
      x = x / 2;
      y = y - 30;
    }

    List<String> event1 = new ArrayList<String>();
    List<String> coordinates = new ArrayList<String>();
    coordinates.add("3 0 " + x);
    coordinates.add("3 1 " + y);
    coordinates.add("1 330 1");
    coordinates.add("0 0 0");
    coordinates.add("1 330 0");
    coordinates.add("0 0 0");
    // TODO lukeis: review if this can be refactored to use the CommandLine class.
    event1.add(AndroidSdk.adb().getAbsolutePath());
    if (isSerialConfigured()) {
      event1.add("-s");
      event1.add(serial);
    }
    event1.add("shell");
    event1.add("sendevent");
    event1.add("dev/input/event0");
    for (String coordinate : coordinates) {
      event1.add(coordinate);
      try {
        ShellCommand.exec(event1);
      } catch (ShellCommandException e) {
        throw new AndroidDeviceException(e);
      }
      event1.remove(coordinate);
    }

    try {
      Thread.sleep(750);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() throws AndroidDeviceException {
    this.device = null;
    CommandLine command = new CommandLine(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.addArgument("-s", false);
      command.addArgument(serial, false);
    }
    command.addArgument("emu", false);
    command.addArgument("kill", false);

    try {
      ShellCommand.exec(command, 20000);
      Boolean killed = false;
      while (isEmulatorStarted()) {
        log.info("emulator still running, sleeping 0.5, waiting for it to release the lock");
        try {
          Thread.sleep(500);
        } catch (InterruptedException ie) {
          throw new RuntimeException(ie);
        }
        if (!killed) {
          try {
            ShellCommand.exec(command, 20000);
          } catch (ShellCommandException sce) {
            killed = true;
          }
        }
      }
    } catch (ShellCommandException e) {
      throw new AndroidDeviceException(e);
    }
  }

  @Override
  public void kill(InstalledAndroidApp aut) throws AndroidDeviceException, AndroidSdkException {
    CommandLine command = new CommandLine(AndroidSdk.adb());

    if (isSerialConfigured()) {
      command.addArgument("-s", false);
      command.addArgument(serial, false);
    }
    command.addArgument("shell", false);
    command.addArgument("am", false);
    command.addArgument("force-stop", false);
    command.addArgument(aut.getBasePackage(), false);
    try {
      ShellCommand.exec(command, 20000);
    } catch (ShellCommandException e) {
      e.printStackTrace();
    }

    CommandLine clearCommand = new CommandLine(AndroidSdk.adb());
    if (isSerialConfigured()) {
      clearCommand.addArgument("-s", false);
      clearCommand.addArgument(serial, false);
    }
    clearCommand.addArgument("shell", false);
    clearCommand.addArgument("pm", false);
    clearCommand.addArgument("clear", false);
    clearCommand.addArgument(aut.getBasePackage(), false);
    try {
      ShellCommand.exec(command, 20000);
    } catch (ShellCommandException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public void setIDevice(IDevice iDevice) {
    super.device = iDevice;
  }

  public String getSerial() {
    return serial;
  }
}
