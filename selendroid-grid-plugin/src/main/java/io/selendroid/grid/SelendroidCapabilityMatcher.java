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
package io.selendroid.grid;

import org.openqa.grid.internal.utils.CapabilityMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelendroidCapabilityMatcher implements CapabilityMatcher {
  public static final String AUT = "aut";
  public static final String PLATFORM_NAME = "platformName";
  public static final String SCREEN_SIZE = "screenSize";
  public static final String BROWSER_NAME = "browserName";
  public static final String PLATFORM_VERSION = "platformVersion";
  public static final String EMULATOR = "emulator";
  private final List<String> toConsider = new ArrayList<String>();

  public SelendroidCapabilityMatcher() {
    toConsider.add(BROWSER_NAME);
    toConsider.add(AUT);
    toConsider.add(PLATFORM_NAME);
    toConsider.add(SCREEN_SIZE);
    toConsider.add(PLATFORM_VERSION);
    toConsider.add(EMULATOR);
  }

  @Override
  public boolean matches(Map<String, Object> nodeCapability, Map<String, Object> requestedCapability) {
    if (nodeCapability == null || requestedCapability == null) {
      return false;
    }
    for (String key : requestedCapability.keySet()) {
      if (toConsider.contains(key)) {
        if (requestedCapability.get(key) != null) {
          String value = requestedCapability.get(key).toString();
          if (!value.equals(nodeCapability.get(key))) {
            return false;
          }
        } else {
          // null value matches anything.
        }
      }
    }
    return true;
  }

}
