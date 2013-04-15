/*
 * Copyright 2013 selendroid committers.
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
package org.openqa.selendroid.server.model;

import org.openqa.selendroid.android.internal.Dimension;
import org.openqa.selendroid.android.internal.Point;
import org.openqa.selendroid.server.model.interactions.Coordinates;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

public class AndroidRElement implements AndroidElement {
  
  public Integer id;

  private static final String NOT_IMPLEMENTED_ERROR_MSG =
      "Android R Element is only a placeholder for an id, you need to find a real element";
  
  public AndroidRElement(int id) {
    this.id = id;  
  }
  
  @Override
  public AndroidElement getParent() {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public Collection<AndroidElement> getChildren() {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public AndroidElement findElement(By by) throws NoSuchElementException {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public List<AndroidElement> findElements(By by) throws NoSuchElementException {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public void enterText(CharSequence... keysToSend) {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public String getText() {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public void click() {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public void submit() {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public boolean isSelected() {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public boolean isDisplayed() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public void clear() {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public Point getLocation() {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public Coordinates getCoordinates() {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public Dimension getSize() {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }

  @Override
  public String getAttribute(String name) {
    throw new RuntimeException(NOT_IMPLEMENTED_ERROR_MSG);
  }
}
