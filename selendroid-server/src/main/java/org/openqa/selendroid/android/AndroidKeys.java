/*
 * Copyright 2011 NativeDriver committers Copyright 2011 Google Inc.
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

package org.openqa.selendroid.android;

import org.openqa.selendroid.server.exceptions.SelendroidException;

import android.view.KeyEvent;

/**
 * Contains keys that can be sent to Android Native driver implementations of
 * {@link org.openqa.selenium.WebElement#sendKeys}. These keys are not easily represented as
 * strings, so an instance of this {@code enum} can be used instead. Each instance is a
 * {@code CharSequence} that is exactly one character long, and that character is a value in the
 * Unicode private use space.
 * 
 * <p>
 * For keys that are also in {@link Keys}, the key code in this class is the same.
 * 
 * @author Matt DeVore
 */
public enum AndroidKeys implements CharSequence {
  // Keys that are shared with normal WebDriver (sorted alphabetically)
  ALT_LEFT(Keys.ALT, KeyEvent.KEYCODE_ALT_LEFT), DEL(Keys.DELETE, KeyEvent.KEYCODE_DEL), DPAD_DOWN(
      Keys.ARROW_DOWN, KeyEvent.KEYCODE_DPAD_DOWN), DPAD_LEFT(Keys.ARROW_LEFT,
      KeyEvent.KEYCODE_DPAD_LEFT), DPAD_RIGHT(Keys.ARROW_RIGHT, KeyEvent.KEYCODE_DPAD_RIGHT), DPAD_UP(
      Keys.ARROW_UP, KeyEvent.KEYCODE_DPAD_UP), ENTER(Keys.ENTER, KeyEvent.KEYCODE_ENTER), SHIFT_LEFT(
      Keys.SHIFT, KeyEvent.KEYCODE_SHIFT_LEFT),

  // Keys only for native Android apps (sorted by key code)
  BACK('\uE100', KeyEvent.KEYCODE_BACK), HOME('\uE101', KeyEvent.KEYCODE_HOME), MENU('\uE102',
      KeyEvent.KEYCODE_MENU), SEARCH('\uE103', KeyEvent.KEYCODE_SEARCH), SYM('\uE104',
      KeyEvent.KEYCODE_SYM), ALT_RIGHT('\uE105', KeyEvent.KEYCODE_ALT_RIGHT), SHIFT_RIGHT('\uE106',
      KeyEvent.KEYCODE_SHIFT_RIGHT);

  private final char keyCode;
  private final int androidKeyCode;

  private AndroidKeys(char keyCode, int androidKeyCode) {
    this.keyCode = keyCode;
    this.androidKeyCode = androidKeyCode;
  }

  private AndroidKeys(Keys key, int androidKeyCode) {
    this.keyCode = key.charAt(0);
    this.androidKeyCode = androidKeyCode;
  }

  /**
   * Returns a character's corresponding Android {@code KeyEvent} code.
   * 
   * @param keyCode character to get {@code KeyEvent} code for
   * @return integer representing {@code KeyEvent} code
   */
  public static int keyCodeFor(char keyCode) throws SelendroidException {
    // see whether char is a special key; if so, return that
    for (AndroidKeys key : AndroidKeys.values()) {
      if (key.charAt(0) == keyCode) {
        return key.getAndroidKeyCode();
      }
    }

    // otherwise, figure out corresponding KeyEvent integer
    char upperCaseKey = Character.toUpperCase(keyCode);
    if (Character.isDigit(upperCaseKey)) {
      return upperCaseKey - '0' + KeyEvent.KEYCODE_0;
    }
    if (Character.isLetter(upperCaseKey)) {
      return upperCaseKey - 'A' + KeyEvent.KEYCODE_A;
    }
    throw new SelendroidException("Character '" + keyCode + "' is not yet "
        + "supported by Selendroid.");
  }

  /**
   * Returns true if key character is defined within {@code AndroidKeys}.
   * 
   * @param keyCode character to check
   * @return true if key is present within {@code AndroidKeys}
   */
  public static boolean hasAndroidKeyEvent(char keyCode) {
    for (AndroidKeys key : AndroidKeys.values()) {
      if (key.charAt(0) == keyCode) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns key's corresponding Android {@code KeyEvent} code.
   * 
   * @return Android {@code KeyEvent} code
   */
  public int getAndroidKeyCode() {
    return androidKeyCode;
  }

  @Override
  public char charAt(int index) {
    if (index != 0) {
      throw new IndexOutOfBoundsException();
    }

    return keyCode;
  }

  @Override
  public int length() {
    return 1;
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    if (end == start) {
      return "";
    } else if (start == 0 && end == 1) {
      return this;
    } else {
      throw new IndexOutOfBoundsException();
    }
  }

  @Override
  public String toString() {

    return String.valueOf(keyCode);
  }
}
