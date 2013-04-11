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
package org.openqa.selendroid.server.model.internal.execute_native;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.server.Session;
import org.openqa.selendroid.server.model.AndroidRElement;

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
      e.printStackTrace();
      return "Must pass an AndroidRElement or integer to invokeMenuActionSync (check adb log for full stacktrace): " + e.getMessage();
    }
    serverInstrumentation.invokeMenuActionSync(serverInstrumentation.getCurrentActivity(), id, 0);
    return "invoked";
  }
}
