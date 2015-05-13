package io.selendroid.server.action;

import io.selendroid.server.action.ActionContext;
import org.json.JSONException;
import org.json.JSONObject;
import io.selendroid.server.common.action.touch.TouchActionName;
import org.json.JSONArray;

import java.lang.String;

/**
 * Class to iterate over JSON action chains
 */
public class ActionChain {
  private JSONArray actionChain;
  private ActionContext context;
  private String inputDevice;
  private int pauseTime = 0;
  private int index = 0;

  private static int id_counter = 0;

  public ActionChain(JSONObject actionChain) throws JSONException {
    this.inputDevice = actionChain.getString("inputDevice");
    this.actionChain = actionChain.getJSONArray("actions");
    this.context = new ActionContext();
    this.context.setId(id_counter++);
  }

  public int length() {
    return actionChain.length();
  }

  public ActionContext getContext() {
    return context;
  }

  public String getInputDevice() {
    return inputDevice;
  }

  public int getPauseTime() {
    return pauseTime;
  }

  public boolean hasNext() {
    return index < length();
  }

  public JSONObject next() throws JSONException {
    JSONObject action = actionChain.getJSONObject(index++);
    String actionName = action.getString("name");
    if (actionName.equals(TouchActionName.PAUSE)) {
      pauseTime = action.getInt("ms");
    }
    else {
      pauseTime = 0;
    }
    return action;
  }

}