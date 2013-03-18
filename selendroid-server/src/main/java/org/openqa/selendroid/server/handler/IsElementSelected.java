package org.openqa.selendroid.server.handler;

import org.json.JSONException;
import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.openqa.selendroid.server.model.AndroidElement;
import org.webbitserver.HttpRequest;

public class IsElementSelected extends RequestHandler {

  public IsElementSelected(HttpRequest request,String mappedUri) {
    super(request,mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    System.out.println("is element selected command");
    Long id = getElementId();

    AndroidElement element = getElementFromCache(id);
    
    return new Response(getSessionId(), element.isSelected());
  }
}
