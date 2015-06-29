package io.selendroid.extension;

import org.junit.Ignore;
import org.junit.Test;

import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.support.BaseAndroidTest;

import static org.junit.Assert.assertEquals;

public class ExtensionLoadTest extends BaseAndroidTest {
  @Test
  @Ignore("Unable to load the extension?")
  public void extensionCallShouldSucceed() {
    assertEquals("I'm an extension!",
        driver().callExtension("io.selendroid.extension.DemoExtensionHandler"));
  }

  @Override
  protected SelendroidCapabilities getDefaultCapabilities() {
    SelendroidCapabilities caps = super.getDefaultCapabilities();
    caps.setSelendroidExtensions("extension.dex"); // This path doesn't work and wasn't working before?
    return caps;
  }
}
