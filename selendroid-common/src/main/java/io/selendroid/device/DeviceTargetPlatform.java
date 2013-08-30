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
package io.selendroid.device;

public enum DeviceTargetPlatform {
  ANDROID10, ANDROID11, ANDROID12, ANDROID13, ANDROID14, ANDROID15, ANDROID16, ANDROID17, ANDROID18;
  public static final String ANDROID = "ANDROID";

  public String getSdkFolderName() {
    return name().replace(ANDROID, "android-");
  }

  public Integer getVersion() {
    return Integer.parseInt(name().replace(ANDROID, ""));
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
}
