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
package io.selendroid.server.model.interactions;

import io.selendroid.server.android.internal.Point;

public class AndroidCoordinates implements Coordinates {

  private final String elementId;
  private final Point point;

  public AndroidCoordinates(String elementId, Point pointOfLocationOfElement) {
    this.elementId = elementId;
    this.point = pointOfLocationOfElement;
  }

  public Point getLocationOnScreen() {
    return point;
  }

  public Point getLocationInViewPort() {
    return point;
  }

  public Point getLocationInDOM() {
    return point;
  }

  public Object getAuxiliary() {
    return elementId;
  }
}
