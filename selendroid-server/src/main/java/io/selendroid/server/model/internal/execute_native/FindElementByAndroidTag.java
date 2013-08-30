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

import io.selendroid.ServerInstrumentation;
import io.selendroid.android.ViewHierarchyAnalyzer;
import io.selendroid.server.model.AndroidNativeElement;
import io.selendroid.server.model.KnownElements;
import io.selendroid.util.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;

/**
 * This class contains the functionality to find an Android view with the tag name assigned to it
 * sample usage : WebElement element = (WebElement)webDriver.executeScript("findElementByAndroidTag", "view_test_tag");
 *
 */

public class FindElementByAndroidTag implements NativeExecuteScript {

  private ServerInstrumentation serverInstrumentation;
  protected ViewHierarchyAnalyzer viewAnalyzer;
  private KnownElements knownElements;

  public FindElementByAndroidTag(KnownElements knownElements, ServerInstrumentation serverInstrumentation) {
    this.serverInstrumentation = serverInstrumentation;
    this.knownElements = knownElements;
    this.viewAnalyzer = ViewHierarchyAnalyzer.getDefaultInstance();
  }
  

  @Override
  public Object executeScript(JSONArray args) {
	  String tagName = null;
	  JSONObject result = new JSONObject();
	  try {
		  tagName = args.getString(0);
		  Collection<View> currentViews = viewAnalyzer.getViews(getTopLevelViews());
		  for (View view : currentViews) {
				  Object tag = view.getTag();
				  if (tag!=null && tag.toString().equalsIgnoreCase(tagName)) {
					  AndroidNativeElement element =  newAndroidElement(view);
			    	  result.put("ELEMENT", knownElements.getIdOfElement(element));
			  }
		  }
	    } catch (JSONException e) {
	      e.printStackTrace();
	    }
	  return result;
  }



private AndroidNativeElement newAndroidElement(View view) {
    Preconditions.checkNotNull(view);
    if (knownElements.hasElement(new Long(view.getId()))) {
      AndroidNativeElement element =
          (AndroidNativeElement) knownElements.get(new Long(view.getId()));
      if (element.getView().equals(view)) {
        return element;
      }
    }
    AndroidNativeElement e = new AndroidNativeElement(view, serverInstrumentation, knownElements);
    knownElements.add(e);
    return e;
  }

protected List<View> getTopLevelViews() {
    List<View> views = new ArrayList<View>();
    views.addAll(viewAnalyzer.getTopLevelViews());
    if (serverInstrumentation.getCurrentActivity() != null
        && serverInstrumentation.getCurrentActivity().getCurrentFocus() != null) {
      views.add(serverInstrumentation.getCurrentActivity().getCurrentFocus());
    }
    return views;
  }


}