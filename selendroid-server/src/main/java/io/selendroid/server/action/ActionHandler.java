package io.selendroid.server.action;

import io.selendroid.server.action.touch.TouchHandler;
import io.selendroid.server.common.exceptions.UnsupportedOperationException;
import io.selendroid.server.model.SelendroidDriver;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public abstract class ActionHandler {

  protected Map<String, Class<? extends Action>> actionMap = new HashMap<String, Class<? extends Action>>();

  private static Map<String, ActionHandler> handlerMap = new HashMap<String, ActionHandler>();

  static {
    handlerMap.put("touch", new TouchHandler());
  }

  protected ActionHandler() {
    init();
  }

  public abstract void init();

  public void register(String actionName, Class<? extends Action> actionClass) {
    actionMap.put(actionName, actionClass);
  }

  public void handle(String actionName, SelendroidDriver driver, JSONObject properties,
      ActionContext context) throws JSONException {
    Class<? extends Action> actionClass = actionMap.get(actionName);
    try {
      Action action = actionClass
          .getDeclaredConstructor(SelendroidDriver.class)
          .newInstance(driver);
      action.perform(properties, context);
    } catch (IllegalAccessException e) {
      throw new UnsupportedOperationException("Action: " + actionName + " was not recognised.");
    } catch (IllegalArgumentException e) {
      throw new UnsupportedOperationException("Action: " + actionName + " was not recognised.");
    } catch (InstantiationException e) {
      throw new UnsupportedOperationException("Action: " + actionName + " was not recognised.");
    } catch (NoSuchMethodException e) {
      throw new UnsupportedOperationException("Action: " + actionName + " was not recognised.");
    } catch (InvocationTargetException e) {
      throw new UnsupportedOperationException("Action: " + actionName + " was not recognised.");
    }
  }

  public static ActionHandler getHandlerForInputDevice(String inputDevice) {
    ActionHandler handler = handlerMap.get(inputDevice);
    if (handler == null) {
      throw new UnsupportedOperationException("Unreognised input device: " + inputDevice);
    }

    return handler;
  }
}
