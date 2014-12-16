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
package io.selendroid.client;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.remote.ExecuteMethod;

/**
 * Track-ball on android devices can execute roll and press (second currently not implemented) events.
 * Should be used with lists for precise scrolling (for less precise scrolling swiping can be used).
 */
public class TrackBall {

	private ExecuteMethod mExecuteMethod;
	
	public TrackBall(ExecuteMethod executeMethod) {
		this.mExecuteMethod = executeMethod;
	}
	
	public void roll(int dimensionX, int dimensionY) {
	      Map<String, Object> parameters = new HashMap<String, Object>();
	      parameters.put("dx", dimensionX);
	      parameters.put("dy", dimensionY);
	      mExecuteMethod.execute("roll", parameters);
	}
}