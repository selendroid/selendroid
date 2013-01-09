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
package org.openqa.selendroid.server;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.server.exceptions.NoSuchElementException;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.server.model.AndroidNativeElement;
import org.openqa.selendroid.server.model.By;
import org.openqa.selendroid.server.model.By.ById;
import org.openqa.selendroid.server.model.By.ByL10nElement;
import org.openqa.selendroid.server.model.By.ByLinkText;
import org.openqa.selendroid.server.model.KnownElements;
import org.openqa.selendroid.server.model.SearchContext;
import org.openqa.selendroid.server.model.internal.FindsById;
import org.openqa.selendroid.server.model.internal.FindsByL10n;
import org.openqa.selendroid.server.model.internal.FindsByText;
import org.openqa.selendroid.server.webview.AndroidWebDriver;
import org.openqa.selendroid.server.webview.AndroidWebElement;
import org.openqa.selendroid.server.webview.js.AndroidAtoms;

import android.app.Activity;
import android.view.View;
import android.webkit.WebView;

public class WebviewSearchScope implements SearchContext, FindsByL10n, FindsById, FindsByText {
  private ServerInstrumentation instrumentation;
  private KnownElements knownElements;
  private WebView view;
  private AndroidWebDriver driver = null;

  public WebviewSearchScope(ServerInstrumentation instrumentation, KnownElements knownElements,
      WebView webview, AndroidWebDriver driver) {
    if (instrumentation == null) {
      throw new IllegalArgumentException("intrumentation instance is null");
    }
    if (knownElements == null) {
      throw new IllegalArgumentException("knowElements instance is null");
    }
    if (webview == null) {
      throw new IllegalArgumentException("webview instance is null");
    }
    this.instrumentation = instrumentation;
    this.knownElements = knownElements;
    this.view = webview;
    this.driver = driver;
  }

  public AndroidNativeElement newAndroidElement(View view) {
    if (knownElements.hasElement(view.getId())) {
      return (AndroidNativeElement) knownElements.get(view.getId());
    } else {
      AndroidNativeElement e = new AndroidNativeElement(view, instrumentation);
      knownElements.add(e);
      return e;
    }
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
    }
    throw new SelendroidException(String.format("By locator %s is curently not supported!", by
        .getClass().getSimpleName()));
  }

  public AndroidWebElement newAndroidWebElementById(String id) {
    // TODO Review Driver concept
    return new AndroidWebElement(id, view, driver);
  }

  @Override
  public AndroidElement findElement(By by) {
    String response = null;
    if (by instanceof By.ById) {
      String id = ((By.ById) by).getElementLocator();
      response = findById(id);
    } else if (by instanceof By.ByXPath) {
      String xpath = ((By.ByXPath) by).getElementLocator();
      response = findByXPath(xpath);
    } else if (by instanceof By.ByLinkText) {
      throw new UnsupportedOperationException();
    } else if (by instanceof By.ByName) {
      String name = ((By.ByName) by).getElementLocator();
      response = findByName(name);
    } else {
      throw new UnsupportedOperationException();
    }
    if (response == null) {
      throw new NoSuchElementException("The element for locator: '" + by + "' was not found.");
    }
    return newAndroidWebElementById(response);
  }

  @Override
  public AndroidElement findElementById(String using) {
    Activity currentActivity = instrumentation.getCurrentActivity();
    if (currentActivity == null) {
      return null;
    }
    int intId =
        currentActivity.getResources().getIdentifier(using, "id", currentActivity.getPackageName());
    if (intId == 0) {
      return null;
    }
    View view = currentActivity.findViewById(intId);
    if (view == null) {
      return null;
    }
    return newAndroidElement(view);
  }

  @Override
  public List<AndroidElement> findElementsById(String using) {
    throw new UnsupportedOperationException();
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
    List<AndroidElement> list = findElementsByText(using);

    if (list != null && !list.isEmpty()) {
      return list.get(0);
    }
    return null;
  }

  @Override
  public List<AndroidElement> findElementsByText(String using) {
    List<AndroidElement> list = new ArrayList<AndroidElement>();
    return list;
  }

  public String findById(String id) {
    return (String) driver.executeAtom(AndroidAtoms.FIND_ELEMENT, "id", id);
    // return findByXPath("*[@id = '" + id + "']");
  }

  public String findByName(String name) {
    return (String) driver.executeAtom(AndroidAtoms.FIND_ELEMENT, "name", name);
  }

  public String findByXPath(String xpath) {
    return (String) driver.executeAtom(AndroidAtoms.FIND_ELEMENT, "xpath", xpath);
  }
}
