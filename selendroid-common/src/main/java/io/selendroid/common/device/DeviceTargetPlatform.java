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
package io.selendroid.common.device;

public enum DeviceTargetPlatform {
	  ANDROID10("2.3.3"), ANDROID11("3.0"), ANDROID12("3.1"), ANDROID13("3.2"), ANDROID14("4.0"), 
	  ANDROID15("4.0.3"), ANDROID16("4.1.2"), ANDROID17("4.2.2"), ANDROID18("4.3"), ANDROID19("4.4"), 
	  ANDROID20("4.4"), ANDROID21("5.0"), ANDROID22("5.1"), ANDROID23("6.0");
  public static final String ANDROID = "ANDROID";

  private String versionNumber;
  private String api;

  DeviceTargetPlatform(String version) {
    this.versionNumber = version;
    this.api = this.name().replace(ANDROID, "");
  }

  public String getSdkFolderName() {
    return name().replace(ANDROID, "android-");
  }

  public static DeviceTargetPlatform fromPlatformVersion(String text) {
    if (text != null) {
      for (DeviceTargetPlatform b : DeviceTargetPlatform.values()) {
        if (b.name().equals(ANDROID + text) || b.name().equals(text)) {
          return b;
        }
      }
    }
    return null;
  }

  public static DeviceTargetPlatform fromInt(String text) {
    if (text != null) {
      for (DeviceTargetPlatform b : DeviceTargetPlatform.values()) {
        if (b.name().equals(ANDROID + text)) {
          return b;
        }
      }
    }
    return null;
  }

  /**
   * @return version number of OS displayed on the device.
   */
  public String getVersionNumber() {
    return versionNumber;
  }

  /**
   * @return api number, used in #{SelendroidCapabilities#PLATFORM_VERSION}
   */
  public String getApi() {
    return api;
  }
}
