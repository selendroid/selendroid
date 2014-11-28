package io.selendroid.server.action;

import io.selendroid.server.android.internal.Point;
import io.selendroid.server.common.exceptions.StaleElementReferenceException;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.server.model.KnownElements;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.server.util.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Action {

  protected SelendroidDriver driver;

  public Action(SelendroidDriver driver) {
    this.driver = driver;
  }

  protected AndroidElement getElementFromCache(String id) {
    KnownElements knownElements = getKnownElements();
    if (knownElements == null || knownElements.get(id) == null) {
      throw new StaleElementReferenceException(
          "The element with id '" + id + "' was not found.");
    }
    return knownElements.get(id);
  }

  protected KnownElements getKnownElements() {
    if (driver.getSession() == null) {
      return null;
    }
    return driver.getSession().getKnownElements();
  }

  protected Point getActionPosition(JSONObject properties) {
    Integer x = (Integer) properties.opt("x");
    Integer y = (Integer) properties.opt("y");
    String elementId = (String) properties.opt("element");

    //Check the request either has an element or coordinates
    Preconditions.checkState(elementId != null || (x != null && y != null));

    Point elementLocation = (elementId == null)
        ? new Point(0, 0) : getElementFromCache(elementId).getLocation();

    if (x != null && y != null) {
      return new Point(elementLocation.getX() + x, elementLocation.getY() + y);
    }

    return elementLocation;
  }

  public abstract void perform(JSONObject properties, ActionContext context) throws JSONException;
}
