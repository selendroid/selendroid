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
package io.selendroid.common;

import io.selendroid.common.device.DeviceTargetPlatform;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM;
import static org.openqa.selenium.remote.CapabilityType.VERSION;


public class SelendroidCapabilities extends DesiredCapabilities {
  private static final long serialVersionUID = -7061568919298342362L;
  private static final String SELENDROID = "selendroid";
  public static final String AUT = "aut";
  public static final String EMULATOR = "emulator";
  public static final String DISPLAY = "display";
  public static final String LOCALE = "locale";
  public static final String SCREEN_SIZE = "screenSize";
  public static final String PRE_SESSION_ADB_COMMANDS = "preSessionAdbCommands";
  public static final String SERIAL = "serial";
  public static final String MODEL = "model";
  // possible values are google, android
  public static final String API_TARGET_TYPE = "apiTargetType";

  public static final String PLATFORM_VERSION = "platformVersion";
  public static final String PLATFORM_NAME = "platformName";
  public static final String AUTOMATION_NAME = "automationName";
  
  public static final String LAUNCH_ACTIVITY = "launchActivity";
  public static final String SELENDROID_EXTENSIONS = "selendroidExtensions";
  public static final String BOOTSTRAP_CLASS_NAMES = "bootstrapClassNames";

  public static SelendroidCapabilities empty() {
    return new SelendroidCapabilities(new HashMap<String, Object>());
  }

  public static SelendroidCapabilities copyOf(SelendroidCapabilities caps) {
    return new SelendroidCapabilities(caps.getRawCapabilities());
  }

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

  public String getAut() {
    return (String) getRawCapabilities().get(AUT);
  }

  public String getModel() {
    return (String) getRawCapabilities().get(MODEL);
  }

  /**
   * Gets the API target type.
   *
   * @return the API target type
   */
  public String getAPITargetType() {
    return (String) getRawCapabilities().get(API_TARGET_TYPE);
  }

  public String getLaunchActivity() {
    return (String) getRawCapabilities().get(LAUNCH_ACTIVITY);
  }

  public Boolean getEmulator() {
    if (getRawCapabilities().get(EMULATOR) == null
        || getRawCapabilities().get(EMULATOR).equals(JSONObject.NULL)) return null;
    return getBooleanCapability(EMULATOR);
  }

  public String getPlatformName() {
    return (String) getRawCapabilities().get(PLATFORM_NAME);
  }

  public String getAutomationName() {
    return (String) getRawCapabilities().get(AUTOMATION_NAME);
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

  /**
   * Full path of the dex file (on the host machine) containing Selendroid extensions to be loaded at run time
   * Example: /home/user/extension.dex
   */
  public String getSelendroidExtensions() {
    return (String) getRawCapabilities().get(SELENDROID_EXTENSIONS);
  }

  /**
   * Full name of class to run on the device before starting the app under test.
   * The class must be a part of an extension dex pushed to the device.
   */
  public String getBootstrapClassNames() {
    return (String) getRawCapabilities().get(BOOTSTRAP_CLASS_NAMES);
  }

  public void setSerial(String serial) {
    setCapability(SERIAL, serial);
  }

  public void setModel(String model) {
    setCapability(MODEL, model);
  }

  /**
   * Sets the API target type. For example, set this to google if your application requires Google APIs.
   *
   * @param apiTargetType the new API target type
   */
  public void setAPITargetType(String apiTargetType) {
    setCapability(API_TARGET_TYPE, apiTargetType);
  }

  public void setPlatformVersion(DeviceTargetPlatform androidTarget) {
    setCapability(PLATFORM_VERSION, androidTarget.getApi());
  }

  public void setAut(String aut) {
    setCapability(AUT, aut);
  }
  
  public void setLaunchActivity(String launchActivity) {
	setCapability(LAUNCH_ACTIVITY, launchActivity);
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

  public SelendroidCapabilities setSelendroidExtensions(String filePath) {
    setCapability(SELENDROID_EXTENSIONS, filePath);
    return this;
  }

  public void setAutomationName(String automationName) {
        setCapability(AUTOMATION_NAME, automationName);
      }

  /**
   * Adds a class to run on app startup. Class names are stored as a string separated by commas.
   */
  public SelendroidCapabilities addBootstrapClass(String className) {
    String currentClassNames = getBootstrapClassNames();
    if (currentClassNames == null || currentClassNames.isEmpty()) {
      setCapability(BOOTSTRAP_CLASS_NAMES, className);
    } else {
      setCapability(BOOTSTRAP_CLASS_NAMES, currentClassNames + "," + className);
    }
    return this;
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
    setCapability(AUTOMATION_NAME, SELENDROID);
    setBrowserName(SELENDROID);
    setCapability(PLATFORM_NAME, "android");
  }

  public SelendroidCapabilities(String aut) {
    this();
    setAut(aut);
  }

  public SelendroidCapabilities(String serial, String aut) {
    this(aut);
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
    caps.setAut(aut);
    caps.setEmulator(true);
    return caps;
  }

  public static DesiredCapabilities android(DeviceTargetPlatform platform) {
    SelendroidCapabilities capabilities = new SelendroidCapabilities();
    capabilities.setCapability(BROWSER_NAME, BrowserType.ANDROID);
    capabilities.setCapability(VERSION, "");
    capabilities.setCapability(PLATFORM, "android");
    capabilities.setCapability(PLATFORM_NAME, "android");
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
  
  /**
   * Returns a copy of this instance with {@code caps} merged, overwriting existing keys on
   * collision.
   */
  public SelendroidCapabilities withMerged(SelendroidCapabilities caps) {
    SelendroidCapabilities copy = SelendroidCapabilities.copyOf(this);
    for (Map.Entry<String, Object> entry: caps.getRawCapabilities().entrySet()) {
      copy.setCapability(entry.getKey(), entry.getValue());
    }
    return copy;
  }

  /**
   * Returns the application under test in the format of "appName:appVersion", or "appName" if the supported application
   * does not have any version associated with it, or returns null if the requested app is not in the apps store. If the
   * launch activity is also specified with requested application then just return the requested application as app under
   * test so it can be later installed to the device by SelendroidStandaloneDriver.
   *
   * @param supportedApps The list of supported apps in the apps store.
   * @return The application under test in "appName" or "appName:appVersion" format, or null if the application is not
   *         in the list of supported apps and the launch activity is not specified.
   */
  public String getDefaultApp(Set<String> supportedApps) {
    String defaultApp = getAut();
    // if the launch activity is specified, just return.
    if (getLaunchActivity() != null) {
      return defaultApp;
    }
    // App version is not specified. Get the latest version from the apps store.
    if (!defaultApp.contains(":")) {
      return getDefaultVersion(supportedApps, defaultApp);
    }
    return supportedApps.contains(defaultApp) ? defaultApp : null;
  }

  // Go through the supported apps in the apps store to return the
  // the latest version of the app.
  private String getDefaultVersion(Set<String> keys, String appName) {
    SortedSet<String> listOfApps = new TreeSet<String>();
    for (String key : keys) {
      if (key.split(":")[0].contentEquals(appName)) {
        listOfApps.add(key);
      }
    }
    return listOfApps.size() > 0 ? listOfApps.last() : null;
  }

  // throws exception if user didn't pass the capability as a boolean or String parsable as boolean
  private Boolean getBooleanCapability(String key) {
    Object o = getRawCapabilities().get(key);
    if (o == null) {
      return null;
    } else if (o instanceof Boolean) {
      return (Boolean) o;
    } else if (o instanceof String
            && ("true".equalsIgnoreCase((String) o)
            || "false".equalsIgnoreCase((String) o))) {
      return Boolean.valueOf((String) o);
    } else {
      throw new ClassCastException(String.format(
          "DesiredCapability %s's value should be boolean: found value %s of type %s",
              key, o.toString(), o.getClass().getName()));
    }
  }
}
