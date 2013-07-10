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
    appendLine(b,"<html>");
    appendLine(b,"<head>");
    appendLine(b,"<title>Selendroid Inspector</title>");
    appendLine(b,"<link rel='stylesheet' href='" + getResource("ide.css") + "' type='text/css'/>");
    appendLine(b,"<link rel='stylesheet' href='" + getResource("prettify.css") + "' type='text/css'/>");
    appendLine(b,"<script type='text/javascript' src='" + getResource("jquery.min.js") + "'></script>");
    appendLine(b,"<link rel=\"stylesheet\" href='" + getResource("jquery-ui.css")
        + "' type='text/css'/>");
    appendLine(b,"<script type='text/javascript' src='" + getResource("jquery-ui.js") + "'></script>");
    appendLine(b,"<script type='text/javascript' src='" + getResource("jquery.jstree.js")
        + "'></script>");
    appendLine(b,"<script type='text/javascript' src='" + getResource("jquery.xpath.js")
        + "'></script>");
    appendLine(b,"<script type='text/javascript' src='" + getResource("imgscale.jquery.min.js") + "'></script>");
    appendLine(b,"<script type='text/javascript' src='" + getResource("prettify.js") + "'></script>");
    appendLine(b,"<script type='text/javascript' src='" + getResource("Logger.js") + "'></script>");
    appendLine(b,"<script type='text/javascript' src='" + getResource("Recorder.js") + "'></script>");
    appendLine(b,"<script type='text/javascript' src='" + getResource("inspector.js") + "'></script>");
    appendLine(b,"<script type='text/javascript' src='" + getResource("ide.js") + "'></script>");
    appendLine(b,"<script type='text/javascript' src='" + getResource("uiactions.js") + "'></script>");
    

    appendLine(b,"<script>");
    appendLine(b,"$(document).ready(function () {");
    appendLine(b,"  $(\"#tabs\").tabs();");
    appendLine(b,"});");
    appendLine(b,"</script>");

    appendLine(b,"</head>");

    appendLine(b,"<body onload=\"prettyPrint()\">");
    appendLine(b,"<div id='greyout'></div>");
    appendLine(b,"<div id='simulator'>");

    appendLine(b,"<div id='mouseOver'></div>");
    appendLine(b," <div id='rotationCenter'>");

    appendLine(b,"<div id='frame'>");
    // appendLine(b,"<img src='" + getFrame() + "' />");
    appendLine(b," <div id='screen'>");

    appendLine(b," <img id='screenshot' src='" + getScreen() + "\'/>");
    appendLine(b,"</div>");
    appendLine(b,"</div>");
    appendLine(b,"</div>");
    appendLine(b,"</div>");

    appendLine(b,"<div id='xpathHelper' >Xpath Expression:</br><input type='text' value='' id='xpathInput' /><div id='xpathLog' > log</div></div>");
    appendLine(b,"<div id ='detailsparent' >");

    appendLine(b,"<div id ='details' ></div>");
    appendLine(b,"</div>");

    appendLine(b,"<div id ='tree' ></div>");

    String d = "iphone";
    appendLine(b,"<script >configure('" + d + "','UIA_DEVICE_ORIENTATION_PORTRAIT');</script>");
    appendLine(b,"<script >resize();</script>");
    appendLine(b,"<div id ='topmenu'>");
    appendLine(b,"<div id=\"picture\"/>");
    String icon = getIcon();
    if (icon != null) {
      appendLine(b,"<img src=\"" + getIcon() + "\" width='40px' />");
    }

    appendLine(b,"</div>");
    appendLine(b,"<ul>");
    // appendLine(b,"<li><a target=\"_blank\" href=\"http://selendroid.io\">Selendroid Documentation</a></li>");
    appendLine(b,"<li id=\"capabilities\"><a href=\"#\">See Capabilities</a></li>");
    appendLine(b,"<li id=\"htmlshow\"><a href=\"#\">See HTML</a></li>");
    appendLine(b,"</ul>");
    appendLine(b,"</div>");

    appendLine(b,getScriptTabs());

    /*
     * OVERLAY Capabilities
     */
    appendLine(b,"<div class=\"overlay\" id=\"overlay\" style=\"display:none;\"></div>");
    appendLine(b,"<div class=\"box\" id=\"box\">");
    appendLine(b,"<a class=\"boxclose\" id=\"boxclose\"><p class=\"arrow-left\"></p></a>");
    appendLine(b,"<h4>Capabilities</h4>");
    appendLine(b,"<p>");
    appendLine(b,displayCapabilities());
    appendLine(b,"</p>");
    appendLine(b,"<h4>Supported Languages</h4>");
    appendLine(b,"<p>");
    // appendLine(b,getListOfLanguagesInHTML());
    appendLine(b,"</p>");
    appendLine(b,"</div>");
    /* END OVERLAY CAPABILITIES */

    /*
     * OVERLAY HTML
     */
    appendLine(b,"<div class=\"overlayhtml\" id=\"overlayhtml\" style=\"display:none;\"></div>");
    appendLine(b,"<div class=\"boxhtml\" id=\"boxhtml\">");
    appendLine(b,"<a class=\"boxclosehtml\" id=\"boxclosehtml\"><p class=\"arrow-right-html\"></p></a>");
    appendLine(b,"<h4>Web Inspector</h4>");
    appendLine(b,"<iframe id=\"webinspector\" src=\"/inspector/latestWebView\"></iframe> ");
    appendLine(b,"</div>");
    /* END OVERLAY HTML */

    appendLine(b,"</body>");
    appendLine(b,"</html>");

    return b.toString();
  }

  private void appendLine(StringBuilder buffer, String line) {
    buffer.append(line + System.getProperty("line.separator"));
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
    appendLine(b,"<div id=\"tabs\">");
    appendLine(b,"<ul>");
    appendLine(b,"<li><a href=\"#tabs-java\">Java</a></li>");
    appendLine(b,"<li><a href=\"#tabs-raw\">Raw</a></li>");
    appendLine(b,"<li><a href=\"#tabs-logs\">Logs</a></li>");
    appendLine(b,"</ul>");

    appendLine(b,"<div id=\"tabs-java\" class='tab' >");
    appendLine(b,"<pre id='java' class=\"prettyprint\"></pre>");

    appendLine(b,"</div>");

    appendLine(b,"<div id=\"tabs-raw\" class='tab'>");
    appendLine(b,"TAB2");
    appendLine(b,"</div>");

    appendLine(b,"<div id=\"tabs-logs\" class='tab' >");
    appendLine(b,"TAB3");
    appendLine(b,"</div>");

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
