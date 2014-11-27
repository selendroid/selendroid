package io.selendroid.server.server.action.touch;

import io.selendroid.server.server.action.Action;
import io.selendroid.server.server.action.ActionContext;
import io.selendroid.server.server.model.SelendroidDriver;
import io.selendroid.server.server.model.TouchScreen;

import org.json.JSONException;
import org.json.JSONObject;

public class PointerCancel extends Action {

  public PointerCancel(SelendroidDriver driver) {
    super(driver);
  }

  @Override
  public void perform(
      JSONObject properties, ActionContext context) throws JSONException {
    TouchScreen touchScreen = driver.getTouch();

    if (context.getIsPressed()) {
      touchScreen.up(context.getCurrentX(), context.getCurrentY());
      context.release();
    }
  }
}
