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

import io.selendroid.client.SelendroidDriver;
import io.selendroid.client.waiter.WaitingConditions;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.SelendroidLauncher;
import io.selendroid.standalone.log.LogLevelEnum;
import io.selendroid.standalone.server.model.impl.DefaultInitAndroidDevicesStrategy;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.TimeUnit;

import static io.selendroid.client.waiter.TestWaiter.waitFor;

public class BaseAndroidTest {
  protected static final String HOMESCREEN_ACTIVITY = "and-activity://io.selendroid.testapp.HomeScreenActivity";
  protected static final String USER_REGISTRATION_ACTIVITY = "and-activity://io.selendroid.testapp.RegisterUserActivity";
  private static SelendroidConfiguration conf = new SelendroidConfiguration();
  private static SelendroidLauncher launcher = new SelendroidLauncher(conf);

  private SelendroidDriver driver = null;
  public static final String NATIVE_APP = "NATIVE_APP";
  public static final String WEBVIEW = "WEBVIEW_0";

  public SelendroidDriver driver() {
    return driver;
  }

  @Before
  public void setup() throws Exception {
    createDriver(getDefaultCapabilities());
  }

  @After
  public void teardown() {
    closeDriver();
  }

  protected void createDriver(final DesiredCapabilities caps) throws Exception {
    driver = new SelendroidDriver(caps);
    driver().manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  protected void closeDriver() {
    if (driver() != null) {
      driver().quit();
    }
  }

  protected void openWebdriverTestPage(String page) {
    driver().switchTo().window(NATIVE_APP);
    driver().get("and-activity://" + "io.selendroid.testapp." + "WebViewActivity");
    waitFor(WaitingConditions.driverUrlToBe(driver(), "and-activity://WebViewActivity"));

    driver().context(WEBVIEW);
    driver().get(page);
    // waitFor(WaitingConditions.driverUrlToBe(driver(), page),15,TimeUnit.SECONDS);
  }

  protected void openStartActivity() {
    driver().context(NATIVE_APP);
    driver().get(HOMESCREEN_ACTIVITY);
  }

  protected SelendroidCapabilities getDefaultCapabilities() {
    SelendroidCapabilities caps = new SelendroidCapabilities();
    caps.setAut("io.selendroid.testapp:0.12.0-SNAPSHOT");
    caps.setLaunchActivity("io.selendroid.testapp.HomeScreenActivity");

    return caps;
  }

  @Rule
  public TestRule rule = new TestRule() {

    @Override
    public Statement apply(final Statement base, final Description description) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          System.out.println(String.format("%s.%s", description.getTestClass().getName(),
              description.getMethodName()));
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

  @BeforeClass
  public static void startSelendroidServer() throws Exception {
    conf.setLogLevel(LogLevelEnum.DEBUG);
    launcher.launchSelendroid(new DefaultInitAndroidDevicesStrategy());
  }

  @AfterClass
  public static void stopSelendroidServer() {
    launcher.stopSelendroid();
  }
}
