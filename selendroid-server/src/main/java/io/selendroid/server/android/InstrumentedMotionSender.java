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

package io.selendroid.server.android;

import android.view.MotionEvent;
import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.util.SelendroidLogger;

public class InstrumentedMotionSender implements MotionSender {

  private final ServerInstrumentation instrumentation;

  public InstrumentedMotionSender(ServerInstrumentation instrumentation) {
    this.instrumentation = instrumentation;
  }

  @Override
  public boolean send(Iterable<MotionEvent> events) {
    try {
      instrumentation.waitForIdleSync();
      for (MotionEvent event : events) {
        instrumentation.sendPointerSync(event);
      }
      return true;
    } catch (SecurityException ignored) {
      SelendroidLogger.error("Error sending instrumented MotionEvent", ignored);
    }
    return false;
  }
}
