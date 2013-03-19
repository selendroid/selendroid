package org.openqa.selendroid.server.model.interactions;

import org.openqa.selendroid.android.internal.Point;

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
