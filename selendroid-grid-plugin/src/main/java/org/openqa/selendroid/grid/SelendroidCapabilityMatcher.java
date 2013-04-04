/*
 * Copyright 2013 selendroid committers.
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
package org.openqa.selendroid.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.grid.internal.utils.CapabilityMatcher;

public class SelendroidCapabilityMatcher implements CapabilityMatcher {
  public static final String LOCALE = "locale";
  public static final String AUT = "aut";
  public static final String SDK_VERSION = "sdkVersion";
  public static final String SCREEN_SIZE = "screenSize";
  public static final String BROWSER_NAME = "browserName";
  private final List<String> toConsider = new ArrayList<String>();

  public SelendroidCapabilityMatcher() {
    toConsider.add(BROWSER_NAME);
    toConsider.add(LOCALE);
    toConsider.add(AUT);
    toConsider.add(SDK_VERSION);
    toConsider.add(SCREEN_SIZE);
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
          if (!requestedCapability.get(key).equals(nodeCapability.get(key))) {
            return false;
          } else {
            // null value matches anything.
          }
        }
      }
    }
    return true;
  }

}
