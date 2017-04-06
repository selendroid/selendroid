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
package io.selendroid.server.model;

import io.selendroid.server.android.internal.Dimension;
import io.selendroid.server.android.internal.Point;
import io.selendroid.server.android.internal.Rectangle;
import io.selendroid.server.model.interactions.Coordinates;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

public interface AndroidElement {
  public AndroidElement getParent();

  public Collection<AndroidElement> getChildren();

  public AndroidElement findElement(By by) throws NoSuchElementException;

  public List<AndroidElement> findElements(By by) throws NoSuchElementException;

  public void enterText(CharSequence... keysToSend);
  
  public void setText(CharSequence... keysToSend);

  public String getText();

  public void click();

  public void submit();

  public boolean isSelected();

  public boolean isDisplayed();

  public boolean isEnabled();

  public void clear();

  public Point getLocation();

  public Rectangle getRect();

  public Coordinates getCoordinates();

  public Dimension getSize();

  public String getTagName();

  public String getAttribute(String name);

  public String toString();

  public String id();
}
