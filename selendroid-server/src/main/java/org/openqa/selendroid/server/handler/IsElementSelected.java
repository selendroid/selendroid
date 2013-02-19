package org.openqa.selendroid.server.handler;

import org.openqa.selendroid.server.RequestHandler;
import org.openqa.selendroid.server.Response;
import org.openqa.selendroid.server.model.AndroidElement;
import org.webbitserver.HttpRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class IsElementSelected extends RequestHandler {

  public IsElementSelected(HttpRequest request) {
    super(request);
  }

  @Override
  public Response handle() {
    System.out.println("is element selected command");
    Long id = getElementId();

    AndroidElement element = getElementFromCache(id);
    return new Response(getSessionId(), new JsonPrimitive(element.isSelected()));
  }



}
