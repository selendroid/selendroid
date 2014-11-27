package io.selendroid.server.common.action.touch;

import java.util.HashMap;
import java.util.Map;

public enum FlickDirection {
  UP("up", 0, -1),
  DOWN("down", 0, 1),
  LEFT("left", -1, 0),
  RIGHT("right", 1 ,0);

  private final String direction; // String representation so these can be sent over the wire
  private final int xMultiplier;
  private final int yMultiplier;

  private final static Map<String, FlickDirection> directionMap
      = new HashMap<String, FlickDirection>();

  FlickDirection(String direction, int xMultiplier, int yMultiplier) {
    this.direction = direction;
    this.xMultiplier = xMultiplier;
    this.yMultiplier = yMultiplier;
  }

  static {
    // Build map so we get the scroll direction for a given string
    for (FlickDirection direction : FlickDirection.values()) {
      directionMap.put(direction.getDirection(), direction);
    }
  }

  public static FlickDirection fromString(String directionString) {
    FlickDirection direction = directionMap.get(directionString);
    if (direction == null) {
      throw new IllegalArgumentException(
          "Attempted to lookup invalid direction: " + directionString);
    }

    return direction;
  }

  public String getDirection() {
    return direction;
  }

  public int getxMultiplier() {
    return xMultiplier;
  }

  public int getyMultiplier() {
    return yMultiplier;
  }
}
