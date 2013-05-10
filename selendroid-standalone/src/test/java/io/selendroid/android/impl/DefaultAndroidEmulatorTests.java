package io.selendroid.android.impl;

import io.selendroid.android.AndroidEmulator;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DefaultAndroidEmulatorTests {
  @Test
  public void shouldBeAbleToListAvds() throws Exception {
   List<AndroidEmulator> avds = DefaultAndroidEmulator.listAvailableAvds();
    Assert.assertFalse("Expecting list of avds not to be empty", avds.isEmpty());
  }
}
