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
package org.openqa.selendroid.android;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ScrollView;
import com.android.internal.util.Predicate;
import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.util.InstanceOfPredicate;
import org.openqa.selendroid.util.ListUtil;
import org.openqa.selendroid.util.Preconditions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewHierarchyAnalyzer {
  private static final ViewHierarchyAnalyzer INSTANCE = new ViewHierarchyAnalyzer();

  public static ViewHierarchyAnalyzer getDefaultInstance() {
    return INSTANCE;
  }

  public Set<View> getTopLevelViews() {
    Class<?> windowManager = null;
    try {
      String windowManagerClassName;
      if (android.os.Build.VERSION.SDK_INT >= 17) {
        windowManagerClassName = "android.view.WindowManagerGlobal";
      } else {
        windowManagerClassName = "android.view.WindowManagerImpl";
      }
      windowManager = Class.forName(windowManagerClassName);

    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    Field views;
    Field instanceField;
    try {
      views = windowManager.getDeclaredField("mViews");
      instanceField = windowManager.getDeclaredField(getWindowManagerString());
      views.setAccessible(true);
      instanceField.setAccessible(true);
      Object instance = instanceField.get(null);
      synchronized (windowManager) {
        return new HashSet<View>(Arrays.asList(((View[]) views.get(instance))));
      }
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  private String getWindowManagerString() {
    if (android.os.Build.VERSION.SDK_INT >= 17) {
      return "sDefaultWindowManager";
    } else if (android.os.Build.VERSION.SDK_INT >= 13) {
      return "sWindowManager";
    } else {
      return "mWindowManager";
    }
  }

  public View getRecentDecorView() {
    return getRecentDecorView(getTopLevelViews());
  }

  private View getRecentDecorView(Set<View> views) {
    Collection<View> decorViews = (Collection<View>)ListUtil.filter(new ArrayList<View>(views), new DecorViewPredicate());
    View container = null;
    // candidate is to fall back to most recent 'shown' view if none have the 'window focus'
    // this seems to be able to happen with menus
    View candidate = null;
    long drawingTime = 0;
    long candidateTime = 0;
    for (View view : decorViews) {
      if (view.isShown() && view.getDrawingTime() > drawingTime) {
        if (view.hasWindowFocus()) {
          container = view;
          drawingTime = view.getDrawingTime();
        } else if (view.getDrawingTime() > candidateTime) {
          candidate = view;
          candidateTime = view.getDrawingTime();
        }
      }
    }
    if (container == null) {
      container = candidate;
    }
    return container;
  }

  private static class DecorViewPredicate<E> implements Predicate<E> {
    @Override
    public boolean apply(E view) {
      // PopupViewContainer can be a top level menu shown
      return "DecorView".equals(view.getClass().getSimpleName())
          || "PopupViewContainer".equals(view.getClass().getSimpleName());
    }
  }

  public Collection<View> getViews(List<View> rootViews) {
    final List<View> views = new ArrayList<View>();
    for (View rootView : rootViews) {
      Preconditions.checkNotNull(rootView);
      if (rootView instanceof ViewGroup) {
        addAllChilren((ViewGroup) rootView, views);
      }
    }
    return views;
  }

  private void addAllChilren(ViewGroup viewGroup, List<View> list) {
    if (viewGroup == null || viewGroup.getChildCount() == 0) {
      return;
    }
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View childView = viewGroup.getChildAt(i);
      list.add(childView);

      if (childView instanceof ViewGroup) {
        addAllChilren((ViewGroup) childView, list);
      }
    }
  }

  public static String getNativeId(View view) {
    String id = "";
    try {
      id =
          ServerInstrumentation.getInstance().getCurrentActivity().getResources()
              .getResourceName(view.getId());
      // remove the package name
      id = id.split(":")[1];
    } catch (Resources.NotFoundException e) {
      // can happen
    }
    return id;
  }

  public WebView findWebView() {
    final List<WebView> webViews =
        (List<WebView>) ListUtil.filter(getViews(Arrays.asList(getRecentDecorView())),
            new InstanceOfPredicate(WebView.class));

    if (webViews.isEmpty()) {
      return null;
    }
    return webViews.get(0);
  }

  public List<View> findScrollableContainer() {
    Collection<View> allViews = getViews(Arrays.asList(getRecentDecorView()));
    List<View> container = new ArrayList<View>();
    List<AbsListView> listview =
        (List<AbsListView>) ListUtil.filter(allViews,
            new InstanceOfPredicate(AbsListView.class));
    if (listview != null && !listview.isEmpty()) {
      container.addAll(listview);
    }
    List<ScrollView> scrollview =
        (List<ScrollView>) ListUtil.filter(allViews,
            new InstanceOfPredicate(ScrollView.class));
    container.addAll(scrollview);
    List<WebView> webview =
        (List<WebView>) ListUtil.filter(allViews,
            new InstanceOfPredicate(WebView.class));
    container.addAll(webview);
    return container;
  }
}
