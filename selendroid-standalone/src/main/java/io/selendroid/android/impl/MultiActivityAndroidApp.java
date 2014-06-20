/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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
package io.selendroid.android.impl;

import java.io.File;

/**
 * Class offers the option to set the main activity.
 * <ul>
 * Useful in following cases:
 * <li>if the activity to be started is not the one defined in Manifest.xml as
 * MAIN</li>
 * <li>or if the {@code<activity-alias>} tag is used instead of {@code<alias>}.</li>
 * </ul>
 */
public class MultiActivityAndroidApp extends DefaultAndroidApp {

	/**
	 * This constructor is mainly used by the ClassCast to transform
	 * DefaultAndroidApp into MultiActivityAndroidApp.
	 * 
	 * @param app
	 */
	public MultiActivityAndroidApp(DefaultAndroidApp app) {
		super(new File(app.getAbsolutePath()));
	}

	public void setMainActivity(String mainActivity) {
		this.mainActivity = mainActivity;
	}

}
