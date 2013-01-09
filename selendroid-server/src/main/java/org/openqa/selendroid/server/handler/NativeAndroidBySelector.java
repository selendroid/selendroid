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
package org.openqa.selendroid.server.handler;


import org.openqa.selendroid.android.WindowType;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.By;

public class NativeAndroidBySelector {
  public static final String SELECTOR_NATIVE_ID = "id";
  // TODO review this, not perfect, but main goal is to use default bindings
  public static final String SELECTOR_L10N = "tag name";
  public static final String SELECTOR_TEXT = "link text";
  public static final String SELECTOR_XPATH = "xpath";
  public static final String SELECTOR_NAME = "name";

  public By pickFrom(String method, String selector, WindowType type) {
    if (SELECTOR_NATIVE_ID.equals(method)) {
      return By.id(selector);
    } else if (SELECTOR_L10N.equals(method)) {
      return By.l10n(selector);
    } else if (SELECTOR_NAME.equals(method)) {
      return By.name(selector);
    } else if (SELECTOR_TEXT.equals(method)) {
      return By.linkText(selector);
    } else if (SELECTOR_XPATH.equals(method)) {
      return By.xpath(selector);
    }

    else {
      System.out.println("By type for methof not found: " + method);
      throw new SelendroidException("method (by) not found: " + method);
    }
  }
}
