/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.server.common.inspector;

import io.selendroid.server.common.http.HttpRequest;

import org.json.JSONException;

public abstract class BaseInspectorViewRenderer {
  public String buildHtml(HttpRequest request) throws JSONException {
    StringBuilder b = new StringBuilder();

    appendLine(b, "<!DOCTYPE html>");
    appendLine(b, "<html>");
    appendLine(b, "<head>");
    appendLine(b, "<title>Selendroid Inspector</title>");
    appendLine(b, "<link rel=\"stylesheet\" href='" + getResource("inspector.css")
        + "' type='text/css'/>");
    appendLine(b, "<link rel=\"stylesheet\" href='" + getResource("ide.css")
        + "' type='text/css'/>");
    appendLine(b, "<link rel=\"stylesheet\" href='" + getResource("jquery.layout.css")
        + "' type='text/css'/>");
    appendLine(b, "<link rel=\"stylesheet\" href='" + getResource("jquery-ui.css")
        + "' type='text/css'/>");
    appendLine(b, "<link rel=\"stylesheet\" href='" + getResource("prettify.css")
        + "' type='text/css'/>");
    appendLine(b, "<script type='text/javascript' src='" + getResource("jquery-1.9.1.js")
        + "'></script>");
    appendLine(b, "<script type='text/javascript' src='" + getResource("jquery-ui-1.10.2.min.js")
        + "'></script>");
    appendLine(b, "<script type='text/javascript' src='" + getResource("inspector1.js")
        + "'></script>");
    appendLine(b, "<script type='text/javascript' src='" + getResource("jquery.jstree.js")
        + "'></script>");
    appendLine(b, "<script type='text/javascript' src='" + getResource("jquery.xpath.js")
        + "'></script>");
    appendLine(b, "<script type='text/javascript' src='" + getResource("prettify.js")
        + "'></script>");
    appendLine(b, "<script type='text/javascript' src='" + getResource("Logger.js") + "'></script>");
    appendLine(b, "<script type='text/javascript' src='" + getResource("Recorder.js")
        + "'></script>");
    appendLine(b, "<script type='text/javascript' src='" + getResource("inspector.js")
        + "'></script>");
    appendLine(b, "<script type='text/javascript' src='" + getResource("ide.js") + "'></script>");
    appendLine(b, "<script type='text/javascript' src='" + getResource("uiactions.js")
        + "'></script>");
    appendLine(b, "<script type='text/javascript' src='" + getResource("jquery.layout1.3.js")
        + "'></script>");
    appendLine(b, "<script>");
    appendLine(b, "jQuery(window).load(function () {");
    appendLine(b, "  console.log('before calling resize()');");
    appendLine(b, "  resize();");
    appendLine(b, "});");
    appendLine(b, "</script>");

    appendLine(b, "</head>");
    appendLine(b, "<body>");
    appendLine(b, "<div id='header'>");
    appendLine(
        b,
        "<div><a target=\"_blank\" href='http://selendroid.io'><img id='logo' src=\""
            + getResource("images/selendroid-logo.png")
            + "\"></a><div id='logo'> Inspector shortcuts: <em>CTRL</em> locks tree element selection, <em>ESC</em> opens XPath helper</div></div>");
    appendLine(b, "<div style=\"display: none\"><input type='checkbox' id='record'/>");
    appendLine(b, "<label for='record' id='record_text'>Record</label></div>");

    appendLine(b, "</div>");

    appendLine(b, "<div id='content' style='height: 750px'>");
    appendLine(b, "<div class='ui-layout-center'>");
    appendLine(b, "<div class='ui-layout-west' id='device'>");
    appendLine(b, "<div id='simulator'>");

    appendLine(b, "<div id='rotationCenter'>");
    appendLine(b, "<div id='mouseOver'></div>");
    appendLine(b, "<div id='frame'>");
    // appendLine(b, "<img src='"getFrame()"'/>");

    appendLine(b, "<div id='screen'>");
    appendLine(b, "<img id='screenshot' src='" + getScreen(request) + "\' width='320px'/>");
    appendLine(b, "</div>");
    appendLine(b, "</div>");
    appendLine(b, "</div>");
    appendLine(b, "</div>");
    appendLine(b, "</div>");
    appendLine(b, "<div class='ui-layout-center'>");
    appendLine(b, "    <div id='tree'></div>");
    appendLine(b, "</div>");
    appendLine(b, "<div class='ui-layout-east'>");
    appendLine(b, "    <div id='details'></div>");
    appendLine(b, "</div>");

    appendLine(b, "</div>");
    appendLine(b, "<div class='ui-layout-south'>");
    appendLine(b, "<ul>");
    appendLine(b, "<li><a href='#java'>Java</a></li>");
    appendLine(b, "<li><a href='#htmlSource'>Html Source</a></li>");
    appendLine(b, "</ul>");

    appendLine(b, "<div class='ui-layout-content ui-widget-content'>");
    appendLine(b, "<pre id='java' class='prettyprint' width='100%'></pre>");
    appendLine(b, "<pre id='htmlSource' class='prettyprint' width='100%'></pre>");
    appendLine(b, "</div>");
    appendLine(b, "</div>");

    appendLine(b, "</div>");
    appendLine(b, "</div>");
    appendLine(b, "<div id='xpathHelper' title='Xpather helper'>");
    appendLine(b, "<input type='text' value='' id='xpathInput'/>");

    appendLine(b, "<div id='xpathLog'> log</div>");
    appendLine(b, "</div>");

    appendLine(b, "<div id='footer'>");
    appendLine(b,
        "<div>&copy; 2012 - 2014 <a href=\"http://ebayopensource.github.io/\" target=\"_blank\"><img src=\""
            + getResource("images/ebay-logo.png") + "\"> software foundation</a> | ");
    appendLine(
        b,
        "Maintained by <a href=\"https://github.com/selendroid/selendroid/blob/master/AUTHORS\" target=\"_blank\">Selendroid Authors</a> | ");
    appendLine(b, "<a href=\"http://groups.google.com/group/selendroid\" target=\"_blank\">Mailing List</a> | ");
    appendLine(
        b,
        "<a href=\"https://github.com/selendroid/selendroid/issues?direction=desc&amp;sort=updated&amp;state=open\" target=\"_blank\">Issue Tracker</a></div>");
    appendLine(b, "</div>");
    appendLine(b, "<script >configure('iphone','Regular','PORTRAIT');</script>");
    appendLine(b, "</body>");
    appendLine(b, "</html>");

    return b.toString();
  }


  private void appendLine(StringBuilder buffer, String line) {
    buffer.append(line + System.getProperty("line.separator"));
  }

  public abstract String getResource(String name);

  public abstract String getScreen(HttpRequest request);

  private String getFrame() {
    return getResource("frameNexus4.png");
  }
}
