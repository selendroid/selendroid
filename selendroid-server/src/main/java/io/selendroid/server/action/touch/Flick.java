package io.selendroid.server.action.touch;

import io.selendroid.server.action.Action;
import io.selendroid.server.action.ActionContext;
import io.selendroid.server.android.internal.Point;
import io.selendroid.server.common.action.touch.FlickDirection;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.server.model.TouchScreen;

import org.json.JSONException;
import org.json.JSONObject;

public class Flick extends Action {

  public Flick(SelendroidDriver driver) {
    super(driver);
  }

  @Override
  public void perform(
      JSONObject properties, ActionContext context) throws JSONException {
    TouchScreen touchScreen = driver.getTouch();

    Point origin = getActionPosition(properties);
    FlickDirection direction = FlickDirection.fromString(properties.getString("direction"));
    int distance = properties.getInt("distance");
    int duration = properties.getInt("duration");

    touchScreen.flick(origin, direction, distance, duration);
  }
}
