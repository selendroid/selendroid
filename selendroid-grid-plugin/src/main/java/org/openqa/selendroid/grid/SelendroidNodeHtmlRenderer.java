/*
 * Copyright 2013 selendroid committers.
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
package org.openqa.selendroid.grid;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.internal.utils.HtmlRenderer;
import org.openqa.grid.web.utils.BrowserNameUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Selendroid node renderer used in the Selenium Grid hub.
 * 
 * @author ddary
 * 
 */
public class SelendroidNodeHtmlRenderer implements HtmlRenderer {
  public static final String BROWSER_NAME = "selendroid";
  public static final String LOCALE = "locale";
  public static final String SDK_VERSION = "sdkVersion";
  public static final String AUT = "aut";

  private SelendroidSessionProxy proxy;

  SelendroidNodeHtmlRenderer(SelendroidSessionProxy proxy) {
    this.proxy = proxy;
  }

  public String renderSummary() {
    StringBuilder builder = new StringBuilder();
    builder.append("<fieldset>");

    builder
        .append("<legend>")
        .append(
            "<img width='30' src='/grid/resources/images/mac.png' style='vertical-align:middle;' title='"
                + proxy.getClass().getSimpleName() + "'/>")
        .append(proxy.getClass().getSimpleName()).append("</legend>");

    builder.append("<div id='browsers'>");
    for (TestSlot slot : proxy.getTestSlots()) {
      builder.append("<a href='#' ");
      builder.append(" title='").append(slot.getCapabilities()).append("' ");
      builder.append(" >");

      String icon = null;

      if (BROWSER_NAME.equals(slot.getCapabilities().get(RegistrationRequest.BROWSER))) {
        icon = "android";
      } else {
        icon =
            BrowserNameUtils.consoleIconName(new DesiredCapabilities(slot.getCapabilities()),
                proxy.getRegistry());
      }

      builder.append("<img src='/grid/resources/images/" + icon + ".png' height='20px' ");

      builder.append("</a>");
    }

    builder.append("</div>");
    builder.append("</fieldset>");
    return builder.toString();
  }
}
