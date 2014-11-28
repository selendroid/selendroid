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
import io.selendroid.server.extension.BootstrapHandler;

/**
 * Parses arguments passed to instrumentation using 'adb shell am instrument'.
 */
public class InstrumentationArguments {
  // Copy to avoid holding reference to the Bundle
  private final String mainActivityClassName;
  private final boolean loadExtensions;
  private final String bootstrapClassNames;
  private final String serverPort;

  public InstrumentationArguments(Bundle arguments) {
    mainActivityClassName = arguments.getString("main_activity");
    loadExtensions = Boolean.parseBoolean(arguments.getString("load_extensions"));
    bootstrapClassNames = arguments.getString("bootstrap");
    serverPort = arguments.getString("server_port");
  }

  /** Full class name of the activity to start. */
  public String getActivityClassName() {
    return mainActivityClassName;
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
}
