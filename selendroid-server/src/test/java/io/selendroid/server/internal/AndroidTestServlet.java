/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
