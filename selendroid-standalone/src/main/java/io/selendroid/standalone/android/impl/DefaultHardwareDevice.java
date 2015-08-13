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

import io.selendroid.common.device.DeviceTargetPlatform;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;

import io.selendroid.standalone.exceptions.AndroidDeviceException;
import org.openqa.selenium.Dimension;

public class DefaultHardwareDevice extends AbstractDevice {
  private static final Logger log = Logger.getLogger(DefaultHardwareDevice.class.getName());
  private String model = null;

  private Locale locale = null;
  private DeviceTargetPlatform targetPlatform = null;
  private Dimension screenSize = null;

  public DefaultHardwareDevice(IDevice device) {
    super(device);
    // today the only API we check for is Google APIs by looking for a maps jar which only exists if google apis are on
    // the target
    String output = runAdbCommand("shell ls /system/framework/*map*");
    if (!output.contains("No such file")) {
      this.apiTargetType = "google";
    }
  }

  public String getModel() {
    if (model == null) {
      model = getProp("ro.product.model");
    }
    return model;
  }

  @Override
  protected String getProp(String key) {
    return device.getProperty(key);
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
  public Dimension getScreenSize() {
    if (this.screenSize == null) {
      try {
        RawImage screenshot = device.getScreenshot();
        this.screenSize = new Dimension(screenshot.width, screenshot.height);
      } catch (Exception e) {
        log.log(Level.WARNING, "Unable to determine screen size", e);
      }
    }

    return this.screenSize;
  }

  public Locale getLocale() {
    if (this.locale == null) {
      this.locale = new Locale(getProp("persist.sys.language"), getProp("persist.sys.country"));
    }
    return locale;
  }

  @Override
  public boolean isDeviceReady() {
    // TODO ddary maybe use property dev.bootcomplete
    return true;
  }

  @Override
  public String toString() {
    return "HardwareDevice [serial=" + serial + ", model=" + getModel() + ", targetVersion="
        + getTargetPlatform() + ", apiTargetType=" + apiTargetType + "]";
  }
  
  public String getSerial() {
    return serial;
  }

  public void unlockScreen() throws AndroidDeviceException {
    // Get phone's android version and whether screen is off or not.
    // Different ways to detect if screen is off depending on version.

    String output = runAdbCommand("shell dumpsys power");

    // Lollipop and up -- API >= 20
    if (Integer.parseInt(getTargetPlatform().getApi()) >= 20) {
      String value = extractValue("Display Power: state=(.*?)$", output);
      if (value.equals("OFF")) {
        // Wake screen
        inputKeyevent(26);
      }
      // Kitkat and below -- API <= 19
    } else {
      String value = extractValue("mScreenOn=(.*?)$", output);
      if (value.equals("false")) {
        // Wake screen
        inputKeyevent(26);
      }
    }
  }
}
