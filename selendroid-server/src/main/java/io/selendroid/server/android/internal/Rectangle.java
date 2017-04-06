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
package io.selendroid.server.android.internal;

/**
 * A copy of java.awt.Rectangle, to remove dependency on awt.
 */
public class Rectangle {
  public int x;
  public int y;
  public int width;
  public int height;

  public Rectangle(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Rectangle)) {
      return false;
    }

    Rectangle other = (Rectangle) o;
    return other.x == x && other.y == y && other.width == width && other.height == height;
  }

  @Override
  public int hashCode() {
    return x << 12 + y + width << 12 + height;
  }

  @Override
  public String toString() {
    return String.format("(%s, %s, %s, %s)", x, y, width, height);
  }
}
