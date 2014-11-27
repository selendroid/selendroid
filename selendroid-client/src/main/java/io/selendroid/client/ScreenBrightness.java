/*
 * Copyright 2013-2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.client;

/**
 * Allow the user to set the brightness of the screen, turning it on or off as necessary.
 */
public interface ScreenBrightness {
  /**
   * @return The brightness of the screen, with 0% meaning off and 100% being at full brightness.
   */
  int getBrightness();

  /**
   * @param desiredBrightness The brightness to set the screen to, as a percentage.
   */
  void setBrightness(int desiredBrightness);
}
