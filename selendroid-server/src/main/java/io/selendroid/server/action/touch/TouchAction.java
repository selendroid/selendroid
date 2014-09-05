package io.selendroid.server.action.touch;

import io.selendroid.server.action.Action;
import io.selendroid.server.action.ActionContext;
import io.selendroid.server.model.SelendroidDriver;

public abstract class TouchAction extends Action {
  protected ActionContext actionContext;

  public TouchAction(SelendroidDriver driver) {
    super(driver);

    actionContext = new ActionContext();
  }
}
