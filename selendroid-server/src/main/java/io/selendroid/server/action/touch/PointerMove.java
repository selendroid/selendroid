package io.selendroid.server.action.touch;

import io.selendroid.server.action.Action;
import io.selendroid.server.action.ActionContext;
import io.selendroid.server.android.internal.Point;
import io.selendroid.server.model.SelendroidDriver;

import org.json.JSONException;
import org.json.JSONObject;

public class PointerMove extends Action {

  public PointerMove(SelendroidDriver driver) {
    super(driver);
  }

  @Override
  public void perform(JSONObject properties, ActionContext context) throws JSONException {
    Point actionPosition = getActionPosition(properties);
    int x = actionPosition.getX();
    int y = actionPosition.getY();

    context.setPosition(x, y);
    driver.getTouch().move(x, y, context.getId());
  }
}
