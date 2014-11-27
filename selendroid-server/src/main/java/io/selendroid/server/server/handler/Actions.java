package io.selendroid.server.server.handler;

import java.util.ArrayList;
import java.util.List;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.action.touch.TouchActionName;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.server.SafeRequestHandler;
import io.selendroid.server.server.action.ActionContext;
import io.selendroid.server.server.action.ActionHandler;
import io.selendroid.server.util.SelendroidLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Actions extends SafeRequestHandler {

  public Actions(String mappedUri) {
    super(mappedUri);
  }

  private int longestActionChain(JSONArray actionChainList) throws JSONException {
    int longestActionChain = 0;
    for (int i = 0; i < actionChainList.length(); i++) {
      JSONObject actionChain = actionChainList.getJSONObject(i);
      JSONArray actions = actionChain.getJSONArray("actions");

      int actionCount = actions.length();
      if (actionCount > longestActionChain) {
        longestActionChain = actionCount;
      }
    }

    return longestActionChain;
  }

  private int longestPause(int currentAction, JSONArray payload) throws JSONException {
    int longestPause = 0;

    int actionChainCount = payload.length();
    for (int i = 0; i < actionChainCount; i++) {
      JSONObject actionChain = payload.getJSONObject(i);
      JSONArray actions = actionChain.getJSONArray("actions");
      if (actions.length() > currentAction) {
        JSONObject action = actions.getJSONObject(currentAction);
        String actionName = action.getString("name");

        if (actionName.equals(TouchActionName.PAUSE)) {
          int duration = action.getInt("ms");
          if (duration > longestPause) {
            longestPause = duration;
          }
        }
      }
    }

    return longestPause;
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    SelendroidLogger.info("Got actions request");
    JSONArray payload = getPayload(request).getJSONArray("payload");
    int actionChainCount = payload.length();
    int longestActionChain = longestActionChain(payload);

    // Create contexts for all chains of actions
    List<ActionContext> chainContexts = new ArrayList<ActionContext>();
    for (int i = 0; i < actionChainCount; i++) {
      chainContexts.add(new ActionContext());
    }

    /*
       Action chains are a sequence of actions for a single finger on the touch screen device
       These chains are executed in parallel when only one finger wont suffice,
       chains can be of different lengths so we iterate over the longest one -
       padding out shorter chains with wait commands once they are done.
    */
    for (int i = 0; i < longestActionChain; i++) {
      for (int j = 0; j < actionChainCount; j++) {
        JSONObject actionChain = payload.getJSONObject(j);
        String inputDevice = actionChain.getString("inputDevice");

        JSONArray actions = actionChain.getJSONArray("actions");
        if (actions.length() > i) {
          ActionContext context = chainContexts.get(j);
          JSONObject action = actions.getJSONObject(i);
          String actionName = action.getString("name");

          SelendroidLogger.info("Performing action " + inputDevice + "/" + actionName);

          // Pauses are now handled separately as we only wait for the longest pause.
          if (!actionName.equals(TouchActionName.PAUSE)) {
            ActionHandler handler = ActionHandler.getHandlerForInputDevice(inputDevice);
            handler.handle(actionName, getSelendroidDriver(request), action, context);
          }
        }
      }

      int pauseTime = longestPause(i, payload);
      if (pauseTime > 0) {
        try {
          Thread.sleep(pauseTime);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }

    return new SelendroidResponse(getSessionId(request), "");
  }
}
