/*
 * Copyright 2013-2014 eBay Software Foundation and selendroid committers.
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import java.util.List;
import java.util.Map;

import io.selendroid.server.common.action.touch.FlickDirection;
import io.selendroid.server.common.action.touch.TouchActionName;

/**
 * The TouchActionBuilder is used to construct TouchActions by sequencing low level
 * touch screen interactions that are executed on a device
 */
public class TouchActionBuilder {

  private List<Map<String, Object>> actionChain = Lists.newArrayList();
  private boolean isDown = false;

  private void addAction(String actionName, Map<String, Object> params) {
    params.put("name", actionName);
    actionChain.add(params);
  }

  public void addAction(String actionName) {
    Map<String, Object> params = Maps.newHashMap();
    addAction(actionName, params);
  }

  private Map<String, Object> getTouchParameters(WebElement element, int x, int y) {
    Map<String, Object> params = Maps.newHashMap();
    if (element != null) {
      params.put("element", ((RemoteWebElement) element).getId());
    }
    params.put("x", x);
    params.put("y", y);
    return params;
  }

  /**
   * Places the pointer down at the top left corner of the specified WebElement.
   * @param element WebElement to place pointer down on
   * @return this
   */
  public TouchActionBuilder pointerDown(WebElement element) {
    return pointerDown(element, 0, 0);
  }

  /**
   * Places Pointer down at the specified (x, y) coordinates.
   * @param x x-coordinate
   * @param y y-coordinate
   * @return this
   */
  public TouchActionBuilder pointerDown(int x, int y) {
    return pointerDown(null, x, y);
  }

  /**
   * Places pointer down at an offset from the top left corner of the specified WebElement
   * @param element WebElement to place pointer relative to
   * @param x x-offset from top left
   * @param y y-offset from top left
   * @return this
   */
  public TouchActionBuilder pointerDown(WebElement element, int x, int y) {
    Preconditions.checkState(!isDown);

    Map<String, Object> params = getTouchParameters(element, x, y);
    addAction(TouchActionName.POINTER_DOWN, params);

    isDown = true;
    return this;
  }

  /**
   * Removes pointer from the touch screen at it's current position.
   * This method should only be called after pointerDown() has been called.
   * @return this
   */
  public TouchActionBuilder pointerUp() {
    Preconditions.checkState(isDown);

    addAction(TouchActionName.POINTER_UP);
    return this;
  }

  /**
   * Moves the pointer to the top left corner of the specified WebElement.
   * This is only possible if the pointer is currently down.
   * @param element WebElement to place pointer down on
   * @return this
   */
  public TouchActionBuilder pointerMove(WebElement element) {
    return pointerMove(element, 0, 0);
  }

  /**
   * Places Moves the pointer to the specified (x, y) coordinates.
   * This is only possible if the pointer is currently down.
   * @param x x-coordinate
   * @param y y-coordinate
   * @return this
   */
  public TouchActionBuilder pointerMove(int x, int y) {
    return pointerMove(null, x, y);
  }

  /**
   * Moves the pointer to a position offset from the top left corner of the specified WebElement
   * This is only possible if the pointer is currently down.
   * @param element WebElement to place pointer relative to
   * @param x x-offset from top left
   * @param y y-offset from top left
   * @return this
   */
  public TouchActionBuilder pointerMove(WebElement element, int x, int y) {
    Preconditions.checkState(isDown);

    Map<String, Object> params = getTouchParameters(element, x, y);
    addAction(TouchActionName.POINTER_MOVE, params);
    return this;
  }

  /**
   * Pause can be used to synchronize actions in a MultiTouchAction chain, pause() is equivalent
   * to a no-op for a tick.
   * @return this
   */
  public TouchActionBuilder pause() {
    return pause(0);
  }

  /**
   * Pause for a given period of time (in milliseconds).
   * If used in a MultiTouchAction all actions will pause for the longest pause in a given tick.
   * @param ms Pause Time (in ms)
   * @return this
   */
  public TouchActionBuilder pause(int ms) {
    Map<String, Object> params = Maps.newHashMap();
    params.put("ms", ms);
    addAction(TouchActionName.PAUSE, params);
    return this;
  }

  /**
   * Non-standard (high-level) command to flick the screen in a given tick.
   * This method should not be used in MultiTouchActions, or if the pointer is already down in a
   * given position.
   * @param origin Point at which the flick should start
   * @param direction Direction in which to flick
   * @param distance Distance in which the flick should occur
   * @param duration Length of time the finger should be down on the screen
   * @return this
   */
  public TouchActionBuilder flick(Point origin, FlickDirection direction,
                                  int distance, int duration) {
    Preconditions.checkState(!isDown);

    Map<String, Object> params = Maps.newHashMap();
    params.put("x", origin.getX());
    params.put("y", origin.getY());
    params.put("direction", direction.getDirection());
    params.put("distance", distance);
    params.put("duration", duration);

    addAction(TouchActionName.FLICK, params);
    return this;
  }

  /**
   * Cancels the existing sequence of actions.
   * This will release the pointer if it is already down.
   * @return this
   */
  public TouchActionBuilder pointerCancel() {
    addAction(TouchActionName.POINTER_CANCEL);
    return this;
  }

  public TouchAction build() {
    return new TouchAction(ImmutableList.copyOf(actionChain));
  }
}
