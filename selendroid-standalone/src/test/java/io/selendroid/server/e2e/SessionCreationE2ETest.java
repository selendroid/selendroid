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

import io.selendroid.SelendroidCapabilities;
import io.selendroid.SelendroidDriver;
import io.selendroid.device.DeviceTargetPlatform;
import io.selendroid.server.model.SelendroidDriverTests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SessionCreationE2ETest {
  @Test
  public void assertThatSessionCanBeExecutedOnAndroid10Emulator() throws Exception {
    testMethod(SelendroidCapabilities.emulator(DeviceTargetPlatform.ANDROID10,
        SelendroidDriverTests.TEST_APP_ID));
  }

  @Test
  public void assertThatSessionCanBeExecutedOnAndroid16Emulator() throws Exception {
    testMethod(SelendroidCapabilities.emulator(DeviceTargetPlatform.ANDROID16,
        SelendroidDriverTests.TEST_APP_ID));
  }

  @Test
  public void assertThatSessionCanBeExecutedOnAndroid17Device() throws Exception {
    SelendroidCapabilities capa =
        SelendroidCapabilities.device(DeviceTargetPlatform.ANDROID17,
            SelendroidDriverTests.TEST_APP_ID);
    capa.setScreenSize("720x1280");

    testMethod(capa);
  }

  private void testMethod(SelendroidCapabilities capa) throws Exception {
    WebDriver driver = new SelendroidDriver("http://localhost:5555/wd/hub", capa);
    WebElement inputField = driver.findElement(By.id("my_text_field"));
    Assert.assertEquals("true", inputField.getAttribute("enabled"));
    inputField.sendKeys("Selendroid");
    Assert.assertEquals("Selendroid", inputField.getText());
    driver.quit();
  }
}
