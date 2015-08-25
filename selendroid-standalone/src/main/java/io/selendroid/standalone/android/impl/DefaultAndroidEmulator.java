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
package io.selendroid.standalone.android.impl;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableMap;

import io.selendroid.common.device.DeviceTargetPlatform;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.standalone.android.AndroidEmulator;
import io.selendroid.standalone.android.AndroidSdk;
import io.selendroid.standalone.android.TelnetClient;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.exceptions.ShellCommandException;
import io.selendroid.standalone.io.ShellCommand;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultAndroidEmulator extends AbstractAndroidDeviceEmulator {

  private static final Logger log = Logger.getLogger(DefaultAndroidEmulator.class.getName());
  public static final String ANDROID_EMULATOR_HARDWARE_CONFIG = "hardware-qemu.ini";
  public static final String FILE_LOCKING_SUFIX = ".lock";
  private static final ImmutableMap<String, Dimension> SKIN_NAME_DIMENSIONS = new
      ImmutableMap.Builder<String, Dimension>()
      .put("QVGA", new Dimension(240, 320))
      .put("WQVGA400", new Dimension(240, 400))
      .put("WQVGA432", new Dimension(240, 432))
      .put("HVGA", new Dimension(320, 480))
      .put("WVGA800", new Dimension(480, 800))
      .put("WVGA854", new Dimension(480, 854))
      .put("WXGA", new Dimension(1280, 800))
      .put("WXGA720", new Dimension(1280, 720))
      .put("WXGA800", new Dimension(1280, 800))
      .build();

  private boolean wasStartedBySelendroid;

  protected DefaultAndroidEmulator() {
    this.wasStartedBySelendroid = Boolean.FALSE;
  }
  
  // this contructor is used only for test purposes in setting the capabilities information. Change to public if there
  // is ever a desire to construct one of these besides reading the avdOutput
  public DefaultAndroidEmulator(String avdName, String abi, Dimension screenSize, String target,
                                String model, File avdFilePath, String apiTargetType) {
    this.avdName = avdName;
    this.model = model;
    this.screenSize = screenSize;
    this.avdRootFolder = avdFilePath;
    this.targetPlatform = DeviceTargetPlatform.fromInt(target);
    this.wasStartedBySelendroid = !isEmulatorStarted();
    this.apiTargetType = apiTargetType;
  }

  // avdOutput is expected to look like the following
    /*Name: Android_TV
    Device: tv_720p (Google)
    Path: /Users/antnguyen/.android/avd/Android_TV.avd
    Target: Android 5.0.1 (API level 21)
    Tag/ABI: android-tv/armeabi-v7a
    Skin: tv_720p
    Sdcard: 100M
    Snapshot: no*/
  public DefaultAndroidEmulator(String avdOutput) {
    this.avdName = extractValue("Name: (.*?)$", avdOutput);
    this.screenSize = getScreenSizeFromSkin(extractValue("Skin: (.*?)$", avdOutput));
    this.targetPlatform = DeviceTargetPlatform.fromInt(extractValue("\\(API level (.*?)\\)", avdOutput));
    this.avdRootFolder = new File(extractValue("Path: (.*?)$", avdOutput));
    this.model = extractValue("Device: (.*?)$", avdOutput);
    extractAPITargetType(avdOutput);
  }

  private void extractAPITargetType(String avdOutput) {
    String target = extractValue("Target: (.*?)$", avdOutput);
    // chose to compare against both of these strings because currently some targets say google_api [Google APIs] so
    // perhaps the actual name which looks to be google_api will be the only string in the target in the future
    if (StringUtils.containsIgnoreCase(target, "Google APIs") || StringUtils.containsIgnoreCase(target, "google_apis")) {
      this.apiTargetType = "google";
    }
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

  public static List<AndroidEmulator> listAvailableAvds() throws AndroidDeviceException {
    List<AndroidEmulator> avds = Lists.newArrayList();

    CommandLine cmd = new CommandLine(AndroidSdk.android());
    cmd.addArgument("list", false);
    cmd.addArgument("avds", false);

    String output = null;
    try {
      output = ShellCommand.exec(cmd, 20000);
    } catch (ShellCommandException e) {
      throw new AndroidDeviceException(e);
    }
    Map<String, Integer> startedDevices = mapDeviceNamesToSerial();

    String[] avdsOutput = StringUtils.splitByWholeSeparator(output, "---------");
    if (avdsOutput != null && avdsOutput.length > 0) {
      for (String element : avdsOutput) {
        if (!element.contains("Name:")) {
          continue;
        }
        DefaultAndroidEmulator emulator = new DefaultAndroidEmulator(element);
        if (startedDevices.containsKey(emulator.getAvdName())) {
          emulator.setSerial(startedDevices.get(emulator.getAvdName()));
        }
        avds.add(emulator);
      }
    }
    return avds;
  }

  private static Dimension getScreenSizeFromSkin(String skinName) {
    final Pattern dimensionSkinPattern = Pattern.compile("([0-9]+)x([0-9]+)");
    Matcher matcher = dimensionSkinPattern.matcher(skinName);
    if (matcher.matches()) {
      int width = Integer.parseInt(matcher.group(1));
      int height = Integer.parseInt(matcher.group(2));
      return new Dimension(width, height);
    } else if (SKIN_NAME_DIMENSIONS.containsKey(skinName.toUpperCase())) {
      return SKIN_NAME_DIMENSIONS.get(skinName.toUpperCase());
    } else {
      log.warning("Failed to get dimensions for skin: " + skinName);
      return null;
    }
  }

  private static Map<String, Integer> mapDeviceNamesToSerial() {
    Map<String, Integer> mapping = new HashMap<String, Integer>();
    CommandLine command = new CommandLine(AndroidSdk.adb());
    command.addArgument("devices");
    List<String> allLines = Lists.newArrayList();

    try {
      allLines = getADBDevicesOutput();
    } catch (ShellCommandException e) {
      log.warning("Shell exception when trying to get adb devices");
      log.warning(e.getMessage());
    }

    for (String line: allLines) {
      Pattern pattern = Pattern.compile("emulator-\\d+");
      Matcher matcher = pattern.matcher(line);
      if (matcher.find()) {
        String serial = matcher.group(0);


        Integer port = Integer.valueOf(serial.replaceAll("emulator-", ""));
        TelnetClient client = null;
        try {
          client = new TelnetClient(port);
          String avdName = client.sendCommand("avd name");
          mapping.put(avdName, port);
        } catch (AndroidDeviceException e) {
          // ignore
        } finally {
          if (client != null) {
            client.close();
          }
        }
      }
    }

    return mapping;
  }

  @Override
  public boolean isEmulatorStarted() {
      File lockedEmulatorHardwareConfig =
              new File(getAvdRootFolder(), ANDROID_EMULATOR_HARDWARE_CONFIG + FILE_LOCKING_SUFIX);
      return lockedEmulatorHardwareConfig.exists();
  }

  @Override
  public void start(Locale locale, int emulatorPort, Map<String, Object> options)
      throws AndroidDeviceException {
    if (isEmulatorStarted()) {
      throw new SelendroidException("Error - Android emulator is already started " + this);
    }
    Long timeout = null;
    String emulatorOptions = null;
    String display = null;
    if (options != null) {
      if (options.containsKey(TIMEOUT_OPTION)) {
        timeout = (Long) options.get(TIMEOUT_OPTION);
      }
      if (options.containsKey(DISPLAY_OPTION)) {
        display = (String) options.get(DISPLAY_OPTION);
      }
      if (options.containsKey(EMULATOR_OPTIONS)) {
        emulatorOptions = (String) options.get(EMULATOR_OPTIONS);
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

    CommandLine cmd = new CommandLine(AndroidSdk.emulator());


    cmd.addArgument("-no-snapshot-save", false);
    cmd.addArgument("-avd", false);
    cmd.addArgument(getAvdName(), false);
    cmd.addArgument("-port", false);
    cmd.addArgument(String.valueOf(emulatorPort), false);
    if (locale != null) {
      cmd.addArgument("-prop", false);
      cmd.addArgument("persist.sys.language=" + locale.getLanguage(), false);
      cmd.addArgument("-prop", false);
      cmd.addArgument("persist.sys.country=" + locale.getCountry(), false);
    }
    if (emulatorOptions != null && !emulatorOptions.isEmpty()) {
      cmd.addArguments(emulatorOptions.split(" "), false);
    }

    long start = System.currentTimeMillis();
    long timeoutEnd = start + timeout;
    try {
      ShellCommand.execAsync(display, cmd);
    } catch (ShellCommandException e) {
      throw new SelendroidException("unable to start the emulator: " + this);
    }
    setSerial(emulatorPort);
    Boolean adbKillServerAttempted = false;

    // Without this one seconds, the call to "isDeviceReady" is
    // too quickly sent while the emulator is still starting and
    // not ready to receive any commands. Because of this the
    // while loops failed and sometimes hung in isDeviceReady function.
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    while (!isDeviceReady()) {
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
      if (timeoutEnd >= System.currentTimeMillis()) {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      } else {
        throw new AndroidDeviceException("The emulator with avd '" + getAvdName()
            + "' was not started after " + (System.currentTimeMillis() - start) / 1000
            + " seconds.");
      }
    }

    log.info("Emulator start took: " + (System.currentTimeMillis() - start) / 1000 + " seconds");
    log.info("Please have in mind, starting an emulator takes usually about 45 seconds.");
    unlockScreen();

    waitForLauncherToComplete();

    // we observed that emulators can sometimes not be 'fully loaded'
    // if we click on the All Apps button and wait for it to load it is more likely to be in a
    // usable state.
    allAppsGridView();

    waitForLauncherToComplete();
    setWasStartedBySelendroid(true);
  }

  private void waitForLauncherToComplete() throws AndroidDeviceException {
    CommandLine processListCommand = getAdbCommand();
    processListCommand.addArgument("shell", false);
    processListCommand.addArgument("ps", false);
    String processList = null;
    do {
      try {
        processList = ShellCommand.exec(processListCommand, 20000);
      } catch (ShellCommandException e) {
        throw new AndroidDeviceException(e);
      }

      //Wait a bit
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    } while (processList == null || !processList.contains("S com.android.launcher"));
  }

  private void allAppsGridView() throws AndroidDeviceException {
    int x = getScreenSize().width;
    int y = getScreenSize().height;
    if (x > y) {
      y = y / 2;
      x = x - 30;
    } else {
      x = x / 2;
      y = y - 30;
    }

    List<String> coordinates = new ArrayList<String>();
    coordinates.add("3 0 " + x);
    coordinates.add("3 1 " + y);
    coordinates.add("1 330 1");
    coordinates.add("0 0 0");
    coordinates.add("1 330 0");
    coordinates.add("0 0 0");

    for (String coordinate : coordinates) {
      CommandLine event1 = getAdbCommand();
      event1.addArgument("shell", false);
      event1.addArgument("sendevent", false);
      event1.addArgument("dev/input/event0", false);
      event1.addArgument(coordinate, false);
      try {
        ShellCommand.exec(event1);
      } catch (ShellCommandException e) {
        throw new AndroidDeviceException(e);
      }
    }

    try {
      Thread.sleep(750);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void stopEmulator() throws AndroidDeviceException {
    TelnetClient client = null;
    try {
      client = new TelnetClient(getPort());
      client.sendQuietly("kill");
    } catch (AndroidDeviceException e) {
      // ignore
    } finally {
      if (client != null) {
        client.close();
      }
    }
  }

  @Override
  public void stop() throws AndroidDeviceException {
    if (wasStartedBySelendroid) {
      stopEmulator();
      Boolean killed = false;
      while (isEmulatorStarted()) {
        log.info("emulator still running, sleeping 0.5, waiting for it to release the lock");
        try {
          Thread.sleep(500);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }

        if (!killed) {
          try {
            stopEmulator();
          } catch (AndroidDeviceException sce) {
            killed = true;
          }
        }
      }
    }
  }

  public void setWasStartedBySelendroid(boolean wasStartedBySelendroid) {
    this.wasStartedBySelendroid = wasStartedBySelendroid;
  }

  @Override
  public String toString() {
    return "AndroidEmulator [screenSize=" + getScreenSize() + ", targetPlatform=" + getTargetPlatform()
        + ", serial=" + getSerial() + ", avdName=" + getAvdName() + ", model=" + model + ", apiTargetType=" + apiTargetType + "]";
  }
}
