/*
Copyright 2007-2010 WebDriver committers
Copyright 2007-2010 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package io.selendroid.server.model;

/**
 * Interface representing basic keyboard operations.
 *
 * <h2>Known platform quirks</h2>
 * <ul>
 *   <li><b>Android 4.0 WebView (issue #267).</b> Sending characters to a
 *       WebView text input on Android 4.0 (API 14 to 15) yields an
 *       all-uppercase result because the system WebView leaks the SHIFT
 *       meta state between consecutive synthesized {@code KeyEvent}s. The
 *       behaviour does not appear on Android 2.3, 4.2 or 4.3. The bundled
 *       {@code InstrumentedKeySender} works around the bug by sending one
 *       character at a time and injecting an explicit {@code SHIFT_LEFT}
 *       {@code ACTION_UP} with a zeroed meta state between characters on
 *       API 14 and 15 only; other platforms keep the original batched
 *       fast path. {@code adb shell input text} via the
 *       {@code AdbSendText} handler in selendroid-standalone remains a
 *       valid alternative for tests that prefer to bypass the WebView
 *       input pipeline entirely.</li>
 * </ul>
 */
public interface Keyboard {
  /**
   * Type the given key sequence into the focused element.
   *
   * @param keysToSend characters to type, in order. Implementations are
   *                   expected to clear any previously latched modifier
   *                   meta state (SHIFT, ALT, CTRL) between characters so
   *                   that mixed-case input is preserved. See the class
   *                   javadoc for the Android 4.0 WebView caveat.
   */
  void sendKeys(CharSequence... keysToSend);
}
