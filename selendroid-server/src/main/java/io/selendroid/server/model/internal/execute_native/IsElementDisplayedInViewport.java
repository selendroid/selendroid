/*
 * Copyright 2014 eBay Software Foundation and selendroid committers.
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

import io.selendroid.ServerInstrumentation;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.server.model.AndroidNativeElement;
import io.selendroid.server.model.KnownElements;
import io.selendroid.util.SelendroidLogger;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class IsElementDisplayedInViewport implements NativeExecuteScript {
  private KnownElements knownElements;
  private ServerInstrumentation instrumentation;

  public IsElementDisplayedInViewport(KnownElements knownElements,
      ServerInstrumentation instrumentation) {
    this.knownElements = knownElements;
    this.instrumentation = instrumentation;
  }

  @Override
  public Object executeScript(JSONArray args) {
    SelendroidLogger.info("executing script isElementDisplayedInViewport");

    try {
      String elementId = args.getJSONObject(0).getString("ELEMENT");
      AndroidElement element = knownElements.get(elementId);
      if (element instanceof AndroidNativeElement) {
        return isDisplayedOnViewport(((AndroidNativeElement) element).getView());
      }
      return false;
    } catch (JSONException e) {
      e.printStackTrace();
      return false;
    }
  }

  @SuppressWarnings("deprecation")
  public boolean isDisplayedOnViewport(View view) {
    int coordinates[] = {-1, -1};
    int width = 0, height = 0;

    view.getLocationOnScreen(coordinates);
    if (coordinates[0] + view.getWidth() < 0) return false;
    if (coordinates[1] + view.getHeight() < 0) return false;

    if (width == 0 || height == 0) {
      if (instrumentation.getContext() == null) return false;
      Display display =
          ((WindowManager) instrumentation.getContext().getSystemService(Context.WINDOW_SERVICE))
              .getDefaultDisplay();
      try {
        android.graphics.Point screenSize = new android.graphics.Point();
        display.getSize(screenSize);
        width = screenSize.x;
        height = screenSize.y;
      } catch (NoSuchMethodError e) {
        width = display.getWidth();
        height = display.getHeight();
      }
    }

    if (coordinates[0] > width) return false;
    if (coordinates[1] > height) return false;

    return true;
  }
}
