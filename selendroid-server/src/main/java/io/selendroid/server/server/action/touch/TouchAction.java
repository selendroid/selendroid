package io.selendroid.server.server.action.touch;

import io.selendroid.server.server.action.Action;
import io.selendroid.server.server.action.ActionContext;
import io.selendroid.server.server.model.SelendroidDriver;

public abstract class TouchAction extends Action {
  protected ActionContext actionContext;

  public TouchAction(SelendroidDriver driver) {
    super(driver);

    actionContext = new ActionContext();
  }
}
