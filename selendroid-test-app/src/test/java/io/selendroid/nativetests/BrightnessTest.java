package io.selendroid.nativetests;

import io.selendroid.ScreenBrightness;
import io.selendroid.support.BaseAndroidTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BrightnessTest extends BaseAndroidTest {

  @Test
  public void shouldBeAbleToGetAndSetBrightness() throws InterruptedException {
    ScreenBrightness brightness = (ScreenBrightness) driver();

    brightness.setBrightness(0);
    int seen = brightness.getBrightness();
    assertEquals(0, seen);

    brightness.setBrightness(50);
    seen = brightness.getBrightness();
    assertEquals(50, seen);

    brightness.setBrightness(100);
    seen = brightness.getBrightness();
    assertEquals(100, seen);
  }
}
