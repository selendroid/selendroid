/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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

import io.selendroid.android.AndroidTouchScreen;
import org.json.JSONArray;
import org.json.JSONObject;

import io.selendroid.android.internal.Point;
import io.selendroid.util.SelendroidLogger;

public class TwoPointerGestureAction implements NativeExecuteScript {

  private AndroidTouchScreen touch;

  public TwoPointerGestureAction(AndroidTouchScreen touch) {
    this.touch = touch;
  }

  @Override
  public Object executeScript(JSONArray args) {
    if (args.length() != 1) {
    	return "Wrong number of arguments";
    }
    try {
      SelendroidLogger.info("TwoPointerGestureAction args = " + args.toString());
      JSONObject arg = args.getJSONObject(0);
      int startPoint1X = Integer.parseInt(arg.getString("startPoint1X"));
      int startPoint1Y = Integer.parseInt(arg.getString("startPoint1Y"));
      int startPoint2X = Integer.parseInt(arg.getString("startPoint2X"));
      int startPoint2Y = Integer.parseInt(arg.getString("startPoint2Y"));
      int endPoint1X = Integer.parseInt(arg.getString("endPoint1X"));
      int endPoint1Y = Integer.parseInt(arg.getString("endPoint1Y"));
      int endPoint2X = Integer.parseInt(arg.getString("endPoint2X"));
      int endPoint2Y = Integer.parseInt(arg.getString("endPoint2Y"));
      int steps = Integer.parseInt(arg.getString("steps"));
      touch.performTwoPointerGesture(
    		  new Point(startPoint1X, startPoint1Y), 
    		  new Point(startPoint2X, startPoint2Y), 
    		  new Point(endPoint1X, endPoint1Y), 
    		  new Point(endPoint2X, endPoint2Y), steps);
     } catch (Exception e) {
      e.printStackTrace();
      return "arguments missing " + e.getMessage();
    }
    return "invoked";
  }
}
