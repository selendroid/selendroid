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
package io.selendroid.server;

import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.HashMap;

import io.selendroid.server.extension.BootstrapHandler;
import io.selendroid.server.common.utils.SelendroidArguments;
import io.selendroid.server.model.ExternalStorage;
import io.selendroid.server.util.SelendroidLogger;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * Parses arguments passed to instrumentation using 'adb shell am instrument'.
 */
public class InstrumentationArguments {
  // Copy to avoid holding reference to the Bundle
  private final String mainActivityClassName;
  private final String intentUri;
  private final String intentAction;
  private final String serviceClassName;
  private final boolean loadExtensions;
  private final String bootstrapClassNames;
  private final String serverPort;
  private final JSONObject extraArgs;

  public InstrumentationArguments(Bundle arguments) {
    mainActivityClassName = arguments.getString(SelendroidArguments.MAIN_ACTIVITY);
    intentUri = arguments.getString(SelendroidArguments.INTENT_URI);
    intentAction = arguments.getString(SelendroidArguments.INTENT_ACTION);
    serviceClassName = arguments.getString(SelendroidArguments.SERVICE);
    loadExtensions = Boolean.parseBoolean(arguments.getString(SelendroidArguments.LOAD_EXTENSIONS));
    bootstrapClassNames = arguments.getString(SelendroidArguments.BOOTSTRAP);
    serverPort = arguments.getString(SelendroidArguments.SERVER_PORT);
    extraArgs = readExtraArgsFile();
  }

  private JSONObject readExtraArgsFile() {
    File extraArgsFile = ExternalStorage.getExtraArgsFile();
    if (!extraArgsFile.exists()) {
      return new JSONObject();
    }

    try {
      return new JSONObject(readFile(extraArgsFile));
    } catch (JSONException e) {
      throw new RuntimeException(
        "Should never have a malformed JSON in extra args file",
        e
      );
    }
  }

  // There's no nio on Android and it's not worth importing Apache commons
  private String readFile(File file) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
      String line = null;
      StringBuilder sb = new StringBuilder();
      String separator = System.getProperty("line.separator");
      while ((line = reader.readLine()) != null) {
        sb.append(line).append(separator);
      }

      return sb.toString();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(
        "We already made sure the extra args file exists",
        e
      );
    } catch (IOException e) {
      throw new RuntimeException(
        "Error while reading from extra args file",
        e
      );
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          SelendroidLogger.error("Failed to close reader for args file", e);
        }
      }
    }
  }

  /** Full class name of the activity to start. */
  public String getActivityClassName() {
    return mainActivityClassName;
  }

  public String getIntentUri() {
    return intentUri;
  }

  public String getIntentAction() {
    return intentAction;
  }

  public String getServiceClassName() {
    return serviceClassName;
  }

  /** Should we load extensions (assumes they have already been pushed to the device) */
  public boolean isLoadExtensions() {
    return loadExtensions;
  }

  /** Full name of a {@link BootstrapHandler} class to run before starting the activity. */
  public String getBootstrapClassNames() {
    return bootstrapClassNames;
  }

  /** The port at which the server should listen. */
  public String getServerPort() {
    return serverPort;
  }

  public JSONObject getExtraArgs() {
    return extraArgs;
  }

  public Object getExtraArg(String key) {
    if (extraArgs.has(key)) {
      return null;
    }

    try {
      return extraArgs.get(key);
    } catch (JSONException e) {
      return null;
    }
  }
}
