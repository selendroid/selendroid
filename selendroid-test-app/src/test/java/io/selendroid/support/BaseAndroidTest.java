/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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

import static io.selendroid.waiter.TestWaiter.waitFor;
import io.selendroid.SelendroidCapabilities;
import io.selendroid.SelendroidDriver;
import io.selendroid.SelendroidLauncher;
import io.selendroid.android.AndroidSdk;
import io.selendroid.io.ShellCommand;
import io.selendroid.waiter.WaitingConditions;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.remote.DesiredCapabilities;


public class BaseAndroidTest {
  private static SelendroidDriver driver = null;
  protected SelendroidLauncher selendroidServerLauncher = null;
  final String pathSeparator = File.separator;
  public static final String NATIVE_APP = "NATIVE_APP";
  public static final String WEBVIEW = "WEBVIEW";

  public static SelendroidDriver driver() {
    return driver;
  }

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
  }

  @Before
  public void setup() throws Exception {
    driver = new SelendroidDriver("http://localhost:8080/wd/hub", getDefaultCapabilities());
    driver().manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  @AfterClass
  public static void teardown() {
    if (driver() != null) {
      driver().quit();
    }
  }

  protected void openWebdriverTestPage(String page) {
    driver().switchTo().window(NATIVE_APP);
    driver().get("and-activity://" + "io.selendroid.testapp." + "WebViewActivity");
    waitFor(WaitingConditions.driverUrlToBe(driver(), "and-activity://WebViewActivity"));

    driver().switchTo().window(WEBVIEW);
    driver().get(page);
    waitFor(WaitingConditions.driverUrlToBe(driver(), page));
  }

  protected void openStartActivity() {
    driver().switchTo().window(NATIVE_APP);
    driver().get("and-activity://io.selendroid.testapp.HomeScreenActivity");
  }

  protected DesiredCapabilities getDefaultCapabilities() {
    return SelendroidCapabilities.emulator("io.selendroid.testapp:0.8.0-SNAPSHOT");
  }

  @Rule
  public TestRule rule = new TestRule() {

    @Override
    public Statement apply(final Statement base, final Description description) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          System.out.println(String.format("%s.%s", description.getTestClass().getName(), description.getMethodName()));
          try {
            base.evaluate();
          } catch (Exception e) {
            e.printStackTrace();
            throw e;
          }
          System.out.println("test completed.");
        }
      };
    }
  };
}
