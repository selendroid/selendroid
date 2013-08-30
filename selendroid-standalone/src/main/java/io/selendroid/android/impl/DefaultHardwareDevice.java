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

import io.selendroid.device.DeviceTargetPlatform;

import java.util.Locale;
import java.util.logging.Logger;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;


public class DefaultHardwareDevice extends AbstractDevice {
  private static final Logger log = Logger.getLogger(DefaultHardwareDevice.class.getName());
  private String model = null;

  private Locale locale = null;
  private DeviceTargetPlatform targetPlatform = null;
  private String screenSize = null;

  public DefaultHardwareDevice(IDevice device) {
    super(device);
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
  public String getScreenSize() {
    if (this.screenSize == null) {
      RawImage screeshot = null;

      try {
        screeshot = device.getScreenshot();
        this.screenSize = screeshot.height + "x" + screeshot.width;
      } catch (Exception e) {
        log.warning("was not able to determine screensize: " + e.getMessage());
        // can happen
        e.printStackTrace();
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
        + getTargetPlatform() + "]";
  }
}
