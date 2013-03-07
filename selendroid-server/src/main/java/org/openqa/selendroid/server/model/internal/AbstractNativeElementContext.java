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
package org.openqa.selendroid.server.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.android.ViewHierarchyAnalyzer;
import org.openqa.selendroid.server.exceptions.NoSuchElementException;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.server.model.AndroidNativeElement;
import org.openqa.selendroid.server.model.By;
import org.openqa.selendroid.server.model.By.ByClass;
import org.openqa.selendroid.server.model.By.ById;
import org.openqa.selendroid.server.model.By.ByLinkText;
import org.openqa.selendroid.server.model.By.ByName;
import org.openqa.selendroid.server.model.By.ByTagName;
import org.openqa.selendroid.server.model.KnownElements;
import org.openqa.selendroid.server.model.SearchContext;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

public abstract class AbstractNativeElementContext
    implements
      SearchContext,
      FindsByTagName,
      FindsByName,
      FindsById,
      FindsByText,
      FindsByClass {
  protected ServerInstrumentation instrumentation;
  protected KnownElements knownElements;
  protected ViewHierarchyAnalyzer viewAnalyzer;

  public AbstractNativeElementContext(ServerInstrumentation instrumentation,
      KnownElements knownElements) {
    this.instrumentation = instrumentation;
    this.knownElements = knownElements;
    this.viewAnalyzer = ViewHierarchyAnalyzer.getDefaultInstance();
  }

  AndroidNativeElement newAndroidElement(View view) {
    if (knownElements.hasElement(new Long(view.getId()))) {
      return (AndroidNativeElement) knownElements.get(new Long(view.getId()));
    } else {
      AndroidNativeElement e = new AndroidNativeElement(view, instrumentation, knownElements);
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
    } else if (by instanceof ByTagName) {
      return findElementsByTagName(by.getElementLocator());
    } else if (by instanceof ByLinkText) {
      return findElementsByText(by.getElementLocator());
    } else if (by instanceof ByClass) {
      return findElementsByClass(by.getElementLocator());
    } else if (by instanceof ByName) {
      return findElementsByName(by.getElementLocator());
    }

    throw new SelendroidException(String.format("By locator %s is curently not supported!", by
        .getClass().getSimpleName()));
  }

  @Override
  public AndroidElement findElement(By by) {
    if (by instanceof ById) {
      return findElementById(by.getElementLocator());
    } else if (by instanceof ByTagName) {
      return findElementByTagName(by.getElementLocator());
    } else if (by instanceof ByLinkText) {
      return findElementByText(by.getElementLocator());
    } else if (by instanceof ByClass) {
      return findElementByClass(by.getElementLocator());
    } else if (by instanceof ByName) {
      return findElementByName(by.getElementLocator());
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
    for (View view : viewAnalyzer.getViews(getRootView())) {
      String id = ViewHierarchyAnalyzer.getNativeId(view);
      if (id.endsWith("id/" + using)) {
        list.add(newAndroidElement(view));
      }
    }
    if (list.isEmpty()) {
      throw new NoSuchElementException("No elements were found.");
    }
    return list;
  }

  @Override
  public AndroidElement findElementByName(String using) {
    List<AndroidElement> list = findElementsByName(using);

    if (list != null && !list.isEmpty()) {
      return list.get(0);
    }
    return null;
  }

  @Override
  public List<AndroidElement> findElementsByName(String using) {
    Collection<View> currentViews = viewAnalyzer.getViews(getRootView());
    Predicate<View> predicate = new ViewContentDescriptionPredicate(using);
    return filterAndTransformElements(currentViews, predicate);
  }

  class ViewContentDescriptionPredicate implements Predicate<View> {
    private String contenDescription = null;

    ViewContentDescriptionPredicate(String contenDescription) {
      this.contenDescription = contenDescription;
    }

    public boolean apply(View view) {
      return contenDescription.equals(view.getContentDescription());
    }
  }

  class ViewTextPredicate implements Predicate<View> {
    private String text = null;

    ViewTextPredicate(String text) {
      this.text = text;
    }

    public boolean apply(View view) {
      if (view instanceof TextView) {
        return (text.equals(((TextView) view).getText()));
      }
      return false;
    }
  }


  @Override
  public AndroidElement findElementByTagName(String using) {
    String localizedString = getLocalizedString(using);
    return findElementByText(localizedString);
  }

  @Override
  public List<AndroidElement> findElementsByTagName(String using) {
    String localizedString = getLocalizedString(using);
    return findElementsByText(localizedString);
  }

  protected String getLocalizedString(String l10nKey) {
    Activity currentActivity = instrumentation.getCurrentActivity();
    int resourceId =
        currentActivity.getResources().getIdentifier(l10nKey, "string",
            currentActivity.getPackageName());
    try {
      return currentActivity.getResources().getString(resourceId);
    } catch (Resources.NotFoundException e) {
      throw new NoSuchElementException("The l10n key '" + l10nKey + "' was not found.");
    }
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
    Collection<View> currentViews = viewAnalyzer.getViews(getRootView());
    Predicate<View> predicate = new ViewTextPredicate(using);
    return filterAndTransformElements(currentViews, predicate);
  }

  @Override
  public AndroidElement findElementByClass(String using) {
    List<AndroidElement> list = findElementsByClass(using);

    if (list != null && !list.isEmpty()) {
      return list.get(0);
    }
    return null;
  }

  @Override
  public List<AndroidElement> findElementsByClass(String using) {
    Collection<View> currentViews = viewAnalyzer.getViews(getRootView());
    Class viewClass = null;
    try {
      viewClass = Class.forName(using);
    } catch (ClassNotFoundException e) {
      throw new NoSuchElementException("The view class '" + using + "' was not found.");
    }
    return filterAndTransformElements(currentViews, Predicates.instanceOf(viewClass));
  }

  private List<AndroidElement> filterAndTransformElements(Collection<View> currentViews,
      Predicate predicate) {
    final List<AndroidElement> filtered =
        FluentIterable.from(currentViews).filter(predicate)
            .transform(new Function<View, AndroidElement>() {
              @Override
              public AndroidNativeElement apply(final View view) {
                return newAndroidElement(view);
              }
            }).toImmutableList();

    if (filtered.isEmpty()) {
      throw new NoSuchElementException("No elements were found.");
    }

    return filtered;


  }

  protected abstract View getRootView();
}
