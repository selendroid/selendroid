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

public enum DeviceType {
  PHONE, EMULATOR;

  public static DeviceType fromString(String text) {
    if (text != null) {
      for (DeviceType b : DeviceType.values()) {
        if (text.equalsIgnoreCase(b.name())) {
          return b;
        }
      }
    }
    return null;
  }
}
