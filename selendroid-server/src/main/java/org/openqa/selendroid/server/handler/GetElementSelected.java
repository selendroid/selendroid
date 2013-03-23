package org.openqa.selendroid.server.handler;

import org.json.JSONException;
import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.webbitserver.HttpRequest;

public class GetElementSelected extends RequestHandler {

  public GetElementSelected(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    System.out.println("is element selected command");
    Long id = getElementId();

    AndroidElement element = getElementFromCache(id);
    if (element == null) {
      return new Response(getSessionId(), 7, new SelendroidException("Element with id '" + id
          + "' was not found."));
    }
    boolean selected=element.isSelected();
    return new Response(getSessionId(), selected);
  }
}
