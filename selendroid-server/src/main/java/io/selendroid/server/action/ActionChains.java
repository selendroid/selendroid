import io.selendroid.server.action.ActionContext;
import org.json.JSONException;
import org.json.JSONObject;
import io.selendroid.server.common.action.touch.TouchActionName;
import org.json.JSONArray;

import java.lang.String;

public class ActionChains {
    private JSONArray actionChain;
    private ActionContext context;
    private int pauseTime = 0;
    private int index = 0;

    public ActionChains(JSONObject actionChain) throws JSONException {
        this.actionChain = actionChain.getJSONArray("action");
        this.context = new ActionContext();
    }
    public int length(){
        return actionChain.length();
    }

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
        return index < length() -1;
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