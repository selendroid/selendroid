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
package io.selendroid.server.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.selendroid.ServerInstrumentation;
import io.selendroid.android.ViewHierarchyAnalyzer;
import io.selendroid.exceptions.NoSuchElementException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.UnsupportedOperationException;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.server.model.AndroidNativeElement;
import io.selendroid.server.model.AndroidRElement;
import io.selendroid.server.model.By;
import io.selendroid.server.model.By.ByClass;
import io.selendroid.server.model.By.ById;
import io.selendroid.server.model.By.ByLinkText;
import io.selendroid.server.model.By.ByName;
import io.selendroid.server.model.By.ByTagName;
import io.selendroid.server.model.KnownElements;
import io.selendroid.server.model.SearchContext;
import io.selendroid.util.InstanceOfPredicate;
import io.selendroid.util.ListUtil;
import io.selendroid.util.Preconditions;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.internal.util.Predicate;

public abstract class AbstractNativeElementContext
    implements
      SearchContext,
      FindsByTagName,
      FindsByName,
      FindsById,
      FindsByText,
      FindsByPartialText,
      FindsByClass {
  protected ServerInstrumentation instrumentation;
  protected KnownElements knownElements;
  protected ViewHierarchyAnalyzer viewAnalyzer;

  public AbstractNativeElementContext(ServerInstrumentation instrumentation,
      KnownElements knownElements) {
    this.instrumentation = Preconditions.checkNotNull(instrumentation);
    this.knownElements = Preconditions.checkNotNull(knownElements);
    this.viewAnalyzer = ViewHierarchyAnalyzer.getDefaultInstance();
  }

  AndroidNativeElement newAndroidElement(View view) {
    Preconditions.checkNotNull(view);
    if (knownElements.hasElement(new Long(view.getId()))) {
      AndroidNativeElement element =
          (AndroidNativeElement) knownElements.get(new Long(view.getId()));
      // Caution: this is needed because e.g.
      // in spinner lists the items have by default all the same id
      if (element.getView().equals(view)) {
        return element;
      }
    }

    AndroidNativeElement e = new AndroidNativeElement(view, instrumentation, knownElements);
    knownElements.add(e);
    return e;
  }

  AndroidElement newAndroidElement(int id) {
    if (id < 0) {
      return null;
    }
    String idString = Integer.toString(id);
    if (knownElements.hasElement(idString)) {
      return knownElements.get(idString);
    }
    AndroidElement e = new AndroidRElement(id);
    knownElements.add(e);
    return e;

  }

  public AndroidNativeElement getElementTree() {
    View decorView = viewAnalyzer.getRecentDecorView();
    if (decorView == null) {
      throw new SelendroidException("No open windows.");
    }
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
    } else if (by instanceof By.ByPartialLinkText) {
      return findElementsByPartialText(by.getElementLocator());
    } else if (by instanceof ByClass) {
      return findElementsByClass(by.getElementLocator());
    } else if (by instanceof ByName) {
      return findElementsByName(by.getElementLocator());
    }

    throw new UnsupportedOperationException(String.format(
        "By locator %s is curently not supported!", by.getClass().getSimpleName()));
  }

  @Override
  public AndroidElement findElement(By by) {
    if (by instanceof ById) {
      return findElementById(by.getElementLocator());
    } else if (by instanceof ByTagName) {
      return findElementByTagName(by.getElementLocator());
    } else if (by instanceof ByLinkText) {
      return findElementByText(by.getElementLocator());
    } else if (by instanceof By.ByPartialLinkText) {
      return findElementByPartialText(by.getElementLocator());
    } else if (by instanceof ByClass) {
      return findElementByClass(by.getElementLocator());
    } else if (by instanceof ByName) {
      return findElementByName(by.getElementLocator());
    }
    throw new UnsupportedOperationException(String.format(
        "By locator %s is curently not supported!", by.getClass().getSimpleName()));
  }

  @Override
  public AndroidElement findElementById(String using) {
    List<AndroidElement> elements = findElementsById(using, true);
    if (!elements.isEmpty()) {
      return elements.get(0);
    }
    return null;
  }

  @Override
  public List<AndroidElement> findElementsById(String using) {
    return findElementsById(using, false);
  }

  private List<AndroidElement> findElementsById(String using, Boolean findJustOne) {
    List<AndroidElement> elements = new ArrayList<AndroidElement>();
    for (View view : viewAnalyzer.getViews(getTopLevelViews())) {
      String id = ViewHierarchyAnalyzer.getNativeId(view);
      if (id.equalsIgnoreCase("id/" + using) && viewAnalyzer.isViewChieldOfCurrentRootView(view)) {
        elements.add(newAndroidElement(view));
        if (findJustOne) return elements;
      }
    }
    if (elements.isEmpty()) {
      // didn't find any in the views, check the current activity
      // haven't seen this happen, just covering my basis / preserving some previous code.
      Activity currentActivity = instrumentation.getCurrentActivity();
      if (currentActivity != null) {
        int intId =
            currentActivity.getResources().getIdentifier(using, "id",
                currentActivity.getPackageName());
        if (intId > 0) {
          View view = currentActivity.findViewById(intId);

          if (viewAnalyzer.isViewChieldOfCurrentRootView(view)) {
            elements.add(newAndroidElement(view));
          }
        }
      }
    }

    return elements;
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
    Collection<View> currentViews = viewAnalyzer.getViews(getTopLevelViews());
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
        String textFieldText = String.valueOf(((TextView) view).getText());
        boolean textEqual = text.equals(textFieldText);
        return textEqual;
      }
      return false;
    }
  }

  class ViewPartialTextPredicate implements Predicate<View> {
    private String text = null;

    ViewPartialTextPredicate(String text) {
      this.text = text;
      System.out.println("Finding by partial text: " + text);
    }

    public boolean apply(View view) {
      if (view instanceof TextView) {
        String viewText = ((TextView) view).getText().toString();

        return viewText.indexOf(text) >= 0;
      }
      return false;
    }
  }

  class ViewTagNamePredicate implements Predicate<View> {
    private String tag;

    ViewTagNamePredicate(String tag) {
      this.tag = tag;
    }

    public boolean apply(View view) {
    	return view.getClass().getSimpleName().equals(tag);
    }
  }

  @Override
  public AndroidElement findElementByTagName(String using) {
    List<AndroidElement> elements = findElementsByTagName(using);
    if (elements != null && elements.size() > 0) {
      return elements.get(0);
    }
    return null;
  }

  @Override
  public List<AndroidElement> findElementsByTagName(String using) {
    Collection<View> currentViews = viewAnalyzer.getViews(getTopLevelViews());
    Predicate<View> predicate = new ViewTagNamePredicate(using);
    return filterAndTransformElements(currentViews, predicate);
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
    Collection<View> currentViews = viewAnalyzer.getViews(getTopLevelViews());
    Predicate<View> predicate = new ViewTextPredicate(using);
    return filterAndTransformElements(currentViews, predicate);
  }

  @Override
  public AndroidElement findElementByPartialText(String using) {
    List<AndroidElement> list = findElementsByPartialText(using);

    if (list != null && !list.isEmpty()) {
      return list.get(0);
    }
    return null;
  }

  @Override
  public List<AndroidElement> findElementsByPartialText(String using) {
    Collection<View> currentViews = viewAnalyzer.getViews(getTopLevelViews());
    Predicate<View> predicate = new ViewPartialTextPredicate(using);
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
    Collection<View> currentViews = viewAnalyzer.getViews(getTopLevelViews());
    Class viewClass = null;
    try {
      viewClass = Class.forName(using);
    } catch (ClassNotFoundException e) {
      throw new NoSuchElementException("The view class '" + using + "' was not found.");
    }
    return filterAndTransformElements(currentViews, new InstanceOfPredicate(viewClass));
  }

  private List<AndroidElement> filterAndTransformElements(Collection<View> currentViews,
      Predicate predicate) {
    Collection<?> filteredViews = ListUtil.filter(currentViews, predicate);
    final List<AndroidElement> filtered = new ArrayList<AndroidElement>();
    for (Object v : filteredViews) {
      filtered.add(newAndroidElement((View) v));
    }

    return filtered;
  }

  protected abstract View getRootView();

  protected abstract List<View> getTopLevelViews();
}
