/*
 * Copyright 2013 selendroid committers.
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
package io.selendroid.android;

public interface AndroidDevice {
  public boolean isDeviceReady();

  public void install(AndroidApp app);

  public void uninstall(AndroidApp app);

  public void clearUserData(AndroidApp app);

  public void startSelendroid(AndroidApp aut, int port);
  
  public boolean isSelendroidRunning();
  
  public int getSelendroidsPort();
}
