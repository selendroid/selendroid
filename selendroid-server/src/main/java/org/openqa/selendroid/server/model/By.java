/*
 * Copyright 2007-2011 Selenium committers
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

import org.openqa.selendroid.server.model.internal.FindsByTagName;

/**
 * Mechanism used to locate elements within a document. In order to create your own locating
 * mechanisms, it is possible to subclass this class and override the protected methods as required,
 * though it is expected that that all subclasses rely on the basic finding mechanisms provided
 * through static methods of this class:
 * 
 * <code>
 * public WebElement findElement(WebDriver driver) {
 *     WebElement element = driver.findElement(By.id(getSelector()));
 *     if (element == null)
 *       element = driver.findElement(By.name(getSelector());
 *     return element;
 * }
 * </code>
 */
public abstract class By {
  public static class ByCssSelector extends By {
    private final String selector;

    public ByCssSelector(String selector) {
      this.selector = selector;
    }

    @Override
    public AndroidElement findElement(SearchContext context) {
      return context.findElement(By.cssSelector(selector));
    }

    @Override
    public List<AndroidElement> findElements(SearchContext context) {
      return context.findElements(By.cssSelector(selector));
    }

    @Override
    public String getElementLocator() {
      return selector;
    }
  }

  public static class ById extends By {
    private final String id;

    public ById(String id) {
      this.id = id;
    }

    @Override
    public AndroidElement findElement(SearchContext context) {
      return context.findElement(By.id(id));
    }

    @Override
    public List<AndroidElement> findElements(SearchContext context) {
      return context.findElements(By.id(id));
    }

    @Override
    public String getElementLocator() {
      return id;
    }

    @Override
    public String toString() {
      return "By.id: " + id;
    }
  }

  public static class ByClass extends By {
    private final String clazz;

    public ByClass(String clazz) {
      this.clazz = clazz;
    }

    @Override
    public AndroidElement findElement(SearchContext context) {
      return context.findElement(By.className(clazz));
    }

    @Override
    public List<AndroidElement> findElements(SearchContext context) {
      return context.findElements(By.className(clazz));
    }

    @Override
    public String getElementLocator() {
      return clazz;
    }

    @Override
    public String toString() {
      return "By.clazz: " + clazz;
    }
  }

  public static class ByTagName extends By {
    private final String tagName;

    public ByTagName(String tagName) {
      this.tagName = tagName;
    }

    @Override
    public AndroidElement findElement(SearchContext context) {
      return ((FindsByTagName) context).findElementByTagName(tagName);
    }

    @Override
    public List<AndroidElement> findElements(SearchContext context) {
      return ((FindsByTagName) context).findElementsByTagName((tagName));
    }

    @Override
    public String getElementLocator() {
      return tagName;
    }

    @Override
    public String toString() {
      return "By.tagName: " + tagName;
    }
  }

  public static class ByLinkText extends By {
    private final String text;

    public ByLinkText(String text) {
      this.text = text;
    }

    @Override
    public AndroidElement findElement(SearchContext context) {
      return context.findElement(By.linkText(text));
    }

    @Override
    public List<AndroidElement> findElements(SearchContext context) {
      return context.findElements(By.linkText(text));
    }

    @Override
    public String getElementLocator() {
      return text;
    }

    @Override
    public String toString() {
      return "By.text: " + text;
    }
  }

  public static class ByPartialLinkText extends By {
    private final String text;

    public ByPartialLinkText(String text) {
      this.text = text;
    }

    @Override
    public AndroidElement findElement(SearchContext context) {
      return context.findElement(By.partialLinkText(text));
    }

    @Override
    public List<AndroidElement> findElements(SearchContext context) {
      return context.findElements(By.partialLinkText(text));
    }

    @Override
    public String getElementLocator() {
      return text;
    }

    @Override
    public String toString() {
      return "By.partialText: " + text;
    }
  }

  public static class ByName extends By {
    private final String name;

    public ByName(String name) {
      this.name = name;
    }

    @Override
    public AndroidElement findElement(SearchContext context) {
      return context.findElement(this);
    }

    @Override
    public List<AndroidElement> findElements(SearchContext context) {
      return context.findElements(this);
    }

    @Override
    public String getElementLocator() {
      return name;
    }
  }

  public static class ByXPath extends By {
    private final String xpathExpression;

    public ByXPath(String xpathExpression) {
      this.xpathExpression = xpathExpression;
    }

    @Override
    public AndroidElement findElement(SearchContext context) {
      return context.findElement(this);
    }

    @Override
    public List<AndroidElement> findElements(SearchContext context) {
      return context.findElements(this);
    }

    @Override
    public String getElementLocator() {
      return xpathExpression;
    }
  }

  public static By cssSelector(String css) {
    if (css == null) throw new IllegalArgumentException("Cannot find elements when css is null.");
    return new ByCssSelector(css);
  }

  /**
   * @param id The value of the "id" attribute to search for
   * @return a By which locates elements by the value of the "id" attribute.
   */
  public static By id(final String id) {
    if (id == null)
      throw new IllegalArgumentException("Cannot find elements with a null id attribute.");

    return new ById(id);
  }

  public static By tagName(final String tagName) {
    if (tagName == null)
      throw new IllegalArgumentException("Cannot find elements when tag name is null.");

    return new ByTagName(tagName);
  }

  public static By linkText(final String text) {
    if (text == null)
      throw new IllegalArgumentException("Cannot find elements when text is null.");

    return new ByLinkText(text);
  }

  public static By partialLinkText(final String text) {
    if (text == null)
      throw new IllegalArgumentException("Cannot find elements when text is null.");

    return new ByPartialLinkText(text);
  }

  public static By name(String name) {
    if (name == null)
      throw new IllegalArgumentException("Cannot find elements when name is null.");
    return new ByName(name);
  }

  public static By xpath(String xpathExpression) {
    if (xpathExpression == null)
      throw new IllegalArgumentException("Cannot find elements when xpath is null.");
    return new ByXPath(xpathExpression);
  }

  public static By className(String className) {
    if (className == null)
      throw new IllegalArgumentException("Cannot find elements when className is null.");
    return new ByClass(className);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    By by = (By) o;

    return toString().equals(by.toString());
  }

  /**
   * Find a single element. Override this method if necessary.
   * 
   * @param context A context to use to find the element
   * @return The AndroidElement that matches the selector
   */
  public abstract AndroidElement findElement(SearchContext context);

  /**
   * Find many elements.
   * 
   * @param context A context to use to find the element
   * @return A list of AndroidElement matching the selector
   */
  public abstract List<AndroidElement> findElements(SearchContext context);

  public abstract String getElementLocator();

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public String toString() {
    // A stub to prevent endless recursion in hashCode()
    return "[unknown locator]";
  }
}
