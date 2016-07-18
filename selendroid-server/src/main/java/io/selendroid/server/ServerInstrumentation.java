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
package io.selendroid.server;

import android.app.Activity;
import android.app.Instrumentation;
import android.view.View;
import io.selendroid.server.android.ActivitiesReporter;
import io.selendroid.server.android.AndroidWait;
import io.selendroid.server.common.ServerDetails;
import io.selendroid.server.common.utils.CallLogEntry;
import io.selendroid.server.extension.ExtensionLoader;

import java.util.List;

public interface ServerInstrumentation extends ServerDetails {
  void onCreate();
  void onDestroy();
  void startService();
  void startService(String serviceClassName, String intentAction);
  void startMainActivity();
  void startActivity(String activityClassName);
  void startServer();
  void stopServer();
  View getRootView();
  ActivitiesReporter getActivitiesReporter();
  Activity getCurrentActivity();
  void setImplicitWait(long millis);
  void finishAllActivities();
  AndroidWait getAndroidWait();
  void backgroundActivity();
  void resumeActivity();
  void addCallLog(CallLogEntry log);
  List<CallLogEntry> readCallLog();
  Instrumentation getInstrumentation();
  ExtensionLoader getExtensionLoader();
}
