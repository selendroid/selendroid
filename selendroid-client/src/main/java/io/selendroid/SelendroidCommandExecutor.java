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
package io.selendroid;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.HttpVerb;

public class SelendroidCommandExecutor extends HttpCommandExecutor {

  private final static Map<String, CommandInfo> SELENDROID_COMMANDS =
      new HashMap<String, CommandInfo>() {
        {
          put("selendroid-getBrightness", new CommandInfo(
              "-selendroid/:sessionId/screen/brightness", HttpVerb.GET));
          put("selendroid-setBrightness", new CommandInfo(
              "-selendroid/:sessionId/screen/brightness", HttpVerb.POST));
          put("selendroid-getCommandConfiguration", new CommandInfo(
              "-selendroid/:sessionId/configure/command/:command", HttpVerb.GET));
          put("selendroid-setCommandConfiguration", new CommandInfo(
              "-selendroid/:sessionId/configure/command/:command", HttpVerb.POST));
          put("selendroid-adb-sendKeyEvent", new CommandInfo(
              "-selendroid/:sessionId/adb/sendKeyEvent", HttpVerb.POST));
          put("selendroid-adb-sendText", new CommandInfo("-selendroid/:sessionId/adb/sendText",
              HttpVerb.POST));
          put("selendroid-adb-tap",
              new CommandInfo("-selendroid/:sessionId/adb/tap", HttpVerb.POST));
        }
      };

  public SelendroidCommandExecutor(URL url) throws MalformedURLException {
    super(SELENDROID_COMMANDS, url);
  }

  public SelendroidCommandExecutor() throws MalformedURLException {
    super(SELENDROID_COMMANDS, (URL) null);
  }
}
