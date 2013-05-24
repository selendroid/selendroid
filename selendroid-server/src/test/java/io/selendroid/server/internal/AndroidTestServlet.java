package io.selendroid.server.internal;

import io.selendroid.server.AndroidServlet;
import io.selendroid.server.handlers.SessionAndIdExtractionTestHandler;
import io.selendroid.server.handlers.SessionAndPayloadExtractionTestHandler;

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
