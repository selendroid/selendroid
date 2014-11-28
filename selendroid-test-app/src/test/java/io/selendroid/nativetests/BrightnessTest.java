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
package io.selendroid.nativetests;

import static org.junit.Assert.assertEquals;
import io.selendroid.client.ScreenBrightness;
import io.selendroid.support.BaseAndroidTest;

import org.junit.Before;
import org.junit.Test;

public class BrightnessTest extends BaseAndroidTest {


  @Before
  public void openApp() throws Exception {
    openStartActivity();
  }

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
