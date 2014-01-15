package io.selendroid.driver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  MultipleWebviewHandlingTests.class,
  NavigationTests.class,
  SelendroidUnknownCommandHandlingTest.class,
  WindowHandlingTests.class,
})
public class DriverTestSuite {
  // the class remains empty,
  // used only as a holder for the above annotations

}
