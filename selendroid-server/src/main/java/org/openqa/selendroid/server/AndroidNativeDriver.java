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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.android.WindowType;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.server.model.AndroidElement;
import org.openqa.selendroid.server.model.AndroidNativeElement;
import org.openqa.selendroid.server.model.By;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class AndroidNativeDriver implements AndroidDriver {
  private ServerInstrumentation serverInstrumentation = null;
  RootSearchScope searchScope = null;
  private final Object syncObject = new Object();
  private boolean done = false;
  static final long RESPONSE_TIMEOUT = 10000L;

  private Session session = null;

  public AndroidNativeDriver(ServerInstrumentation serverInstrumentation) {
    this.serverInstrumentation = serverInstrumentation;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#getSession()
   */
  @Override
  public Session getSession() {
    return session;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#getCurrentUrl()
   */
  @Override
  public String getCurrentUrl() {
    Activity activity = serverInstrumentation.getCurrentActivity();
    if (activity == null) {
      return null;
    }

    return "and-activity://" + activity.getLocalClassName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#getSessionCapabilities(java.lang.String)
   */
  @Override
  public JsonObject getSessionCapabilities(String sessionId) {
    System.out.println("session: " + sessionId);
    System.out.println("capabilities: " + session.getCapabilities());
    return session.getCapabilities();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openqa.selenium.android.server.AndroidDriver#initializeSessionForCapabilities(com.google
   * .gson.JsonObject)
   */
  @Override
  public String initializeSessionForCapabilities(JsonObject desiredCapabilities) {
    if (this.session != null) {
      throw new SelendroidException(
          "There is currently one active session. Not more than one session is possible.");
    }
    this.session =
        new Session(desiredCapabilities, UUID.randomUUID().toString(), WindowType.NATIVE_APP);
    searchScope = new RootSearchScope(serverInstrumentation, getSession().getKnownElements());

    serverInstrumentation.startMainActivity();

    System.out.println("new s: " + session.getSessionId());
    return session.getSessionId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#stopSession()
   */
  @Override
  public void stopSession() {
    serverInstrumentation.finishAllActivities();
    this.session = null;
    searchScope = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openqa.selenium.android.server.AndroidDriver#findElement(org.openqa.selenium.android.common
   * .model.By)
   */
  @Override
  public AndroidElement findElement(final By by) {
    if (by == null) {
      throw new IllegalArgumentException("By cannot be null.");
    }
    long start = System.currentTimeMillis();

    AndroidElement found = by.findElement(searchScope);

    while (found == null
        && (System.currentTimeMillis() - start <= serverInstrumentation.getAndroidWait()
            .getTimeoutInMillis())) {
      sleepQuietly(200);
      found = by.findElement(searchScope);
    }
    return found;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openqa.selenium.android.server.AndroidDriver#findElements(org.openqa.selenium.android.common
   * .model.By)
   */
  @Override
  public List<AndroidElement> findElements(By by) {
    if (by == null) {
      throw new IllegalArgumentException("By cannot be null.");
    }
    long start = System.currentTimeMillis();

    List<AndroidElement> found = by.findElements(searchScope);
    while (found.isEmpty()
        && (System.currentTimeMillis() - start <= serverInstrumentation.getAndroidWait()
            .getTimeoutInMillis())) {
      sleepQuietly(200);
      found = by.findElements(searchScope);
    }
    return found;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#getSourceOfCurrentActivity()
   */
  @Override
  public JsonObject getSourceOfCurrentActivity() {
    AndroidNativeElement rootElement = searchScope.getElementTree();
    JsonObject root = rootElement.toJson();
    root.addProperty("activity", serverInstrumentation.getCurrentActivity().getComponentName()
        .toShortString());
    addChildren(root, rootElement);

    return root;
  }

  private void addChildren(JsonObject parent, AndroidElement parentElement) {
    Collection<AndroidElement> children = parentElement.getChildren();
    if (children == null || children.isEmpty()) {
      return;
    }
    JsonArray childs = new JsonArray();
    for (AndroidElement child : children) {
      if (((AndroidNativeElement) child).getView().getId() != ((AndroidNativeElement) parentElement)
          .getView().getId() && ((AndroidNativeElement) child).getView().getId() != View.NO_ID) {
        JsonObject jsonChild = ((AndroidNativeElement) child).toJson();
        childs.add(jsonChild);

        addChildren(jsonChild, child);
      }
    }
    parent.add("children", childs);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openqa.selenium.android.server.AndroidDriver#takeScreenshot()
   */
  @Override
  public byte[] takeScreenshot() {
    final View view = serverInstrumentation.getRootView();
    if (view == null) {
      throw new SelendroidException("No open windows.");
    }
    done = false;
    long end = System.currentTimeMillis() + RESPONSE_TIMEOUT;
    final byte[][] rawPng = new byte[1][1];
    ServerInstrumentation.getInstance().getCurrentActivity().runOnUiThread(new Runnable() {
      public void run() {
        synchronized (syncObject) {
          Bitmap raw;
          view.setDrawingCacheEnabled(true);
          view.buildDrawingCache(true);
          raw = Bitmap.createBitmap(view.getDrawingCache());
          view.setDrawingCacheEnabled(false);

          ByteArrayOutputStream stream = new ByteArrayOutputStream();
          if (!raw.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
            throw new RuntimeException("Error while compressing screenshot image.");
          }
          try {
            stream.flush();
            stream.close();
          } catch (IOException e) {
            throw new RuntimeException("I/O Error while capturing screenshot: " + e.getMessage());
          } finally {
            IOUtils.closeQuietly(stream);
          }
          rawPng[0] = stream.toByteArray();
          done = true;
          syncObject.notify();
        }
      }
    });

    waitForDone(end, RESPONSE_TIMEOUT, "Failed to take screenshot.");
    return rawPng[0];
  }

  private static void sleepQuietly(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException cause) {
      Thread.currentThread().interrupt();
      throw new SelendroidException(cause);
    }
  }

  private void waitForDone(long end, long timeout, String error) {
    synchronized (syncObject) {
      while (!done && System.currentTimeMillis() < end) {
        try {
          syncObject.wait(timeout);
        } catch (InterruptedException e) {
          throw new SelendroidException(error, e);
        }
      }
    }
  }

}
