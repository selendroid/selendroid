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
package org.openqa.selendroid;

/**
 * Keystrokes to simulate Android native key events.
 * 
 * <pre>{@code
 * ((HasInputDevices) driver).getKeyboard().sendKeys(SelendroidKeys.MENU);
 * }</pre>
 * 
 * @author ddary
 */
public interface SelendroidKeys {
  public static final CharSequence BACK = String.valueOf('\uE100');
  public static final CharSequence ANDROID_HOME = String.valueOf('\uE101');
  public static final CharSequence MENU = String.valueOf('\uE102');
  public static final CharSequence SEARCH = String.valueOf('\uE103');
  public static final CharSequence SYM = String.valueOf('\uE104');
  public static final CharSequence ALT_RIGHT = String.valueOf('\uE105');
  public static final CharSequence SHIFT_RIGHT = String.valueOf('\uE106');
}
