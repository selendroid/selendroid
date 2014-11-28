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
package io.selendroid.server.inspector;

import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.server.common.http.HttpResponse;
import io.selendroid.server.model.SelendroidDriver;

import org.json.JSONException;


public abstract class SelendroidInspectorView {
  protected ServerInstrumentation serverInstrumentation = null;
  protected SelendroidDriver driver = null;

  public SelendroidInspectorView(ServerInstrumentation serverInstrumentation,
      SelendroidDriver driver) {
    this.serverInstrumentation = serverInstrumentation;
    if (driver == null) {
      throw new RuntimeException("driver ==null");
    }
    this.driver = driver;
  }

  public abstract void render(HttpRequest request, HttpResponse response) throws JSONException;
}
