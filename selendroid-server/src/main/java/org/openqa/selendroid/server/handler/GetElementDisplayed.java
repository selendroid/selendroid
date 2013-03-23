package org.openqa.selendroid.server.handler;

import org.json.JSONException;
import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.webbitserver.HttpRequest;

public class GetElementDisplayed extends RequestHandler {

  public GetElementDisplayed(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    System.out.println("is element displayed command");
    Long id = getElementId();

    AndroidElement element = getElementFromCache(id);
    if (element == null) {
      return new Response(getSessionId(), 7, new SelendroidException("Element with id '" + id
          + "' was not found."));
    }
    return new Response(getSessionId(), element.isDisplayed());
  }
}
