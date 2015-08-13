package io.selendroid.standalone.android.impl;

import com.android.ddmlib.IDevice;
import com.beust.jcommander.internal.Lists;
import io.selendroid.common.device.DeviceTargetPlatform;
import io.selendroid.standalone.android.AndroidEmulator;
import io.selendroid.standalone.android.AndroidSdk;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.exceptions.ShellCommandException;
import io.selendroid.standalone.io.ShellCommand;
import org.apache.commons.exec.CommandLine;
import org.openqa.selenium.Dimension;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public abstract class AbstractAndroidDeviceEmulator extends AbstractDevice implements AndroidEmulator {

    protected static final String EMULATOR_SERIAL_PREFIX = "emulator-";
    private static final String LOCALHOST_SERIAL_PREFIX = "localhost:";
    protected String avdName;
    protected Dimension screenSize;
    protected DeviceTargetPlatform targetPlatform;
    protected File avdRootFolder;
    protected Locale locale = null;

    protected static List<String> getADBDevicesOutput() throws ShellCommandException {
      CommandLine command = new CommandLine(AndroidSdk.adb());
      command.addArgument("devices");
      Scanner scanner = new Scanner(ShellCommand.exec(command));
      List<String> allLines = Lists.newArrayList();
      while (scanner.hasNextLine()) {
        allLines.add(scanner.nextLine());
      }
      return allLines;
    }

    @Override
    public String getAvdName() {
      return avdName;
    }

    @Override
    public File getAvdRootFolder() {
      return avdRootFolder;
    }

    @Override
    public Dimension getScreenSize() {
      return screenSize;
    }

    @Override
    public DeviceTargetPlatform getTargetPlatform() {
      return targetPlatform;
    }

    @Override
    public void setSerial(String serial) {
      this.serial = serial;
    }

    @Override
    public Locale getLocale() {
      return locale;
    }

    @Override
    public void setIDevice(IDevice iDevice) {
      super.device = iDevice;
    }

    @Override
    public String getSerial() {
      return serial;
    }

    @Override
    public void setSerial(int port) {
      this.port = port;
      setSerial(EMULATOR_SERIAL_PREFIX + port);
    }

    public Integer getPort() {
      if (isSerialConfigured()) {
        return Integer.parseInt(getSerial().replace(LOCALHOST_SERIAL_PREFIX, "").replace(EMULATOR_SERIAL_PREFIX, ""));
      }
      return null;
    }

    public void unlockScreen() throws AndroidDeviceException {
      // Send menu key event
      CommandLine menuKeyCommand = getAdbCommand();
      menuKeyCommand.addArgument("shell", false);
      menuKeyCommand.addArgument("input", false);
      menuKeyCommand.addArgument("keyevent", false);
      menuKeyCommand.addArgument("82", false);

      try {
        ShellCommand.exec(menuKeyCommand, 20000);
      } catch (ShellCommandException e) {
        throw new AndroidDeviceException(e);
      }

      // Send back key event
      CommandLine backKeyCommand = getAdbCommand();
      backKeyCommand.addArgument("shell", false);
      backKeyCommand.addArgument("input", false);
      backKeyCommand.addArgument("keyevent", false);
      backKeyCommand.addArgument("4", false);
      try {
        ShellCommand.exec(backKeyCommand, 20000);
      } catch (ShellCommandException e) {
        throw new AndroidDeviceException(e);
      }
    }

    protected CommandLine getAdbCommand() {
      CommandLine processListCommand = new CommandLine(AndroidSdk.adb());
      if (isSerialConfigured()) {
        processListCommand.addArgument("-s", false);
        processListCommand.addArgument(getSerial(), false);
      }
      return processListCommand;
    }
}
