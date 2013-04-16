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
import org.openqa.selendroid.server.model.Keyboard;

import android.app.Instrumentation;
import android.view.KeyEvent;

/**
 * Provides a method to send a string to an application under test. Keys are sent using an
 * {@code Instrumentation} instance. The strings may contain any character in the
 * {@link AndroidKeys} {@code enum}.
 * 
 * <p>
 * Note that this class does not focus on a particular {@code View} before sending keys, nor does it
 * require that some {@code View} has focus. This is fine if you are sending the Menu key, or using
 * the arrow keys to select an item in a list. If you are trying to type into a certain widget, be
 * sure it has focus before using this class.
 * 
 * @author Matt DeVore
 */
public class KeySender {
  private class KeyboardImpl implements Keyboard {
    @Override
    public void pressKey(Keys keyToPress) {
      sendKeyEvent(KeyEvent.ACTION_DOWN, keyToPress.charAt(0));
    }

    @Override
    public void releaseKey(Keys keyToRelease) {
      sendKeyEvent(KeyEvent.ACTION_UP, keyToRelease.charAt(0));
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
      StringBuilder sb = new StringBuilder();
      for (CharSequence keys : keysToSend) {
        sb.append(keys);
      }
      send(sb.toString());
    }
  }

  private final Instrumentation instrumentation;
  private final KeyboardImpl keyboardImpl;

  /**
   * Creates a new instance which sends keys to the given {@code Instrumentation}.
   */
  public KeySender(Instrumentation instrumentation) {
    //this.instrumentation = Preconditions.checkNotNull(instrumentation);
    this.instrumentation=instrumentation;
    this.keyboardImpl = new KeyboardImpl();
  }

  /**
   * Returns a {@code Keyboard} object which sends key using this {@code KeySender}.
   */
  public Keyboard getKeyboard() {
    return keyboardImpl;
  }

  private static int indexOfSpecialKey(CharSequence string, int startIndex) {
    for (int i = startIndex; i < string.length(); i++) {
      if (AndroidKeys.hasAndroidKeyEvent(string.charAt(i))) {
        return i;
      }
    }
    return string.length();
  }

  /**
   * Sends a single key event to the {@code Instrumentation} for a given character.
   * 
   * @param action {@code KeyEvent.ACTION_*} code representing key action
   * @param keyCode character representing key to press, release, etc.
   */
  public void sendKeyEvent(int action, char keyCode) {
    instrumentation.waitForIdleSync();
    try {
      instrumentation.sendKeySync(new KeyEvent(action, AndroidKeys.keyCodeFor(keyCode)));
    } catch (SecurityException exception) {
      throw new SelendroidException(exception);
    }
  }

  /**
   * A convenience wrapper for {@link #sendKeyEvent(int, char)} which takes an {@code AndroidKeys}
   * object.
   * 
   * @param action {@code KeyEvent.ACTION_*} code representing key action
   * @param key {@code AndroidKeys} object representing key to press, release, etc.
   */
  public void sendKeyEvent(int action, AndroidKeys key) {
    this.sendKeyEvent(action, key.charAt(0));
  }

  /**
   * Sends key events to the {@code Instrumentation}. This method will send a portion of the given
   * {@code CharSequence} as a single {@code String} if the portion does not contain any special
   * keys.
   * 
   * @param string the keys to send to the {@code Instrumentation}.
   */
  public void send(CharSequence string) {
    int currentIndex = 0;

    instrumentation.waitForIdleSync();

    while (currentIndex < string.length()) {
      char currentCharacter = string.charAt(currentIndex);
      if (AndroidKeys.hasAndroidKeyEvent(currentCharacter)) {
        // The next character is special and must be sent individually
        instrumentation.sendKeyDownUpSync(AndroidKeys.keyCodeFor(currentCharacter));
        currentIndex++;
      } else {
        // There is at least one "normal" character, that is a character
        // represented by a plain Unicode character that can be sent with
        // sendStringSync. So send as many such consecutive normal characters
        // as possible in a single String.
        int nextSpecialKey = indexOfSpecialKey(string, currentIndex);
        instrumentation.sendStringSync(string.subSequence(currentIndex, nextSpecialKey).toString());
        currentIndex = nextSpecialKey;
      }
    }
  }
}
