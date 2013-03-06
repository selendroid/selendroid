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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.android.AndroidKeys;
import org.openqa.selendroid.android.AndroidWait;
import org.openqa.selendroid.android.ViewHierarchyAnalyzer;
import org.openqa.selendroid.android.internal.Point;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.exceptions.TimeoutException;

import android.graphics.Rect;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;

public class AndroidNativeElement implements AndroidElement {
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((view == null) ? 0 : view.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AndroidNativeElement other = (AndroidNativeElement) obj;
    if (view == null) {
      if (other.view != null) return false;
    } else if (!view.equals(other.view)) return false;
    return true;
  }

  // TODO revisit
  protected static final long DURATION_OF_LONG_PRESS = 750L;// (long)
                                                            // (ViewConfiguration.getLongPressTimeout()
                                                            // * 1.5f);
  private View view;
  private Collection<AndroidElement> children = new LinkedHashSet<AndroidElement>();
  private AndroidElement parent;
  private ServerInstrumentation instrumentation;

  public AndroidNativeElement(View view, ServerInstrumentation instrumentation) {
    this.view = view;
    this.instrumentation = instrumentation;
  }

  @Override
  public AndroidElement getParent() {
    return parent;
  }

  public boolean isDisplayed() {
    return view.hasWindowFocus() && view.isEnabled() && view.isShown() && (view.getWidth() > 0)
        && (view.getHeight() > 0);
  }

  private void waitUntilIsDisplayed() {
    AndroidWait wait = instrumentation.getAndroidWait();

    try {
      wait.until(new Function<Void, Boolean>() {
        @Override
        public Boolean apply(Void input) {
          return isDisplayed();
        }
      });
    } catch (TimeoutException exception) {
      throw new SelendroidException("You may only do passive read with element not displayed");
    }
  }

  protected void scrollIntoScreenIfNeeded() {
    // TODO REVIEW: similar to click method
    int[] location = new int[2];
    view.getLocationOnScreen(location);
    final int left = location[0];
    final int top = location[1];
    final int right = left + view.getWidth();
    final int bottom = top + view.getHeight();

    instrumentation.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        view.requestRectangleOnScreen(new Rect(left, top, right, bottom));
      }
    });

  }

  @Override
  public void enterText(CharSequence text) {
    final View viewview = view;
    instrumentation.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        viewview.requestFocus();
      }
    });
    click();
    send(text);
  }

  @Override
  public String getText() {
    if (view instanceof TextView) {
      return ((TextView) view).getText().toString();
    }
    System.err.println("not supported elment for getting the text: "
        + view.getClass().getSimpleName());
    return null;
  }

  @Override
  public void click() {
    waitUntilIsDisplayed();
    scrollIntoScreenIfNeeded();
    try {
      Thread.sleep(300);
    } catch (InterruptedException ignored) {}
    int[] xy = new int[2];
    view.getLocationOnScreen(xy);
    final int viewWidth = view.getWidth();
    final int viewHeight = view.getHeight();
    final float x = xy[0] + (viewWidth / 2.0f);
    float y = xy[1] + (viewHeight / 2.0f);

    clickOnScreen(x, y);
  }

  private void clickOnScreen(float x, float y) {
    final ServerInstrumentation inst = ServerInstrumentation.getInstance();
    long downTime = SystemClock.uptimeMillis();
    long eventTime = SystemClock.uptimeMillis();
    final MotionEvent event =
        MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
    final MotionEvent event2 =
        MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);

    try {
      inst.sendPointerSync(event);
      inst.sendPointerSync(event2);
      try {
        Thread.sleep(300);
      } catch (InterruptedException ignored) {}
    } catch (SecurityException e) {
      System.out.println("error while clicking element: " + e);
    }
  }

  public Integer getAndroidId() {
    int viewId = view.getId();
    return (viewId == View.NO_ID) ? null : viewId;
  }

  @Override
  public AndroidElement findElement(By c) throws NoSuchElementException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<AndroidElement> findElements(By by) throws NoSuchElementException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<AndroidElement> getChildren() {
    return children;
  }

  public void setParent(AndroidElement parent) {
    this.parent = parent;
  }

  public void addChild(AndroidElement child) {
    this.children.add(child);
  }

  public String toString() {
    StringBuilder string = new StringBuilder();
    string.append("id: " + view.getId());
    string.append("view class: " + view.getClass());
    string.append("view content desc: " + view.getContentDescription());

    return string.toString();
  }

  private static int indexOfSpecialKey(CharSequence string, int startIndex) {
    for (int i = startIndex; i < string.length(); i++) {
      if (AndroidKeys.hasAndroidKeyEvent(string.charAt(i))) {
        return i;
      }
    }
    return string.length();
  }

  protected void send(CharSequence string) {
    int currentIndex = 0;

    instrumentation.waitForIdleSync();

    while (currentIndex < string.length()) {
      char currentCharacter = string.charAt(currentIndex);
      if (AndroidKeys.hasAndroidKeyEvent(currentCharacter)) {
        // The next character is special and must be sent individually
        instrumentation.sendKeyDownUpSync(AndroidKeys.keyCodeFor(currentCharacter));
        currentIndex++;
      } else {
        // There is at least one "normal" character, that is a character
        // represented by a plain Unicode character that can be sent
        // with
        // sendStringSync. So send as many such consecutive normal
        // characters
        // as possible in a single String.
        int nextSpecialKey = indexOfSpecialKey(string, currentIndex);
        instrumentation.sendStringSync(string.subSequence(currentIndex, nextSpecialKey).toString());
        currentIndex = nextSpecialKey;
      }
    }
  }

  public JsonObject toJson() {
    JsonObject object = new JsonObject();
    JsonObject l10n = new JsonObject();
    l10n.addProperty("matches", 0);
    object.add("l10n", l10n);
    object.addProperty("label", Strings.nullToEmpty(String.valueOf(view.getContentDescription())));
    object.addProperty("name", getNativeId());
    JsonObject rect = new JsonObject();

    object.add("rect", rect);
    JsonObject origin = new JsonObject();
    int[] xy = new int[2];
    view.getLocationOnScreen(xy);
    origin.addProperty("x", xy[0]);
    origin.addProperty("y", xy[1]);
    rect.add("origin", origin);

    JsonObject size = new JsonObject();
    size.addProperty("height", view.getHeight());
    size.addProperty("width", view.getWidth());
    rect.add("size", size);

    object.addProperty("ref", view.getId());
    object.addProperty("type", view.getClass().getName());
    String value = null;
    if (view instanceof TextView) {
      value = String.valueOf(((TextView) view).getText());
    }
    object.addProperty("value", value);

    return object;
  }

  private String getNativeId() {
    return ViewHierarchyAnalyzer.getNativeId(view);
  }

  public View getView() {
    return view;
  }

  @Override
  public void clear() {
    final View viewview = view;
    instrumentation.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        viewview.requestFocus();
        if (viewview instanceof EditText) {
          ((EditText) viewview).setText("");
        }
      }
    });
  }

  @Override
  public void submit() {
    throw new UnsupportedOperationException();
  }

  /**
   * TODO use reflection
   */
  @Override
  public boolean isSelected() {
    if (view instanceof CheckBox) {
      return ((CheckBox) view).isChecked();
    }
    if (view instanceof RadioButton) {
      return ((RadioButton) view).isChecked();
    }
    throw new UnsupportedOperationException(
        "Is seleted is only available for checkboxes and Radio buttons.");
  }

  @Override
  public Point getLocation() {
    int[] xy = new int[2];
    view.getLocationOnScreen(xy);
    return new Point(xy[0], xy[1]);
  }
}
