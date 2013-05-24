package io.selendroid.server.handler;

import io.selendroid.server.RequestHandler;
import org.json.JSONException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.StaleElementReferenceException;
import io.selendroid.server.Response;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

public class GetElementSelected extends RequestHandler {

  public GetElementSelected(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    SelendroidLogger.log("is element selected command");
    String id = getElementId();

    AndroidElement element = getElementFromCache(id);
    if (element == null) {
      return new Response(getSessionId(), 10, new SelendroidException("Element with id '" + id
          + "' was not found."));
    }
    boolean selected=element.isSelected();
    try {
      return new Response(getSessionId(), selected);
    } catch (StaleElementReferenceException se) {
      return new Response(getSessionId(), 10, se);
    }
  }
}
