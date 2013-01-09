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
package org.openqa.selendroid.server;

import java.util.List;

import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.server.model.By;

import com.google.gson.JsonObject;

public interface AndroidDriver {

  public abstract Session getSession();

  public abstract String getCurrentUrl();

  public abstract JsonObject getSessionCapabilities(String sessionId);

  public abstract String initializeSessionForCapabilities(JsonObject desiredCapabilities);

  public abstract void stopSession();

  public abstract AndroidElement findElement(By by);

  public abstract List<AndroidElement> findElements(By by);

  public abstract JsonObject getSourceOfCurrentActivity();

  public abstract byte[] takeScreenshot();
}
