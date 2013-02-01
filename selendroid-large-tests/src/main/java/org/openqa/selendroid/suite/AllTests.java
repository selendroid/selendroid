package org.openqa.selendroid.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openqa.selendroid.tests.SayHelloWebviewTest;
import org.openqa.selendroid.tests.UserResgistrationTest;

@RunWith(Suite.class)
@SuiteClasses({SayHelloWebviewTest.class, UserResgistrationTest.class})
public class AllTests {

}
