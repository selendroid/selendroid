/*
 * Copyright 2013-2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.client;

import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.http.HttpMethod;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SelendroidCommandExecutor extends HttpCommandExecutor {
  private final static String VENDOR_PREFIX = "/session/:sessionId/selendroid/";
  private final static Map<String, CommandInfo> SELENDROID_COMMANDS =
      new HashMap<String, CommandInfo>() {
        {
          // TODO remove network connection once 2.42 is released for Selenium
          put("getNetworkConnection", new CommandInfo("/session/:sessionId/network_connection",
              HttpMethod.GET));
          put("setNetworkConnection", new CommandInfo("/session/:sessionId/network_connection",
                  HttpMethod.POST));
          put("actions", new CommandInfo("/session/:sessionId/actions", HttpMethod.POST));
          // trackball command, should be part of Selenium mouse API, but is not implemented as of now.
          put("roll", new CommandInfo("/session/:sessionId/trackball/roll", HttpMethod.POST));


          put("selendroid-getBrightness", newVendorCommand("screen/brightness", HttpMethod.GET));
          put("selendroid-setBrightness", newVendorCommand("screen/brightness", HttpMethod.POST));

          put("selendroid-getCommandConfiguration",
                  newVendorCommand("configure/command/:command", HttpMethod.GET));
          put("selendroid-setCommandConfiguration",
                  newVendorCommand("configure/command/:command", HttpMethod.POST));

          put("selendroid-adb-sendKeyEvent",newVendorCommand("adb/sendKeyEvent", HttpMethod.POST));
          put("selendroid-adb-sendText", newVendorCommand("adb/sendText", HttpMethod.POST));
          put("selendroid-adb-tap", newVendorCommand("adb/tap", HttpMethod.POST));
          put("selendroid-adb-executeShellCommand",
                  newVendorCommand("adb/executeShellCommand", HttpMethod.POST));

          put("selendroid-handleByExtension", newVendorCommand("extension", HttpMethod.POST));

          put("backgroundApp", newVendorCommand("background", HttpMethod.POST));
          put("resumeApp", newVendorCommand("resume", HttpMethod.POST));

          put("addCallLog", newVendorCommand("addCallLog", HttpMethod.POST));
          put("readCallLog", newVendorCommand("readCallLog", HttpMethod.POST));

          put("-selendroid-forceGcExplicitly", newVendorCommand("gc", HttpMethod.POST));
          put("-selendroid-setAndroidOsSystemProperty",
                  newVendorCommand("systemProperty", HttpMethod.POST));
        }
      };

  private static CommandInfo newVendorCommand(String path, HttpMethod method) {
    return new CommandInfo(VENDOR_PREFIX + path, method);
  }

  public SelendroidCommandExecutor(URL url) throws MalformedURLException {
    super(SELENDROID_COMMANDS, url);
  }

  public SelendroidCommandExecutor() throws MalformedURLException {
    super(SELENDROID_COMMANDS, null);
  }
}
