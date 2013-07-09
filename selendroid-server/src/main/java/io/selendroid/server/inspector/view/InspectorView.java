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
package io.selendroid.server.inspector.view;

import io.selendroid.ServerInstrumentation;
import io.selendroid.server.inspector.InspectorServlet;
import io.selendroid.server.inspector.SelendroidInspectorView;
import io.selendroid.server.model.SelendroidDriver;

import java.nio.charset.Charset;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

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
    b.append("<link rel='stylesheet' href='" + getResource("ide.css") + "' type='text/css'/>");
    b.append("<link rel='stylesheet' href='" + getResource("prettify.css") + "' type='text/css'/>");
    b.append("<script type='text/javascript' src='" + getResource("jquery.min.js") + "'></script>");
    b.append("<link rel=\"stylesheet\" href='" + getResource("jquery-ui.css")  + "' type='text/css'/>");
    b.append("<script type='text/javascript' src='" + getResource("jquery-ui.js") + "'></script>");
    b.append("<script type='text/javascript' src='" + getResource("jquery.jstree.js")   + "'></script>");
    b.append("<script type='text/javascript' src='" + getResource("jquery.xpath.js")   + "'></script>");
    b.append("<script type='text/javascript' src='" + getResource("prettify.js") + "'></script>");
    b.append("<script type='text/javascript' src='" + getResource("Logger.js") + "'></script>");
    b.append("<script type='text/javascript' src='" + getResource("Recorder.js") + "'></script>");
    b.append("<script type='text/javascript' src='" + getResource("inspector.js") + "'></script>");
    b.append("<script type='text/javascript' src='" + getResource("ide.js") + "'></script>");
    b.append("<script type='text/javascript' src='" + getResource("uiactions.js") + "'></script>");

    b.append("<script>");
    b.append("$(document).ready(function () {");
    b.append("  $(\"#tabs\").tabs();");
    b.append("});");
    b.append("</script>");

    b.append("</head>");

    b.append("<body onload=\"prettyPrint()\">");
    b.append("<div id='greyout'></div>");
    b.append("<div id='simulator'>");

    b.append("<div id='mouseOver'></div>");
    b.append(" <div id='rotationCenter'>");

    b.append("<div id='frame'>");
    // b.append("<img src='" + getFrame() + "' />");
    b.append(" <div id='screen'>");

    int width = 320;

    b.append(" <img id='screenshot' src='" + getScreen() + "' width='" + width + "px' />");
    b.append("</div>");
    b.append("</div>");
    b.append("</div>");
    b.append("</div>");

    b.append("<div id='xpathHelper' >Xpath Expression:</br><input type='text' value='' id='xpathInput' /><div id='xpathLog' > log</div></div>");
    b.append("<div id ='detailsparent' >");

    b.append("<div id ='details' ></div>");
    b.append("</div>");

    b.append("<div id ='tree' ></div>");

    String d = "iphone";
    b.append("<script >configure('" + d + "','UIA_DEVICE_ORIENTATION_PORTRAIT');</script>");
    b.append("<script >resize();</script>");
    b.append("<div id ='topmenu'>");
    b.append("<div id=\"picture\"/>");
    String icon = getIcon();
    if (icon != null) {
      b.append("<img src=\"" + getIcon() + "\" width='40px' />");
    }

    b.append("</div>");
    b.append("<ul>");
    //b.append("<li><a target=\"_blank\" href=\"http://selendroid.io\">Selendroid Documentation</a></li>");
    b.append("<li id=\"capabilities\"><a href=\"#\">See Capabilities</a></li>");
    b.append("<li id=\"htmlshow\"><a href=\"#\">See HTML</a></li>");
    b.append("</ul>");
    b.append("</div>");

    b.append(getScriptTabs());

    /*
     * OVERLAY Capabilities
     */
    b.append("<div class=\"overlay\" id=\"overlay\" style=\"display:none;\"></div>");
    b.append("<div class=\"box\" id=\"box\">");
    b.append("<a class=\"boxclose\" id=\"boxclose\"><p class=\"arrow-left\"></p></a>");
    b.append("<h4>Capabilities</h4>");
    b.append("<p>");
    b.append(displayCapabilities());
    b.append("</p>");
     b.append("<h4>Supported Languages</h4>");
     b.append("<p>");
    // b.append(getListOfLanguagesInHTML());
     b.append("</p>");
    b.append("</div>");
    /* END OVERLAY CAPABILITIES */

    /*
     * OVERLAY HTML
     */
    b.append("<div class=\"overlayhtml\" id=\"overlayhtml\" style=\"display:none;\"></div>");
    b.append("<div class=\"boxhtml\" id=\"boxhtml\">");
    b.append("<a class=\"boxclosehtml\" id=\"boxclosehtml\"><p class=\"arrow-right-html\"></p></a>");
    b.append("<h4>Web Inspector</h4>");
    b.append("<iframe id=\"webinspector\" src=\"/inspector/latestWebView\"></iframe> ");
    b.append("</div>");
    /* END OVERLAY HTML */

    b.append("</body>");
    b.append("</html>");

    return b.toString();
  }


  private String getListOfLanguagesInHTML() throws Exception {
    JSONObject jsonApp = getAppFromStatus();
    JSONArray supportedLanguages = (JSONArray) jsonApp.get("supportedLanguages");

    StringBuffer buffer = new StringBuffer();
    buffer.append("<ul>");
    for (int i = 0; i < supportedLanguages.length(); i++) {
      buffer.append("<li>" + supportedLanguages.getString(i) + "</li>");
    }
    buffer.append("<ul>");

    return buffer.toString();
  }

  private JSONObject getStatus() throws Exception {
    JSONObject status = new JSONObject();

    return status;
  }

  private JSONObject getAppFromStatus() throws Exception {
    JSONObject status = getStatus();
    // no info about the app available
    // JSONArray array = status.getJSONObject("value").getJSONArray("supportedApps");
    // for (int i = 0; i < array.length(); i++) {
    // JSONObject jsonApp = array.getJSONObject(i);
    // String other = (String) jsonApp.get("CFBundleIdentifier");
    // String me = (String) model.getCapabilities().getRawCapabilities().get("CFBundleIdentifier");
    // if (other.equals(me)) {
    // return jsonApp;
    // }
    // }
    return null;
  }

  public String getScriptTabs() {
    StringBuilder b = new StringBuilder();
    b.append("<div id=\"tabs\">");
    b.append("<ul>");
    b.append("<li><a href=\"#tabs-java\">Java</a></li>");
    b.append("<li><a href=\"#tabs-raw\">Raw</a></li>");
    b.append("<li><a href=\"#tabs-logs\">Logs</a></li>");
    b.append("</ul>");

    b.append("<div id=\"tabs-java\" class='tab' >");
    b.append("<pre id='java' class=\"prettyprint\"></pre>");

    b.append("</div>");

    b.append("<div id=\"tabs-raw\" class='tab'>");
    b.append("TAB2");
    b.append("</div>");

    b.append("<div id=\"tabs-logs\" class='tab' >");
    b.append("TAB3");
    b.append("</div>");

    return b.toString();
  }

  private String getResource(String name) {
    return InspectorServlet.INSPECTOR_RESSOURCE + "/" + name;
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
