/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.util;


// TODO ddary rethink logging concept that works also on jvm
public class SelendroidLogger {
  // private static final Logger logger = LoggerFactory.getLogger(SelendroidLogger.class);

  public static void log(String message) {
    System.out.println(message);
  }

  public static void log(String message, Exception e) {
    System.out.println(message);
    if (e != null) {
      e.printStackTrace();
    }
  }
}
