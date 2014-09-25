package io.selendroid.server.action.touch;


import io.selendroid.server.action.ActionHandler;

public class TouchHandler extends ActionHandler {

  @Override
  public void init() {
    register(TouchActionName.POINTER_DOWN, PointerDown.class);
    register(TouchActionName.POINTER_UP, PointerUp.class);
    register(TouchActionName.POINTER_MOVE, PointerMove.class);
    register(TouchActionName.TAP, TAP.class);
    register(TouchActionName.POINTER_CANCEL, PointerCancel.class);

    // Non standard actions
    register(TouchActionName.FLICK, Flick.class);
  }
}
