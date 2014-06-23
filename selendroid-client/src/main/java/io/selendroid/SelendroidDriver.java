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
package io.selendroid;

import io.selendroid.adb.AdbConnection;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ContextAware;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rotatable;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
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
      ScreenBrightness,
      TakesScreenshot,
      Rotatable,
      Configuration,
      JavascriptExecutor,
      AdbSupport,
      ContextAware {

  private RemoteTouchScreen touchScreen;
  private RemoteAdbConnection adbConnection;

  public SelendroidDriver(URL url, Capabilities caps) throws Exception {
    super(new SelendroidCommandExecutor(url), caps);
    RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(this);
    touchScreen = new RemoteTouchScreen(executeMethod);
    adbConnection = new RemoteAdbConnection(executeMethod);
  }

  public SelendroidDriver(Capabilities caps) throws Exception {
    super(new SelendroidCommandExecutor(), caps);
    RemoteExecuteMethod executeMethod = new RemoteExecuteMethod(this);
    touchScreen = new RemoteTouchScreen(executeMethod);
    adbConnection = new RemoteAdbConnection(executeMethod);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TouchScreen getTouch() {
    return touchScreen;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
    String base64 =
        execute(org.openqa.selenium.remote.DriverCommand.SCREENSHOT).getValue().toString();
    return target.convertFromBase64Png(base64);
  }

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
    public void executeShellCommand(String command) {
      execute("selendroid-adb-executeShellCommand", ImmutableMap.of("command", command));
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

}
