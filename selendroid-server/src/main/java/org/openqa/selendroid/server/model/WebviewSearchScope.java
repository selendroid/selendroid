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

import java.util.ArrayList;
import java.util.List;

import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.By.ByClass;
import org.openqa.selendroid.server.model.By.ById;
import org.openqa.selendroid.server.model.By.ByL10nElement;
import org.openqa.selendroid.server.model.By.ByLinkText;
import org.openqa.selendroid.server.model.By.ByXPath;
import org.openqa.selendroid.server.model.internal.FindsByClass;
import org.openqa.selendroid.server.model.internal.FindsById;
import org.openqa.selendroid.server.model.internal.FindsByL10n;
import org.openqa.selendroid.server.model.internal.FindsByText;
import org.openqa.selendroid.server.model.js.AndroidAtoms;

import android.webkit.WebView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class WebviewSearchScope
    implements
      SearchContext,
      FindsByL10n,
      FindsById,
      FindsByText,
      FindsByClass {
  private KnownElements knownElements;
  private volatile WebView view;
  private SelendroidWebDriver driver = null;

  public WebviewSearchScope(KnownElements knownElements, WebView webview, SelendroidWebDriver driver) {
    if (knownElements == null) {
      throw new IllegalArgumentException("knowElements instance is null");
    }
    if (webview == null) {
      throw new IllegalArgumentException("webview instance is null");
    }
    this.knownElements = knownElements;
    this.view = webview;
    this.driver = driver;
  }

  public AndroidWebElement getElementTree() {
    throw new UnsupportedOperationException(
        "Logging the element tree of a webview is currently not supported.");
  }

  @Override
  public List<AndroidElement> findElements(By by) {
    if (by instanceof ById) {
      return findElementsById(by.getElementLocator());
    } else if (by instanceof ByL10nElement) {
      return findElementsByL10n(by.getElementLocator());
    } else if (by instanceof ByLinkText) {
      return findElementsByText(by.getElementLocator());
    } else if (by instanceof ByXPath) {
      return findElementsByXPath(by.getElementLocator());
    } else if (by instanceof ByClass) {
      return findElementsByClass(by.getElementLocator());
    }
    throw new SelendroidException(String.format("By locator %s is curently not supported!", by
        .getClass().getSimpleName()));
  }

  public AndroidWebElement newAndroidWebElementById(String id) {
    AndroidWebElement element = new AndroidWebElement(id, view, driver);
    knownElements.add(element);
    return element;
  }

  @Override
  public AndroidElement findElement(By by) {
    if (by instanceof By.ById) {
      return findElementById(by.getElementLocator());
    } else if (by instanceof By.ByXPath) {
      return findElementByXPath(by.getElementLocator());
    } else if (by instanceof By.ByLinkText) {
      return findElementByText(by.getElementLocator());
    } else if (by instanceof By.ByName) {
      return findElementByName(by.getElementLocator());
    } else if (by instanceof By.ByClass) {
      return findElementByClass(by.getElementLocator());
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public AndroidElement findElementById(String id) {
    return findElementByXPath("//*[@id='" + id + "']");
  }

  private AndroidElement replyElement(JsonElement result) {
    if (result == null || result instanceof JsonNull) {
      return null;
    }
    String id = ((JsonObject) result).get("ELEMENT").getAsString();
    return newAndroidWebElementById(id);
  }

  @Override
  public List<AndroidElement> findElementsById(String id) {
    return findElementsByXPath("//*[@id='" + id + "']");
  }

  @Override
  public AndroidElement findElementByL10n(String using) {
    throw new UnsupportedOperationException(
        "finding elements by l10n locator is not supported in webviews.");
  }

  @Override
  public List<AndroidElement> findElementsByL10n(String using) {
    throw new UnsupportedOperationException(
        "finding elements by l10n locator is not supported in webviews.");
  }

  @Override
  public AndroidElement findElementByText(String using) {
    JsonElement result =
        (JsonElement) driver.executeAtom(AndroidAtoms.FIND_ELEMENT, "linkText", using);
    return replyElement(result);
  }

  @Override
  public List<AndroidElement> findElementsByText(String using) {
    JsonElement result =
        (JsonElement) driver.executeAtom(AndroidAtoms.FIND_ELEMENTS, "linkText", using);

    return replyElements(result);
  }


  public AndroidElement findElementByXPath(String using) {
    JsonElement result =
        (JsonElement) driver.executeAtom(AndroidAtoms.FIND_ELEMENT, "xpath", using);
    return replyElement(result);
  }


  public List<AndroidElement> findElementsByXPath(String using) {
    JsonElement result =
        (JsonElement) driver.executeAtom(AndroidAtoms.FIND_ELEMENTS, "xpath", using);

    return replyElements(result);
  }

  private List<AndroidElement> replyElements(JsonElement result) {
    if (result == null || result instanceof JsonNull) {
      return null;
    }
    List<AndroidElement> elements = new ArrayList<AndroidElement>();
    JsonArray jsonElements = result.getAsJsonArray();
    if (jsonElements != null && jsonElements.size() > 0) {
      for (JsonElement element : jsonElements) {
        String id = ((JsonObject) element).get("ELEMENT").getAsString();
        elements.add(newAndroidWebElementById(id));
      }
    }

    return elements;
  }

  public AndroidElement findElementByName(String using) {
    JsonElement result = (JsonElement) driver.executeAtom(AndroidAtoms.FIND_ELEMENT, "name", using);
    return replyElement(result);
  }

  public List<AndroidElement> findElementsByName(String using) {
    JsonElement result =
        (JsonElement) driver.executeAtom(AndroidAtoms.FIND_ELEMENTS, "name", using);

    return replyElements(result);
  }

  @Override
  public AndroidElement findElementByClass(String using) {
    JsonElement result =
        (JsonElement) driver.executeAtom(AndroidAtoms.FIND_ELEMENT, "class", using);
    return replyElement(result);
  }

  @Override
  public List<AndroidElement> findElementsByClass(String using) {
    JsonElement result =
        (JsonElement) driver.executeAtom(AndroidAtoms.FIND_ELEMENTS, "class", using);

    return replyElements(result);
  }
}
