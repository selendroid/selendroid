package io.selendroid.server.handler;

import java.util.ArrayList;
import java.util.List;

import io.selendroid.server.action.ActionContext;
import io.selendroid.server.action.ActionHandler;
import io.selendroid.server.action.ActionChain;
import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.action.touch.TouchActionName;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.util.SelendroidLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 *   Executes action chains in parallel according to selenium specification found here:
 *   https://w3c.github.io/webdriver/webdriver-spec.html#parallel-actions-1
 */
public class Actions extends SafeRequestHandler {

  public Actions(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response safeHandle(HttpRequest request) throws JSONException {
    SelendroidLogger.info("Got actions request");
    JSONArray payload = getPayload(request).getJSONArray("payload");
    int actionChainCount = payload.length();
    boolean stillRunning = true;
    int longestPause;

    List<ActionChain> actionChains = new ArrayList<ActionChain>();
    for (int i = 0; i < actionChainCount; i++) {
      actionChains.add(new ActionChain(payload.getJSONObject(i)));
    }

    while (stillRunning) {
      longestPause = 0;
      stillRunning = false;
      for (ActionChain chain : actionChains) {
        if (chain.hasNext()) {
          stillRunning = true;
          JSONObject action = chain.next();
          String actionName = action.getString("name");

          SelendroidLogger.info("Performing action " + chain.getInputDevice() + "/" + actionName);

          if (actionName.equals(TouchActionName.PAUSE)) {
            if (chain.getPauseTime() > longestPause) {
              longestPause = chain.getPauseTime();
            }
          } else {
            ActionHandler handler = ActionHandler.getHandlerForInputDevice(chain.getInputDevice());
            handler.handle(actionName, getSelendroidDriver(request), action, chain.getContext());
            // POINTER_CANCEL cancels all actions, so all contexts must be released.
            if (actionName.equals(TouchActionName.POINTER_CANCEL)) {
              for (ActionChain c : actionChains) {
                if(c.getContext().getIsPressed())
                  c.getContext().release();
              }
            }
          }
        }
      }

      // Sleep for the longest pause for this tick
      if (longestPause > 0) {
        try {
          Thread.sleep(longestPause);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }

    return new SelendroidResponse(getSessionId(request), "");
  }

}
