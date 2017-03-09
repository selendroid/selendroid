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

import java.util.Map;
import java.util.HashMap;

import android.os.Bundle;
import io.selendroid.server.extension.BootstrapHandler;
import io.selendroid.server.common.utils.SelendroidArguments;

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
  private final Map<String, String> extraArgs = new HashMap();

  public InstrumentationArguments(Bundle arguments) {
    mainActivityClassName = arguments.getString(SelendroidArguments.MAIN_ACTIVITY);
    intentUri = arguments.getString(SelendroidArguments.INTENT_URI);
    intentAction = arguments.getString(SelendroidArguments.INTENT_ACTION);
    serviceClassName = arguments.getString(SelendroidArguments.SERVICE);
    loadExtensions = Boolean.parseBoolean(arguments.getString(SelendroidArguments.LOAD_EXTENSIONS));
    bootstrapClassNames = arguments.getString(SelendroidArguments.BOOTSTRAP);
    serverPort = arguments.getString(SelendroidArguments.SERVER_PORT);

    for (String key : arguments.keySet()) {
      if (!SelendroidArguments.KNOWN_ARGUMENTS.contains(key)) {
        extraArgs.put(key, arguments.getString(key));
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

  public Map<String, String> getExtraArgs() {
    return extraArgs;
  }

  public String getExtraArg(String key) {
    return extraArgs.get(key);
  }
}
