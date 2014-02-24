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

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rotatable;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
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
      Configuration {

  private RemoteTouchScreen touchScreen;

  public SelendroidDriver(URL url, Capabilities caps) throws Exception {
    super(new SelendroidCommandExecutor(url), caps);
    touchScreen = new RemoteTouchScreen(new RemoteExecuteMethod(this));
  }

  public SelendroidDriver(Capabilities caps) throws Exception {
    super(caps);
    touchScreen = new RemoteTouchScreen(new RemoteExecuteMethod(this));
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
    
    return (Map<String, Object>)response.getValue();
  }

}
