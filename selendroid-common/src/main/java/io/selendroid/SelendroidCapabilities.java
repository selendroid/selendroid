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
package io.selendroid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.selendroid.device.DeviceTargetPlatform;
import org.openqa.selenium.remote.DesiredCapabilities;

public class SelendroidCapabilities extends DesiredCapabilities {
  public static final String ANDROID_TARGET = "androidTarget";
  public static final String AUT = "aut";
  public static final String EMULATOR = "emulator";
  public static final String LANGUAGE = "language";
  public static final String LOCALE = "locale";
  public static final String SCREEN_SIZE = "screenSize";
  private static final long serialVersionUID = -7061568919298342362L;
  public static final String DEFAULT_SCREEN_SIZE = "320x480";

  public SelendroidCapabilities(Map<String, ?> from) {
    for (String key : from.keySet()) {
      setCapability(key, from.get(key));
    }
  }

  public String getAndroidTarget() {
    return (String) getRawCapabilities().get(ANDROID_TARGET);
  }

  public String getAut() {
    return (String) getRawCapabilities().get(AUT);
  }

  public Boolean getEmulator() {
    return (Boolean) getRawCapabilities().get(EMULATOR);
  }

  public String getLanguage() {
    return (String) getRawCapabilities().get(LANGUAGE);
  }

  public String getLocale() {
    return (String) getRawCapabilities().get(LOCALE);
  }

  public Map<String, Object> getRawCapabilities() {
    return (Map<String, Object>) asMap();
  }

  public String getScreenSize() {
    return (String) getRawCapabilities().get(SCREEN_SIZE);
  }

  public void setAndroidTarget(String androidTarget) {
    setCapability(ANDROID_TARGET, androidTarget);
  }

  public void setAut(String aut) {
    setCapability(AUT, aut);
  }

  public void setEmulator(Boolean emulator) {
    setCapability(EMULATOR, emulator);
  }

  public void setLanguage(String language) {
    setCapability(LANGUAGE, language);
  }

  public void setLocale(String locale) {
    setCapability(LOCALE, locale);
  }

  public void setScreenSize(String screenSize) {
    setCapability(SCREEN_SIZE, screenSize);
  }

  public SelendroidCapabilities(JSONObject source) throws JSONException {
    Iterator<String> iter = source.keys();
    while (iter.hasNext()) {
      String key = iter.next();
      Object value = source.get(key);

      setCapability(key, decode(value));
    }
  }

  public SelendroidCapabilities() {
    setEmulator(true);
    setLocale("en_US");
  }

  /**
   * 
   * @param platform The Android target platform to use.
   * @param aut The application under test. Expected format is basePackage:version. E.g.:
   *        org.openqa.selendroid.testapp:0.4
   * @return Desired Capabilities of an emulator.
   */
  public static SelendroidCapabilities emulator(DeviceTargetPlatform platform, String aut) {
    SelendroidCapabilities caps = new SelendroidCapabilities();
    caps.setAndroidTarget(platform.name());
    caps.setScreenSize(DEFAULT_SCREEN_SIZE);
    caps.setLocale("en_US");
    caps.setAut(aut);
    return caps;
  }

  private Object decode(Object o) throws JSONException {
    if (o instanceof JSONArray) {
      List<Object> res = new ArrayList<Object>();
      JSONArray array = (JSONArray) o;
      for (int i = 0; i < array.length(); i++) {
        Object r = array.get(i);
        res.add(decode(r));
      }
      return res;
    } else {
      return o;
    }
  }
}
