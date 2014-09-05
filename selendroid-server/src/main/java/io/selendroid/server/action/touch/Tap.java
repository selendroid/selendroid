package io.selendroid.server.action.touch;

import io.selendroid.android.internal.Point;
import io.selendroid.server.action.Action;
import io.selendroid.server.action.ActionContext;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.server.model.TouchScreen;
import io.selendroid.server.model.interactions.AndroidCoordinates;
import org.json.JSONException;
import org.json.JSONObject;

public class Tap extends Action {

  public Tap(SelendroidDriver driver) {
    super(driver);
  }

  @Override
  public void perform(
      JSONObject properties, ActionContext context) throws JSONException {
    Point actionPosition = getActionPosition(properties);

    TouchScreen touchScreen = driver.getTouch();
    touchScreen.singleTap(new AndroidCoordinates(null, actionPosition));
  }
}
