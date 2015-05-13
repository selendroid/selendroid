package io.selendroid.server.action;

import io.selendroid.server.util.Preconditions;

public class ActionContext {

  int currentX;
  int currentY;
  int id = 0;
  boolean isPressed = false;

  public void setPosition(int x, int y) {
    currentX = x;
    currentY = y;
  }

  public void setId(int i) {
    id = i;
  }

  public int getId() {
    return id;
  }

  public void press(int x, int y) {
    Preconditions.checkState(!isPressed);
    isPressed = true;
    setPosition(x, y);
  }

  public void release() {
    Preconditions.checkState(isPressed);
    isPressed = false;
  }

  public boolean getIsPressed() {
    return isPressed;
  }

  public int getCurrentX() {
    Preconditions.checkState(isPressed);
    return currentX;
  }

  public int getCurrentY() {
    Preconditions.checkState(isPressed);
    return currentY;
  }
}
