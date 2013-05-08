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
package org.openqa.selendroid;

import java.util.Map;

import org.openqa.selenium.remote.DesiredCapabilities;

public class SelendroidCapabilities extends DesiredCapabilities {
  public static final String ANDROID_API = "androidApi";
  public static final String AUT = "aut";
  public static final String EMULATOR = "emulator";
  public static final String LANGUAGE = "language";
  public static final String LOCALE = "locale";
  public static final String SCREEN_SIZE = "screenSize";
  private static final long serialVersionUID = -7061568919298342362L;

  public SelendroidCapabilities(Map<String, ?> from) {
    for (String key : from.keySet()) {
      setCapability(key, from.get(key));
    }
  }

  public String getAndroidApi() {
    return (String) getRawCapabilities().get(ANDROID_API);
  }

  public String getAut() {
    return (String) getRawCapabilities().get(AUT);
  }

  public String getEmulator() {
    return (String) getRawCapabilities().get(EMULATOR);
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

  public void setAndroidApi(String androidApi) {
    setCapability(ANDROID_API, androidApi);
  }

  public void setAut(String aut) {
    setCapability(AUT, aut);
  }

  public void setEmulator(String emulator) {
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
}
