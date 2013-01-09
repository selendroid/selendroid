/*
 * Copyright 2012 selendroid committers.
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
package org.openqa.selendroid.tests.internal;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.android.AndroidDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;

public class BaseAndroidTest {
  protected WebDriver driver = null;
  final String pathSeparator = File.separator;

  @Before
  public void setup() {
    driver = new AndroidDriver(getDefaultCapabilities());
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }

  @After
  public void teardown() {
    driver.quit();
  }

  protected DesiredCapabilities getDefaultCapabilities() {
    DesiredCapabilities capa = DesiredCapabilities.android();
    capa.setCapability("aut", "selendroid-test-app");
    capa.setCapability("locale", "de_DE");
    capa.setCapability("maxInstances", "1");
    capa.setCapability("browserName", "selendroid");
    return capa;
  }

  protected void takeScreenShoot() throws Exception {
    WebDriver augmentedDriver = new Augmenter().augment(driver);
    File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
    String nameScreenshot = UUID.randomUUID().toString() + ".png";
    String path = getPath(nameScreenshot);
    FileUtils.copyFile(screenshot, new File(path));
  }

  private String getPath(String nameTest) throws IOException {
    File directory = new File(".");

    String newFileNamePath =
        directory.getCanonicalPath() + pathSeparator + "target" + pathSeparator
            + "surefire-reports" + pathSeparator + "screenShots" + pathSeparator + nameTest;
    return newFileNamePath;
  }
}
