package io.selendroid.server.action.touch;

import io.selendroid.server.action.Action;
import io.selendroid.server.action.ActionContext;
import io.selendroid.server.android.internal.Point;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.server.model.TouchScreen;

import org.json.JSONException;
import org.json.JSONObject;

public class PointerUp extends Action {

  public PointerUp(SelendroidDriver driver) {
    super(driver);
  }

  @Override
  public void perform(JSONObject properties, ActionContext context) throws JSONException {
    TouchScreen touchScreen = driver.getTouch();

    touchScreen.up(context.getCurrentX(), context.getCurrentY(), context.getId());
    context.release();
  }
}
