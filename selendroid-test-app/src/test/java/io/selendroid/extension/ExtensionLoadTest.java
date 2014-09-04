package io.selendroid.extension;

import io.selendroid.support.BaseAndroidExtensionTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExtensionLoadTest extends BaseAndroidExtensionTest {
  @Test
  public void extensionCallShouldSucceed() {
    assertEquals("I'm an extension!",
        driver().callExtension("io.selendroid.extension.DemoExtensionHandler"));
  }
}
