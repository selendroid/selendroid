/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.server.model;

import android.view.View;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.UUID;

public class KnownElements {
  private final BiMap<String, AndroidElement> cache = HashBiMap.create();
  private final BiMap<View, AndroidNativeElement> nativeElementsByView = HashBiMap.create();

  public String add(AndroidElement element) {
    if (cache.containsValue(element)) {
      return cache.inverse().get(element);
    }
    String id;
    if (element instanceof AndroidNativeElement &&
        ((AndroidNativeElement)element).getView().getId() >= 0) {
      id = new Long(((AndroidNativeElement)element).getView().getId()).toString();
    } else {
      id = UUID.randomUUID().toString();
    }

    cache.put(id, element);
    if(element instanceof AndroidNativeElement){
      AndroidNativeElement nativeElement=(AndroidNativeElement)element;
      nativeElementsByView.put(nativeElement.getView(), nativeElement);
    }
    return id;
  }

  /**
   * Uses the generated Id to look up elements
   */
  public AndroidElement get(String elementId) {
    return cache.get(elementId);
  }

  public AndroidElement get(Long elementId) {
    return get(elementId.toString());
  }

  /**
   * Uses the generated Id to look up elements
   */
  public boolean hasElement(String elementId) {
    return cache.containsKey(elementId);
  }

  public boolean hasElement(Long elementId) {
    return hasElement(elementId.toString());
  }
  
  public AndroidNativeElement getNativeElement(View view) {
    return nativeElementsByView.get(view);
  }

  public boolean hasNativeElement(View view) {
    return nativeElementsByView.containsKey(view);
  }

  public String getIdOfElement(AndroidElement element) {
    if (cache.containsValue(element)) {
      return cache.inverse().get(element);
    }
    return null;
  }

  public void clear() {
    cache.clear();
    nativeElementsByView.clear();
    System.gc();
  }
}
