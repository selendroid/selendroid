package io.selendroid.server.action.touch;

import io.selendroid.server.action.Action;
import io.selendroid.server.action.ActionContext;
import io.selendroid.server.android.internal.Point;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.server.model.TouchScreen;

import org.json.JSONException;
import org.json.JSONObject;

public class PointerDown extends Action {

  public PointerDown(SelendroidDriver driver) {
    super(driver);
  }

  @Override
  public void perform(JSONObject properties, ActionContext context) throws JSONException {
    Point actionPosition = getActionPosition(properties);
    int x = actionPosition.getX();
    int y = actionPosition.getY();

    TouchScreen touchScreen = driver.getTouch();

    touchScreen.down(x, y, context.getId());
    context.press(x, y);
  }
}
