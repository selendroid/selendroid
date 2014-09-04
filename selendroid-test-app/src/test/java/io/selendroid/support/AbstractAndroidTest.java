package io.selendroid.support;

import io.selendroid.SelendroidCapabilities;
import io.selendroid.SelendroidDriver;
import io.selendroid.SelendroidLauncher;
import io.selendroid.waiter.WaitingConditions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static io.selendroid.waiter.TestWaiter.waitFor;

public abstract class AbstractAndroidTest {
  private static SelendroidDriver driver = null;
  protected SelendroidLauncher selendroidServerLauncher = null;
  final String pathSeparator = File.separator;
  public static final String NATIVE_APP = "NATIVE_APP";
  public static final String WEBVIEW = "WEBVIEW_0";

  public static SelendroidDriver driver() {
    return driver;
  }

  @Before
  public void setup() throws Exception {
    driver =
        new SelendroidDriver(new URL("http://localhost:8080/wd/hub"), getDefaultCapabilities());
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

    driver().context(WEBVIEW);
    driver().get(page);
    //waitFor(WaitingConditions.driverUrlToBe(driver(), page),15,TimeUnit.SECONDS);
  }

  protected void openStartActivity() {
    driver().context(NATIVE_APP);
    driver().get("and-activity://io.selendroid.testapp.HomeScreenActivity");
  }

  protected DesiredCapabilities getDefaultCapabilities() {
    return SelendroidCapabilities
        .emulator("io.selendroid.testapp:0.8.0-SNAPSHOT")
        .setSelendroidExtensions("src/test/resources/extension.dex");
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
}
