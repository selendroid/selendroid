package io.selendroid.server.action;

import io.selendroid.server.action.ActionContext;
import org.json.JSONException;
import org.json.JSONObject;
import io.selendroid.server.common.action.touch.TouchActionName;
import org.json.JSONArray;

import java.lang.String;

public class ActionChain {
    private JSONArray actionChain;
    private ActionContext context;
    private int pauseTime = 0;
    private int index = 0;

    public ActionChain(JSONObject actionChain) throws JSONException {
        this.actionChain = actionChain.getJSONArray("actions");
        this.context = new ActionContext();
    }
    public int length(){
        return actionChain.length();
    }
    public ActionContext getContext() { return context; }
    public int getPauseTime(){
        return pauseTime;
    }
    public boolean isPaused(){
        return pauseTime > 0;
    }
    public void updatePause(int time){
        this.pauseTime -= time;
        if(pauseTime < 0){
            pauseTime = 0;
        }
    }
    public boolean hasNext(){
        return index < length();
    }
    public JSONObject next() throws JSONException {
        JSONObject action = actionChain.getJSONObject(index++);
        String actionName = action.getString("name");
        if (actionName.equals(TouchActionName.PAUSE)) {
            pauseTime = action.getInt("ms");
        }
        return action;
    }

}