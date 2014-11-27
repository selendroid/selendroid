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
package io.selendroid.server.extension;

import android.app.Instrumentation;
import android.content.Context;
import dalvik.system.DexClassLoader;
import io.selendroid.server.common.BaseRequestHandler;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.util.SelendroidLogger;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Singleton that manages Selendroid extensions.
 */
public class ExtensionLoader {
  private ClassLoader classLoader;
  private boolean isWithExtension = false;

  public ExtensionLoader(Context context) {
    this.classLoader = context.getClassLoader();
    SelendroidLogger.info("No extension dex path provided. Not loading an extension.");
  }

  public ExtensionLoader(Context context, String extensionDexPath) {
    if (!new File(extensionDexPath).exists()) {
      throw new SelendroidException(
          "Extension dex file not found, have you pushed it to the device? " + extensionDexPath);
    }

    String optimizedDexPath = context.getDir("dexables", 0).getAbsolutePath();

    this.classLoader = new DexClassLoader(
        extensionDexPath,
        optimizedDexPath,
        null,  // libraryPath
        context.getClassLoader());
    this.isWithExtension = true;
    SelendroidLogger.info("Extension path: " + extensionDexPath);
  }

  /**
   * Run bootstrap all BootstrapHandler#runBeforeApplicationCreate classes provided in order.
   */
  public void runBeforeApplicationCreateBootstrap(
      Instrumentation instrumentation, String[] bootstrapClasses) {
    if (!isWithExtension) {
      SelendroidLogger.error("Cannot run bootstrap. Must load an extension first.");
      return;
    }

    for (String bootstrapClassName : bootstrapClasses) {
      try {
        SelendroidLogger.info("Running beforeApplicationCreate bootstrap: " + bootstrapClassName);
        loadBootstrap(bootstrapClassName).runBeforeApplicationCreate(instrumentation);
        SelendroidLogger.info("\"Running beforeApplicationCreate bootstrap: " + bootstrapClassName);
      } catch (Exception e) {
        throw new SelendroidException("Cannot run bootstrap " + bootstrapClassName, e);
      }
    }
  }

  /**
   * Run bootstrap all BootstrapHandler#runAfterApplicationCreate classes provided in order.
   */
  public void runAfterApplicationCreateBootstrap(
      Instrumentation instrumentation, String[] bootstrapClasses) {
    if (!isWithExtension) {
      SelendroidLogger.error("Cannot run bootstrap. Must load an extension first.");
      return;
    }

    for (String bootstrapClassName : bootstrapClasses) {
      try {
        SelendroidLogger.info("Running afterApplicationCreate bootstrap: " + bootstrapClassName);
        loadBootstrap(bootstrapClassName).runAfterApplicationCreate(instrumentation);
        SelendroidLogger.info("\"Running afterApplicationCreate bootstrap: " + bootstrapClassName);
      } catch (Exception e) {
        throw new SelendroidException(
            "Cannot run bootstrap " + bootstrapClassName, e);
      }
    }
  }

  /**
   * Loads a {@link BootstrapHandler} class from the extension dex.
   */
  private BootstrapHandler loadBootstrap(String bootstrapClassName)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    return classLoader.loadClass(bootstrapClassName).asSubclass(BootstrapHandler.class).newInstance();
  }

  /**
   * Loads a {@link BaseRequestHandler} class from the extension dex.
   */
  public BaseRequestHandler loadHandler(String handlerClassName, String uri)
      throws
      ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
      InvocationTargetException, InstantiationException {
    Class<? extends BaseRequestHandler> handlerClass =
        classLoader.loadClass(handlerClassName).asSubclass(BaseRequestHandler.class);
    Constructor<? extends BaseRequestHandler> constructor =
        handlerClass.getConstructor(String.class);
    return constructor.newInstance(uri);
  }
}