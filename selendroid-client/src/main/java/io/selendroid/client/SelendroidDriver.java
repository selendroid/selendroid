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
package io.selendroid.client;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import io.selendroid.client.adb.AdbConnection;
import io.selendroid.server.common.utils.CallLogEntry;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ContextAware;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rotatable;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.ExecuteMethod;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteTouchScreen;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;

import com.google.common.collect.ImmutableMap;

/**
 * {@inheritDoc}
 */
public class SelendroidDriver extends RemoteWebDriver
    implements
      HasTouchScreen,
      HasMultiTouchScreen,
      ScreenBrightness,
      Rotatable,
      Configuration,
      AdbSupport,
      ContextAware,
      SetsSystemProperties,
      CallsGc {

  private RemoteTouchScreen touchScreen;
  private MultiTouchScreen multiTouchScreen;
  private RemoteAdbConnection adbConnection;
  private TrackBall trackBall;

  private SelendroidDriver(CommandExecutor executor, Capabilities caps) throws Exception {
    super(executor, caps);
    RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(this);
    touchScreen = new RemoteTouchScreen(executeMethod);
    multiTouchScreen = new MultiTouchScreen(executeMethod);
    adbConnection = new RemoteAdbConnection(executeMethod);
    trackBall = new TrackBall(executeMethod);
  }

  public SelendroidDriver(URL url, Capabilities caps) throws Exception {
    this(new SelendroidCommandExecutor(url), caps);
  }

  public SelendroidDriver(Capabilities caps) throws Exception {
    this(new SelendroidCommandExecutor(), caps);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TouchScreen getTouch() {
    return touchScreen;
  }
  
  public void roll(int dimensionX, int dimensionY) {
	  trackBall.roll(dimensionX, dimensionY);
  }

  public MultiTouchScreen getMultiTouchScreen() { return multiTouchScreen; }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getBrightness() {
    Response response = execute("selendroid-getBrightness");
    Number value = (Number) response.getValue();
    return value.intValue();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setBrightness(int desiredBrightness) {
    execute("selendroid-setBrightness", ImmutableMap.of("brightness", desiredBrightness));
  }

  @Override
  public void rotate(ScreenOrientation orientation) {
    execute(org.openqa.selenium.remote.DriverCommand.SET_SCREEN_ORIENTATION,
        ImmutableMap.of("orientation", orientation));
  }

  @Override
  public ScreenOrientation getOrientation() {
    return ScreenOrientation.valueOf((String) execute(
        org.openqa.selenium.remote.DriverCommand.GET_SCREEN_ORIENTATION).getValue());
  }

  @Override
  public void setConfiguration(DriverCommand command, String key, Object value) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("command", command.command);
    parameters.put(key, value);
    execute("selendroid-setCommandConfiguration", parameters);
  }

  @Override
  public Map<String, Object> getConfiguration(DriverCommand command) {
    Response response =
        execute("selendroid-getCommandConfiguration", ImmutableMap.of("command", command.command));

    return (Map<String, Object>) response.getValue();
  }

  @Override
  public AdbConnection getAdbConnection() {
    return adbConnection;
  }

  public boolean isAirplaneModeEnabled() {
    return ((Number) execute("getNetworkConnection").getValue()).intValue() == 1;
  }

  public void setAirplaneMode(boolean enabled) {
    Map<String, Integer> mode = ImmutableMap.of("type", enabled ? 1 : 6);
    execute("setNetworkConnection", ImmutableMap.of("parameters", mode));
  }

  public class RemoteAdbConnection implements AdbConnection {
    private final ExecuteMethod executeMethod;

    public RemoteAdbConnection(ExecuteMethod executeMethod) {
      this.executeMethod = executeMethod;
    }

    @Override
    public void tap(int x, int y) {
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("x", x);
      parameters.put("y", y);
      executeMethod.execute("selendroid-adb-tap", parameters);
    }

    @Override
    public void sendText(String text) {
      executeMethod.execute("selendroid-adb-sendText", ImmutableMap.of("text", text));
    }

    @Override
    public void sendKeyEvent(int keyCode) {
      executeMethod.execute("selendroid-adb-sendKeyEvent", ImmutableMap.of("keyCode", keyCode));
    }

    @Override
    public String executeShellCommand(String command) {
      return (String) execute("selendroid-adb-executeShellCommand", ImmutableMap.of("command", command)).getValue();
    }
  }

  @Override
  public WebDriver context(String name) {
    execute(org.openqa.selenium.remote.DriverCommand.SWITCH_TO_CONTEXT,
        ImmutableMap.of("name", name));
    return this;
  }

  @Override
  public Set<String> getContextHandles() {
    Response response = execute(org.openqa.selenium.remote.DriverCommand.GET_CONTEXT_HANDLES);
    Object value = response.getValue();
    try {
      List<String> returnedValues = (List<String>) value;
      return new LinkedHashSet<String>(returnedValues);
    } catch (ClassCastException ex) {
      throw new WebDriverException("Returned value cannot be converted to List<String>: " + value,
          ex);
    }
  }

  @Override
  public String getContext() {
    return String.valueOf(execute(
        org.openqa.selenium.remote.DriverCommand.GET_CURRENT_CONTEXT_HANDLE).getValue());
  }

  /**
   * Sends app under test to background.
   */
  public void backgroundApp() {
    execute("backgroundApp");
  }

  /**
   * Bring app under test back to foreground with its previous state.
   */
  public void resumeApp() {
    execute("resumeApp");
  }

  public void addCallLog(CallLogEntry log) {
	Map<String, String> info = ImmutableMap.of("calllogjson", log.toJSON());
	execute("addCallLog", ImmutableMap.of("parameters",info));
  }

  public List<CallLogEntry> readCallLog() {
    Response response = execute("readCallLog");
    Object value = response.getValue();
    try {
      List<String> returnedLogs = (List<String>) value;
      List<CallLogEntry> logEntries = new ArrayList<CallLogEntry>(returnedLogs.size());
      for (String jsonLogEntry : returnedLogs) {
          logEntries.add(CallLogEntry.fromJson(jsonLogEntry));
      }
      return logEntries;
    } catch (ClassCastException ex) {
      throw new WebDriverException("Returned value cannot be converted to List<String>: " + value,
              ex);
    }
  }

  public Object callExtension(String extensionMethod) {
    return callExtension(extensionMethod, ImmutableMap.<String, Object>of());
  }

  public Object callExtension(String extensionMethod, Map<String, ?> parameters) {
    Map<String, Object> paramsWithHandler = Maps.newHashMap();
    paramsWithHandler.putAll(parameters);
    paramsWithHandler.put("handlerName", extensionMethod);
    Response response = execute("selendroid-handleByExtension", paramsWithHandler);
    return response.getValue();
  }

  /**
   * Sets a Java System Property.
   */
  @Override
  public void setSystemProperty(String propertyName, String value) {
    if (Strings.isNullOrEmpty(propertyName)) {
      throw new IllegalArgumentException("Property name can't be empty.");
    }

    execute(
        "-selendroid-setAndroidOsSystemProperty",
        ImmutableMap.of(
            "propertyName", propertyName,
            "value", value));
  }

  /**
   * Synchronously calls "System.gc()" on device.
   */
  @Override
  public void gc() {
    execute("-selendroid-forceGcExplicitly");
  }

}
