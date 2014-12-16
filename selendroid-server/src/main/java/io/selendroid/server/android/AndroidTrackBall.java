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
package io.selendroid.server.android;

import android.os.SystemClock;
import android.view.MotionEvent;
import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.model.TrackBall;

public class AndroidTrackBall implements TrackBall {

	private ServerInstrumentation instrumentation;

	public AndroidTrackBall(ServerInstrumentation instrumentation) {
		this.instrumentation = instrumentation;

	}

	@Override
	public void roll(int dx, int dy) {
		long eventTime = SystemClock.uptimeMillis();
		instrumentation.sendTrackballEventSync(MotionEvent.obtain(eventTime, eventTime, MotionEvent.ACTION_DOWN, dx, dy, 0));
		instrumentation.waitForIdleSync();
	}
}

