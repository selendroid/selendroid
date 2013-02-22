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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.android.ViewHierarchyAnalyzer;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.By.ById;
import org.openqa.selendroid.server.model.By.ByL10nElement;
import org.openqa.selendroid.server.model.By.ByLinkText;
import org.openqa.selendroid.server.model.internal.FindsById;
import org.openqa.selendroid.server.model.internal.FindsByL10n;
import org.openqa.selendroid.server.model.internal.FindsByText;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NativeSearchScope implements SearchContext, FindsByL10n, FindsById, FindsByText {
  private ServerInstrumentation instrumentation;
  private KnownElements knownElements;
  private ViewHierarchyAnalyzer viewAnalyzer;

  public NativeSearchScope(ServerInstrumentation instrumentation, KnownElements knownElements) {
    this.instrumentation = instrumentation;
    this.knownElements = knownElements;
    this.viewAnalyzer = ViewHierarchyAnalyzer.getDefaultInstance();
  }

  private AndroidNativeElement newAndroidElement(View view) {
    if (knownElements.hasElement(new Long(view.getId()))) {
      return (AndroidNativeElement) knownElements.get(new Long(view.getId()));
    } else {
      AndroidNativeElement e = new AndroidNativeElement(view, instrumentation);
      knownElements.add(e);
      return e;
    }
  }

  public AndroidNativeElement getElementTree() {
    View decorView = viewAnalyzer.getRecentDecorView();
    AndroidNativeElement rootElement = newAndroidElement(decorView);

    if (decorView instanceof ViewGroup) {
      addChildren((ViewGroup) decorView, rootElement);
    }

    return rootElement;
  }

  private void addChildren(ViewGroup viewGroup, AndroidNativeElement parent) {
    if (viewGroup.getChildCount() == 0) {
      return;
    }
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View childView = viewGroup.getChildAt(i);
      AndroidNativeElement child = newAndroidElement(childView);

      parent.addChild(child);
      child.setParent(parent);
      if (childView instanceof ViewGroup) {
        addChildren((ViewGroup) childView, child);
      }
    }
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

  @Override
  public AndroidElement findElement(By by) {
    if (by instanceof ById) {
      return findElementById(by.getElementLocator());
    } else if (by instanceof ByL10nElement) {
      return findElementByL10n(by.getElementLocator());
    } else if (by instanceof ByLinkText) {
      return findElementByText(by.getElementLocator());
    }
    throw new SelendroidException(String.format("By locator %s is curently not supported!", by
        .getClass().getSimpleName()));
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
    List<AndroidElement> list = new ArrayList<AndroidElement>();
    for (View view : viewAnalyzer.getViews()) {
      String id = viewAnalyzer.getNativeId(view);
      if (id.endsWith(":id/" + using)) {
        list.add(newAndroidElement(view));
      }
    }
    return list;
  }

  @Override
  public AndroidElement findElementByL10n(String using) {
    String localizedString = getLocalizedString(using);
    return findElementByText(localizedString);
  }

  @Override
  public List<AndroidElement> findElementsByL10n(String using) {
    String localizedString = getLocalizedString(using);
    return findElementsByText(localizedString);
  }

  protected String getLocalizedString(String l10nKey) {
    Activity currentActivity = instrumentation.getCurrentActivity();
    int resourceId =
        currentActivity.getResources().getIdentifier(l10nKey, "string",
            currentActivity.getPackageName());

    String localizedString = currentActivity.getResources().getString(resourceId);
    return localizedString;
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
    Collection<View> currentViews = viewAnalyzer.getViews();
    if (!currentViews.isEmpty()) {
      for (View view : viewAnalyzer.getViews()) {
        if (view instanceof TextView) {
          if (using.equals(((TextView) view).getText())) {
            list.add(newAndroidElement(view));
          }
        }
      }
    }
    return list;
  }
}
