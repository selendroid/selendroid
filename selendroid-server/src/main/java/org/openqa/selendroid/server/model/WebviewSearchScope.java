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

import java.util.List;

import org.openqa.selendroid.server.model.internal.AbstractWebviewSearchScope;
import org.openqa.selendroid.server.model.js.AndroidAtoms;

import com.google.gson.JsonElement;

import android.webkit.WebView;

public class WebviewSearchScope extends AbstractWebviewSearchScope {

  public WebviewSearchScope(KnownElements knownElements, WebView webview, SelendroidWebDriver driver) {
    super(knownElements, webview, driver);
  }

  @Override
  protected AndroidElement lookupElement(String strategy, String locator) {
    JsonElement result =
        (JsonElement) driver.executeAtom(AndroidAtoms.FIND_ELEMENT, strategy, locator);
    return replyElement(result);
  }

  @Override
  protected List<AndroidElement> lookupElements(String strategy, String locator) {
    JsonElement result =
        (JsonElement) driver.executeAtom(AndroidAtoms.FIND_ELEMENTS, strategy, locator);

    return replyElements(result);
  }
}
