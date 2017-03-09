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

package io.selendroid.server.common.utils;

import java.util.Set;
import java.util.HashSet;

public class SelendroidArguments {
  public static final String MAIN_ACTIVITY = "main_activity";
  public static final String INTENT_URI = "intent_uri";
  public static final String INTENT_ACTION = "intent_action";
  public static final String SERVICE = "service";
  public static final String LOAD_EXTENSIONS = "load_extensions";
  public static final String BOOTSTRAP = "bootstrap";
  public static final String SERVER_PORT = "server_port";

  public static final Set<String> KNOWN_ARGUMENTS = new HashSet<String>();

  static {
    KNOWN_ARGUMENTS.add(MAIN_ACTIVITY);
    KNOWN_ARGUMENTS.add(INTENT_URI);
    KNOWN_ARGUMENTS.add(INTENT_ACTION);
    KNOWN_ARGUMENTS.add(SERVICE);
    KNOWN_ARGUMENTS.add(LOAD_EXTENSIONS);
    KNOWN_ARGUMENTS.add(BOOTSTRAP);
    KNOWN_ARGUMENTS.add(SERVER_PORT);
  }
}
