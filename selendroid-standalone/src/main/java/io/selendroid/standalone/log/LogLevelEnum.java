/*
 * Copyright 2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.standalone.log;

import java.util.logging.Level;

public enum LogLevelEnum {
  ERROR(Level.SEVERE), DEBUG(Level.FINE), INFO(Level.INFO), WARNING(Level.WARNING), VERBOSE(
      Level.FINEST);
  public Level level;

  LogLevelEnum(Level level) {
    this.level = level;
  }

  public static LogLevelEnum fromString(String code) {
    for (LogLevelEnum output : LogLevelEnum.values()) {
      if (output.toString().equalsIgnoreCase(code)) {
        return output;
      }
    }

    return null;
  }
}
