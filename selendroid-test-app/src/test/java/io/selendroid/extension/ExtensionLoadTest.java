package io.selendroid.extension;

import org.junit.Test;

import io.selendroid.support.BaseAndroidTest;

import static org.junit.Assert.assertEquals;

public class ExtensionLoadTest extends BaseAndroidTest {
  @Test
  public void extensionCallShouldSucceed() {
    assertEquals("I'm an extension!",
        driver().callExtension("io.selendroid.extension.DemoExtensionHandler"));
  }
}
