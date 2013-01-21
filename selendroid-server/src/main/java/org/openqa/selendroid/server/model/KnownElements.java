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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class KnownElements {
  private final BiMap<Long, AndroidElement> elements = HashBiMap.create();
  private long nextId = 0L;

  public Long add(AndroidElement element) {
    if (elements.containsValue(element)) {
      return elements.inverse().get(element);
    }
    Long id = null;
    if (element instanceof AndroidNativeElement) {
      id = new Long(((AndroidNativeElement) element).getView().getId());
    } else {
      id = nextId();
    }

    elements.put(id, (AndroidElement) element);
    return id;
  }

  public Long nextId() {
    return ++nextId;
  }

  public AndroidElement get(Long elementId) {
    return elements.get(elementId);
  }

  public boolean hasElement(Long elementId) {
    return elements.containsKey(elementId);
  }

  public Long getIdOfElement(AndroidElement element) {
    if (elements.containsValue(element)) {
      return elements.inverse().get(element);
    }
    return -1L;
  }
}
