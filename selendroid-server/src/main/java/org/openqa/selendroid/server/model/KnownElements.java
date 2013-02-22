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

public class KnownElements {
  private final BiMap<Long, AndroidElement> cache = HashBiMap.create();
  private final BiMap<View, AndroidNativeElement> nativeElementsByView = HashBiMap.create();
  private long nextId = 0L;

  public Long add(AndroidElement element) {
    if (cache.containsValue(element)) {
      return cache.inverse().get(element);
    }
    Long id = nextId();

    cache.put(id, element);
    if(element instanceof AndroidNativeElement){
      AndroidNativeElement nativeElement=(AndroidNativeElement)element;
      nativeElementsByView.put(nativeElement.getView(), nativeElement);
    }
    return id;
  }

  public Long nextId() {
    return ++nextId;
  }

  /**
   * Uses the generated Id {@link #nextId()} to look up elements
   */
  public AndroidElement get(Long elementId) {
    return cache.get(elementId);
  }

  /**
   * Uses the generated Id {@link #nextId()} to look up elements
   */
  public boolean hasElement(Long elementId) {
    return cache.containsKey(elementId);
  }
  
  public AndroidNativeElement getNativeElement(View view) {
    return nativeElementsByView.get(view);
  }

  public boolean hasNativeElement(View view) {
    return nativeElementsByView.containsKey(view);
  }

  public Long getIdOfElement(AndroidElement element) {
    if (cache.containsValue(element)) {
      return cache.inverse().get(element);
    }
    return null;
  }
}
