package org.openqa.selendroid.server.internal;

import org.openqa.selendroid.server.AndroidServlet;
import org.openqa.selendroid.server.handlers.SessionAndIdExtractionTestHandler;
import org.openqa.selendroid.server.handlers.SessionAndPayloadExtractionTestHandler;

public class AndroidTestServlet extends AndroidServlet {
  public AndroidTestServlet() {
    super(null);
  }

  @Override
  protected void init() {
    getHandler.put("/wd/hub/session/:sessionId/element",
        SessionAndPayloadExtractionTestHandler.class);
    postHandler.put("/wd/hub/session/:sessionId/element/:id/click",
        SessionAndIdExtractionTestHandler.class);
  }

}
