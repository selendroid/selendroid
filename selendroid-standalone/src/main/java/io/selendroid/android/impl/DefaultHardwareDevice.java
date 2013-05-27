package io.selendroid.android.impl;

import io.selendroid.android.AndroidSdk;
import io.selendroid.device.DeviceTargetPlatform;

import java.util.List;
import java.util.Locale;

import com.beust.jcommander.internal.Lists;

public class DefaultHardwareDevice extends AbstractDevice {
  private String model = null;
  private Locale locale = null;
  private DeviceTargetPlatform targetPlatform = null;
  private String screenSize = null;

  public DefaultHardwareDevice(String serial) {
    super(serial);
  }

  public String getModel() {
    if (model == null) {
      model = getProp("ro.product.model");
    }
    return model;
  }

  public Integer getDeviceTargetPlatform() {
    return Integer.parseInt(getProp("ro.build.version.sdk"));
  }

  @Override
  public DeviceTargetPlatform getTargetPlatform() {
    if (targetPlatform == null) {
      String version = getProp("ro.build.version.sdk");
      targetPlatform = DeviceTargetPlatform.fromInt(version);
    }
    return targetPlatform;
  }

  @Override
  public String getScreenSize() {
    List<String> command = Lists.newArrayList();
    command.add(AndroidSdk.adb());
    if (isSerialConfigured()) {
      command.add("-s");
      command.add(serial);
    }
    command.add("shell");
    command.add("dumpsys");
    command.add("display");

    String output = executeCommand(command);

    this.screenSize = extractValue("PhysicalDisplayInfo\\{(.*?)\\,", output).replace(" ", "");
    // look for deviceWidth, deviceHeight
    return screenSize;
  }

  public Locale getLocale() {
    if (this.locale == null) {
      this.locale = new Locale(getProp("persist.sys.language"), getProp("persist.sys.country"));
    }
    return locale;
  }
}
