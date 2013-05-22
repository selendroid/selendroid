/*
 * Copyright 2013 selendroid committers.
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
package io.selendroid.server.e2e;

import io.selendroid.builder.SelendroidServerBuilderTest;
import io.selendroid.server.model.SelendroidDriverTests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selendroid.SelendroidCapabilities;
import org.openqa.selendroid.SelendroidDriver;
import org.openqa.selendroid.device.DeviceTargetPlatform;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SessionCreationE2ETest {
  final String testAppApk = SelendroidServerBuilderTest.APK_FILE;
  final int port = 5555;

  @Test
  public void assertThatSessionCanBeStartedAndStopped1() throws Exception {
    testMethod();
  }

  @Test
  public void assertThatSessionCanBeStartedAndStopped2() throws Exception {
    testMethod();
  }

  private void testMethod() throws Exception {
    SelendroidCapabilities capa =
        SelendroidCapabilities.emulator(DeviceTargetPlatform.ANDROID16,
            SelendroidDriverTests.TEST_APP_ID);

    WebDriver driver = new SelendroidDriver("http://10.243.80.142:" + port + "/wd/hub", capa);
    WebElement inputField = driver.findElement(By.id("my_text_field"));
    Assert.assertEquals(inputField.getAttribute("enabled"), "true");

    driver.quit();
  }
}
