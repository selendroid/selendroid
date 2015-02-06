/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.internal.util.Predicate;

import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.android.KeySender;
import io.selendroid.server.android.ViewHierarchyAnalyzer;
import io.selendroid.server.common.exceptions.NoSuchElementException;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.common.exceptions.UnsupportedOperationException;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.server.model.AndroidNativeElement;
import io.selendroid.server.model.AndroidRElement;
import io.selendroid.server.model.By;
import io.selendroid.server.model.KnownElements;
import io.selendroid.server.model.SearchContext;
import io.selendroid.server.model.By.ByClass;
import io.selendroid.server.model.By.ById;
import io.selendroid.server.model.By.ByLinkText;
import io.selendroid.server.model.By.ByName;
import io.selendroid.server.model.By.ByTagName;
import io.selendroid.server.model.By.ByXPath;
import io.selendroid.server.util.InstanceOfPredicate;
import io.selendroid.server.util.ListUtil;
import io.selendroid.server.util.Preconditions;
import io.selendroid.server.util.SelendroidLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class AbstractNativeElementContext
    implements
      SearchContext,
      FindsByTagName,
      FindsByName,
      FindsById,
      FindsByText,
      FindsByXPath,
      FindsByPartialText,
      FindsByClass {
  protected ServerInstrumentation instrumentation;
  protected KeySender keys;
  protected KnownElements knownElements;
  protected ViewHierarchyAnalyzer viewAnalyzer;

  public AbstractNativeElementContext(ServerInstrumentation instrumentation, KeySender keys, KnownElements knownElements) {
    this.instrumentation = Preconditions.checkNotNull(instrumentation);
    this.keys = Preconditions.checkNotNull(keys);
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

    AndroidNativeElement e = new AndroidNativeElement(view, instrumentation, keys, knownElements);
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

  public JSONObject getElementTree() throws JSONException {
    View decorView = viewAnalyzer.getRecentDecorView();
    if (decorView == null) {
      throw new SelendroidException("No open windows.");
    }
    AndroidNativeElement rootElement = newAndroidElement(decorView);

    if (decorView instanceof ViewGroup) {
      addChildren((ViewGroup) decorView, rootElement);
    }

    JSONObject root = rootElement.toJson();
    if (root == null) {
      return new JSONObject();
    }
    root.put("activity", instrumentation.getCurrentActivity().getComponentName().toShortString());

    addChildren(root, rootElement);

    return root;
  }

  private void addChildren(JSONObject parent, AndroidElement parentElement) throws JSONException {
    Collection<AndroidElement> children = parentElement.getChildren();
    if (children == null || children.isEmpty()) {
      return;
    }
    JSONArray childs = new JSONArray();
    for (AndroidElement child : children) {
      if (((AndroidNativeElement) child).getView() != ((AndroidNativeElement) parentElement)
          .getView()) {
        JSONObject jsonChild = ((AndroidNativeElement) child).toJson();
        childs.put(jsonChild);

        addChildren(jsonChild, child);
      }
    }
    parent.put("children", childs);
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
    } else if (by instanceof ByXPath) {
      return findElementsByXPath(by.getElementLocator());
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
    } else if (by instanceof ByXPath) {
      return findElementByXPath(by.getElementLocator());
    }
    throw new UnsupportedOperationException(String.format(
        "By locator %s is curently not supported!", by.getClass().getSimpleName()));
  }


  public AndroidElement findElementByXPath(String using) {
    List<AndroidElement> elements = findElementsByXPath(using);
    if (!elements.isEmpty()) {
      return elements.get(0);
    }
    return null;
  }

  public List<AndroidElement> findElementsByXPath(String expression) {
    JSONObject root = null;
    try {
      root = getElementTree();
    } catch (JSONException e1) {
      SelendroidLogger.error("Could not getElementTree", e1);
    }

    Document xmlDocument = JsonXmlUtil.buildXmlDocument(root);
    XPath xPath = XPathFactory.newInstance().newXPath();

    List<AndroidElement> elements = new ArrayList<AndroidElement>();
    NodeList nodeList;
    try {
      // read a nodelist using xpath
      nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      SelendroidLogger.error("Failed to get NodeList for XPath", e);
      return elements;
    }
    if (nodeList != null && nodeList.getLength() > 0) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);
        if (node.getAttributes() == null) {
          continue;
        }
        Node namedItem = node.getAttributes().getNamedItem("ref");
        if (namedItem != null) {
          elements.add(knownElements.get(namedItem.getTextContent()));
        }
      }
    }

    return elements;
  }

  @Override
  public AndroidElement findElementById(String using) {
    return findFirstByPredicate(new ViewIdPredicate(using));
  }

  @Override
  public List<AndroidElement> findElementsById(String using) {
    return findAllByPredicate(new ViewIdPredicate(using));
  }


  /** visible for testing */
  protected static List<AndroidElement> searchViews(AbstractNativeElementContext context, List<View> roots,
      Predicate predicate, boolean findJustOne) {
    List<AndroidElement> elements = new ArrayList<AndroidElement>();
    if (roots == null || roots.isEmpty()) {
      return elements;
    }
    ArrayDeque<View> queue = new ArrayDeque<View>();

    for (View root : roots) {
      queue.add(root);
      while (!queue.isEmpty()) {
        View view = queue.pop();
        if (predicate.apply(view)) {
          elements.add(context.newAndroidElement(view));
          if (findJustOne) {
            break;
          }
        }
        if (view instanceof ViewGroup) {
          ViewGroup group = (ViewGroup) view;
          int childrenCount = group.getChildCount();
          for (int index = 0; index < childrenCount; index++) {
            queue.add(group.getChildAt(index));
          }
        }
      }
    }
    return elements;
  }

  private List<AndroidElement> findAllByPredicate(Predicate predicate) {
     return searchViews(this, getTopLevelViews(), predicate, false);
  }

  private AndroidElement findFirstByPredicate(Predicate predicate) {
    List<AndroidElement> list = searchViews(this, getTopLevelViews(), predicate, true);
    if (list != null && !list.isEmpty()) {
      return list.get(0);
    }
    return null;
  }
  
  @Override
  public AndroidElement findElementByName(String using) {
    return findFirstByPredicate(new ViewContentDescriptionPredicate(using));
  }

  @Override
  public List<AndroidElement> findElementsByName(String using) {
    return findAllByPredicate(new ViewContentDescriptionPredicate(using));
  }

  class ViewContentDescriptionPredicate implements Predicate<View> {
    private String contentDescription = null;

    ViewContentDescriptionPredicate(String contentDescription) {
      this.contentDescription = contentDescription;
    }

    public boolean apply(View view) {
      return charSequenceEquals(contentDescription, view.getContentDescription());
    }

    private boolean charSequenceEquals(String s, CharSequence cs) {
      if (s == null) {
        return cs == null;
      } else {
        return cs != null && s.contentEquals(cs);
      }
    }
  }

  class ViewIdPredicate implements Predicate<View> {

    private String id;

    public ViewIdPredicate(String id) {
      this.id = "id/" + id;
    }

    @Override
    public boolean apply(View view) {
      return ViewHierarchyAnalyzer.getNativeId(view).equalsIgnoreCase(id);
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
      SelendroidLogger.info("Finding by partial text: " + text);
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
    return findFirstByPredicate(new ViewTagNamePredicate(using));
  }

  @Override
  public List<AndroidElement> findElementsByTagName(String using) {
    return findAllByPredicate(new ViewTagNamePredicate(using));
  }

  @Override
  public AndroidElement findElementByText(String using) {
    return findFirstByPredicate(new ViewTextPredicate(using));
  }

  @Override
  public List<AndroidElement> findElementsByText(String using) {
    return findAllByPredicate(new ViewTextPredicate(using));
  }

  @Override
  public AndroidElement findElementByPartialText(String using) {
    return findFirstByPredicate(new ViewPartialTextPredicate(using));
  }

  @Override
  public List<AndroidElement> findElementsByPartialText(String using) {
    return findAllByPredicate(new ViewPartialTextPredicate(using));
  }

  @Override
  public AndroidElement findElementByClass(String using) {
    Class viewClass = null;
    try {
      viewClass = Class.forName(using);
    } catch (ClassNotFoundException e) {
      throw new NoSuchElementException("The view class '" + using + "' was not found.");
    }
    return findFirstByPredicate(new InstanceOfPredicate(viewClass));
  }

  @Override
  public List<AndroidElement> findElementsByClass(String using) {
    Class viewClass = null;
    try {
      viewClass = Class.forName(using);
    } catch (ClassNotFoundException e) {
      return new ArrayList<AndroidElement>();
    }
    return findAllByPredicate(new InstanceOfPredicate(viewClass));
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
