/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.server.inspector.view;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.server.inspector.SelendroidInspectorView;
import org.openqa.selendroid.server.model.SelendroidDriver;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.nio.charset.Charset;
import java.util.Iterator;

public class InspectorView extends SelendroidInspectorView {
  public InspectorView(ServerInstrumentation serverInstrumentation, SelendroidDriver driver) {
    super(serverInstrumentation, driver);
  }

  @Override
  public void render(HttpRequest request, HttpResponse response) throws JSONException {
    response.header("Content-Type", "text/html").charset(Charset.forName("UTF-8")).status(200)
        .content(buildHtml()).end();
  }

  protected String buildHtml() throws JSONException {
    StringBuilder b = new StringBuilder();
    b.append("<html>");
    b.append("<head>");
    b.append("<title>Selendroid Inspector</title>");
    b.append("<link rel='stylesheet' href='" + getResource("ide.css") + "'  type='text/css'/>");
    b.append("<script type='text/javascript' src='" + getResource("jquery.min.js") + "'></script>");
    b.append("<script type='text/javascript' src='" + getResource("jquery.jstree.js")
        + "'></script>");
    b.append("<script type='text/javascript' src='" + getResource("ide.js") + "'></script>");
    b.append("<script type='text/javascript' src='" + getResource("uiactions.js") + "'></script>");
    b.append("</head>");
    b.append("<body>");
    b.append("<html>");
    b.append("<div id='simulator'>");
    b.append("<div id ='highlight' ></div>");
    b.append("<div id='mouseOver'></div>");
    b.append("<div id='rotationCenter'>");
    b.append("<div id='frame'>");
    //b.append("<img src='" + getFrame() + "' />");
    b.append("        <div id='screen'>");
    b.append("         <img src='" + getScreen() + "' />");
    b.append("</div>");
    b.append("</div>");
    b.append("</div>");
    b.append("</div>");
    b.append("<div id='details'></div>");
    b.append("<div id='tree'></div>");
    String d = "iphone";
    b.append("<script >configure('" + d + "','UIA_DEVICE_ORIENTATION_PORTRAIT');</script>");
    b.append("<script >resize();</script>");
    b.append("<div id ='topmenu'>");
    b.append("<div id=\"picture\"/>");
    b.append("<img src=\"" + getIcon() + "\" width='40px' />");
    b.append("</div>");
    b.append("<ul>");
    b.append("<li><a target=\"_blank\" href=\"https://github.com/DominikDary/selendroid/wiki\">Selendroid Documentation</a></li>");
    b.append("<li id=\"capabilities\"><a href=\"#\">See Capabilities</a></li>");
    b.append("</ul>");
    b.append("</div>");

    /*
     * OVERLAY Capabilities
     */
    b.append("<div class=\"overlay\" id=\"overlay\" style=\"display:none;\"></div>");

    b.append("<div class=\"box\" id=\"box\">");
    b.append("<a class=\"boxclose\" id=\"boxclose\"></a>");
    b.append("<h4>Capabilities</h4>");
    b.append("<p>");
    b.append(displayCapabilities());
    b.append("</p>");
    b.append("</div>");
    /* END OVERLAY CAPABILITIES */

    /* OVERLAY LANGUAGES */
    b.append("<div class=\"overlaylanguages\" id=\"overlayLanguages\" style=\"display:none;\"></div>");

    b.append("<div class=\"boxlanguages\" id=\"boxlanguages\">");
    b.append("<a class=\"boxcloselanguages\" id=\"boxcloselanguages\"></a>");
    b.append("<h4>Supported Languages</h4>");
    b.append("<p>");
    // b.append(getListOfLanguagesInHTML());
    b.append("</p>");
    b.append("</div>");

    /* END OVERLAY FOR LANGUAGES */
    b.append("</body>");
    b.append("</html>");
    return b.toString();
  }

  private String getResource(String name) {
    return "/inspector/resources/" + name;
  }

  private String getScreen() {
    return getResource(ResourceView.SCREENSHOT);
  }

  private String displayCapabilities() throws JSONException {
    if (driver.getSession() != null) {
      StringBuffer capabilities = new StringBuffer();
      JSONObject capa = driver.getSession().getCapabilities();
      if (capa == null) {
        return "No capabilities available.";
      }

      for (Iterator<String> keyIter = capa.keys(); keyIter.hasNext();) {
        String key = keyIter.next();
        capabilities.append("<p><b>" + key + "</b>: " + capa.get(key) + "</p>");
      }
      return capabilities.toString();
    }
    return "No capabilities available. Model = null";
  }

  private String getFrame() {
    return getResource("frameNexus4.png");
  }

  private String getIcon() {
    return getResource("android.png");
  }

}
