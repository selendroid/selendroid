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
package io.selendroid.server.model;

import io.selendroid.server.android.internal.Dimension;

import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface SelendroidDriver {

  public AndroidElement findElement(By by);

  public List<AndroidElement> findElements(By by);

  public String getCurrentUrl();

  public Session getSession();

  public JSONObject getSessionCapabilities(String sessionId);

  public JSONObject getFullWindowTree();
  
  public String getWindowSource();

  public String initializeSession(JSONObject desiredCapabilities);

  public void stopSession();

  public void switchContext(String type);

  public byte[] takeScreenshot();

  public Keyboard getKeyboard();

  public String getTitle();

  public void get(String url);

  public TouchScreen getTouch();

  public void addCookie(String url, Cookie cookie);

  public void deleteCookie(String url);

  public void deleteNamedCookie(String url, String name);

  public Set<Cookie> getCookies(String url);

  public Object executeScript(String script, JSONArray args);

  public Object executeScript(String script, Object... args);

  public Object executeAsyncScript(String script, JSONArray args);

  public String getContext();

  public Set<String> getContexts();

  public Dimension getWindowSize();

  public void setFrameContext(Object index) throws JSONException;

  public void back();

  public void forward();

  public void refresh();

  public boolean isAlertPresent();

  public String getAlertText();

  public void acceptAlert();

  public void dismissAlert();

  public void setAlertText(CharSequence... keysToSend);

  public ScreenOrientation getOrientation();

  public void rotate(ScreenOrientation orientation);

  public void setAsyncTimeout(long timeout);

  public void setPageLoadTimeout(long timeout);

  public boolean isAirplaneMode();
  
  public void roll(int dimensionX, int dimensionY);
}
