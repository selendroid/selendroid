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
package io.selendroid.support;

import io.selendroid.android.AndroidSdk;
import io.selendroid.io.ShellCommand;
import io.selendroid.server.util.HttpClientUtil;
import org.apache.commons.exec.CommandLine;
import org.junit.BeforeClass;


public class BaseAndroidTest extends AbstractAndroidTest{
  @BeforeClass
  public static void startSelendroidServer() throws Exception {
    CommandLine startSelendroid = new CommandLine(AndroidSdk.adb());
    startSelendroid.addArgument("shell");
    startSelendroid.addArgument("am");
    startSelendroid.addArgument("instrument");
    startSelendroid.addArgument("-e");
    startSelendroid.addArgument("main_activity");
    startSelendroid.addArgument("io.selendroid.testapp.HomeScreenActivity");
    startSelendroid.addArgument("io.selendroid/.ServerInstrumentation");
    ShellCommand.exec(startSelendroid);
    CommandLine forwardPort = new CommandLine(AndroidSdk.adb());
    forwardPort.addArgument("forward");
    forwardPort.addArgument("tcp:8080");
    forwardPort.addArgument("tcp:8080");

    ShellCommand.exec(forwardPort);
    // instrumentation needs a beat to come up before connecting right away
    // without this the first test often will fail, there's a similar wait
    // in the selendroid-standalone
    HttpClientUtil.waitForServer(8080);
  }
}
