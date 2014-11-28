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
package io.selendroid.server.model.internal.execute_native;

import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.model.AndroidRElement;
import io.selendroid.server.model.Session;
import io.selendroid.server.util.SelendroidLogger;

import org.json.JSONArray;
import org.json.JSONObject;
/**
 * 
 * @deprecated Please use new implemented extension mechanism
 * @see <a href="https://github.com/selendroid/selendroid-extension">extension mechanism Docu</a>
 */
public class InvokeMenuAction implements NativeExecuteScript {

  private Session session;
  private ServerInstrumentation serverInstrumentation;

  public InvokeMenuAction(Session session, ServerInstrumentation serverInstrumentation) {
    this.session = session;
    this.serverInstrumentation = serverInstrumentation;
  }

  @Override
  public Object executeScript(JSONArray args) {
    Integer id = null;
    try {
      if (args.get(0) instanceof JSONObject) {
        id = ((AndroidRElement)session.getKnownElements().get(args.getJSONObject(0).getString("ELEMENT"))).id;
      } else {
        // assume an integer
        id = args.getInt(0);
      }

    } catch (Exception e) {
      SelendroidLogger.error("Cannot invoke menu action", e);
      return "Must pass an AndroidRElement or integer to invokeMenuActionSync (check adb log for full stacktrace): " + e.getMessage();
    }
    serverInstrumentation.invokeMenuActionSync(serverInstrumentation.getCurrentActivity(), id, 0);
    return "invoked";
  }
}
