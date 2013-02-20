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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.android.KeySender;
import org.openqa.selendroid.android.WindowType;
import org.openqa.selendroid.server.Session;
import org.openqa.selendroid.server.exceptions.SelendroidException;
import org.openqa.selendroid.util.SelendroidLogger;

import android.graphics.Bitmap;
import android.view.View;

import com.google.gson.JsonObject;

public abstract class AbstractSelendroidDriver implements SelendroidDriver {
  protected boolean done = false;
  protected SearchContext nativeSearchScope;
  protected WebviewSearchScope webviewSearchScope = null;
  protected ServerInstrumentation serverInstrumentation = null;
  protected Session session = null;
  protected final Object syncObject = new Object();
  private KeySender keySender = null;


  public AbstractSelendroidDriver(ServerInstrumentation instrumentation) {
    serverInstrumentation = instrumentation;
    keySender = new KeySender(serverInstrumentation);
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

    AndroidElement found = by.findElement(getSearchContext());

    while (found == null
        && (System.currentTimeMillis() - start <= serverInstrumentation.getAndroidWait()
            .getTimeoutInMillis())) {
      sleepQuietly(400);
      found = by.findElement(getSearchContext());
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

    List<AndroidElement> found = by.findElements(getSearchContext());
    while (found.isEmpty()
        && (System.currentTimeMillis() - start <= serverInstrumentation.getAndroidWait()
            .getTimeoutInMillis())) {
      sleepQuietly(200);
      found = by.findElements(getSearchContext());
    }
    return found;
  }

  private SearchContext getSearchContext() {
    if (session == null) {
      throw new SelendroidException("No Actice session found.");
    }
    if (WindowType.NATIVE_APP.equals(session.getActiveWindowType())) {
      return nativeSearchScope;
    } else {
      return webviewSearchScope;
    }

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
   * @see org.openqa.selenium.android.server.AndroidDriver#getSessionCapabilities(java.lang.String)
   */
  @Override
  public JsonObject getSessionCapabilities(String sessionId) {
    SelendroidLogger.log("session: " + sessionId);
    SelendroidLogger.log("capabilities: " + session.getCapabilities());
    return session.getCapabilities();
  }

  protected void sleepQuietly(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException cause) {
      Thread.currentThread().interrupt();
      throw new SelendroidException(cause);
    }
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
    nativeSearchScope = null;
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
    long end =
        System.currentTimeMillis() + serverInstrumentation.getAndroidWait().getTimeoutInMillis();
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

    waitForDone(end, serverInstrumentation.getAndroidWait().getTimeoutInMillis(),
        "Failed to take screenshot.");
    return rawPng[0];
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

  public void switchWindow(WindowType type) {
    session.setActiveWindowType(WindowType.WEBVIEW);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.openqa.selenium.android.server.AndroidDriver#initializeSessionForCapabilities(com.google
   * .gson.JsonObject)
   */
  @Override
  public String initializeSession(JsonObject desiredCapabilities) {
    if (this.session != null) {
      throw new SelendroidException(
          "There is currently one active session. Not more than one session is possible.");
    }
    this.session =
        new Session(desiredCapabilities, UUID.randomUUID().toString(), WindowType.NATIVE_APP);
    nativeSearchScope =
        new NativeSearchScope(serverInstrumentation, getSession().getKnownElements());

    serverInstrumentation.startMainActivity();

    SelendroidLogger.log("new s: " + session.getSessionId());
    return session.getSessionId();
  }

  @Override
  public Keyboard getKeyboard() {
    return keySender.getKeyboard();
  }
}
