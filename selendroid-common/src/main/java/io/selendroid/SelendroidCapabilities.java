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
package io.selendroid;

import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM;
import static org.openqa.selenium.remote.CapabilityType.VERSION;
import io.selendroid.device.DeviceTargetPlatform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class SelendroidCapabilities extends DesiredCapabilities {
  private static final long serialVersionUID = -7061568919298342362L;
  @Deprecated
  public static final String ANDROID_TARGET = "androidTarget";
  public static final String AUT = "aut";
  public static final String EMULATOR = "emulator";
  public static final String DISPLAY = "display";
  public static final String LOCALE = "locale";
  public static final String SCREEN_SIZE = "screenSize";
  public static final String PRE_SESSION_ADB_COMMANDS = "preSessionAdbCommands";
  public static final String SERIAL = "serial";

  public static final String PLATFORM_VERSION = "platformVersion";
  public static final String PLATFORM_NAME = "platformName";
  public static final String AUTOMATION_NAME = "automationName";

  public SelendroidCapabilities(Map<String, ?> from) {
    for (String key : from.keySet()) {
      setCapability(key, from.get(key));
    }
  }

  public String getSerial() {
    if (getRawCapabilities().get(SERIAL) == null
        || getRawCapabilities().get(SERIAL).equals(JSONObject.NULL)) return null;
    return (String) getRawCapabilities().get(SERIAL);
  }


  public String getPlatformVersion() {
    return (String) getRawCapabilities().get(PLATFORM_VERSION);
  }

  /**
   * @deprecated use {@link #getPlatformVersion()} instead
   */
  @Deprecated
  public String getAndroidTarget() {
    return (String) getRawCapabilities().get(ANDROID_TARGET);
  }

  public String getAut() {
    return (String) getRawCapabilities().get(AUT);
  }

  public Boolean getEmulator() {
    if (getRawCapabilities().get(EMULATOR) == null
        || getRawCapabilities().get(EMULATOR).equals(JSONObject.NULL)) return null;
    return (Boolean) getRawCapabilities().get(EMULATOR);
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

  public void setSerial(String serial) {
    setCapability(SERIAL, serial);
  }

  public void setPlatformVersion(DeviceTargetPlatform androidTarget) {
    setCapability(PLATFORM_VERSION, androidTarget.getApi());
  }

  /**
   * @deprecated use {@link #setPlatformVersion(DeviceTargetPlatform)} instead
   */
  @Deprecated
  public void setAndroidTarget(String androidTarget) {
    setCapability(ANDROID_TARGET, androidTarget);
  }

  public void setAut(String aut) {
    setCapability(AUT, aut);
  }

  public void setEmulator(Boolean emulator) {
    setCapability(EMULATOR, emulator);
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
    if (source.has(CapabilityType.BROWSER_NAME) && !source.has(AUT)) {
      setAut(source.getString(CapabilityType.BROWSER_NAME));
    }
  }

  public SelendroidCapabilities() {
    super();
    setBrowserName("selendroid");
    setLocale("en_US");
  }

  public SelendroidCapabilities(String aut) {
    setAut(aut);
  }

  public SelendroidCapabilities(String serial, String aut) {
    setAut(aut);
    setSerial(serial);
    if (serial == null) {
      setEmulator(null);
    } else if (serial.startsWith("emulator")) {
      setEmulator(true);
    } else {
      setEmulator(false);
    }
  }

  /**
   * 
   * @param aut The application under test. Expected format is basePackage:version. E.g.:
   *        io.selendroid.testapp:0.4
   * @return Desired Capabilities of an emulator.
   */
  public static SelendroidCapabilities emulator(String aut) {
    SelendroidCapabilities caps = new SelendroidCapabilities();
    caps.setAut(aut);
    caps.setEmulator(true);
    return caps;
  }

  /**
   * 
   * @param platform The Android target platform to use.
   * @param aut The application under test. Expected format is basePackage:version. E.g.:
   *        io.selendroid.testapp:0.4
   * @return Desired Capabilities of an emulator.
   */
  public static SelendroidCapabilities emulator(DeviceTargetPlatform platform, String aut) {
    SelendroidCapabilities caps = new SelendroidCapabilities();
    caps.setPlatformVersion(platform);
    caps.setLocale("en_US");
    caps.setAut(aut);
    caps.setEmulator(true);
    return caps;
  }

  public static DesiredCapabilities android(DeviceTargetPlatform platform) {
    SelendroidCapabilities capabilities = new SelendroidCapabilities();
    capabilities.setCapability(BROWSER_NAME, BrowserType.ANDROID);
    capabilities.setCapability(VERSION, "");
    capabilities.setCapability(PLATFORM, platform);
    capabilities.setCapability(PLATFORM_VERSION, platform.getApi());
    return capabilities;
  }

  /**
   * @return The list of ADB commands that will be executed before the test session starts on the
   *         device.
   */
  @SuppressWarnings("unchecked")
  public List<String> getPreSessionAdbCommands() {
    List<String> res = new ArrayList<String>();

    Object capa = getCapability(PRE_SESSION_ADB_COMMANDS);
    if (capa != null) {
      res.addAll((Collection<String>) capa);
    }
    return res;
  }

  /**
   * Command like: "shell setprop name selendroid", please note that the adb command itself and the
   * serial will be added by selendroid automatically.
   * 
   * @param commands The list of ADB commands that will be executed before the test session starts
   *        on the device.
   */
  public void setPreSessionAdbCommands(List<String> commands) {
    setCapability(PRE_SESSION_ADB_COMMANDS, commands);
  }

  /**
   * 
   * @param platform The Android target platform to use.
   * @param aut The application under test. Expected format is basePackage:version. E.g.:
   *        io.selendroid.testapp:0.4
   * @return Desired Capabilities of an device.
   */
  public static SelendroidCapabilities device(DeviceTargetPlatform platform, String aut) {
    SelendroidCapabilities caps = emulator(platform, aut);
    caps.setEmulator(false);

    return caps;
  }

  /**
   * 
   * @param aut The application under test. Expected format is basePackage:version. E.g.:
   *        io.selendroid.testapp:0.4
   * @return Desired Capabilities of an device.
   */
  public static SelendroidCapabilities device(String aut) {
    SelendroidCapabilities caps = emulator(aut);
    caps.setEmulator(false);

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
