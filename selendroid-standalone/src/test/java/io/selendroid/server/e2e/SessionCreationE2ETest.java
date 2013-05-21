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
  public void assertThatSessionCanBeStartedAndStopped() throws Exception {
    SelendroidCapabilities capa =
        SelendroidCapabilities.emulator(DeviceTargetPlatform.ANDROID16,
            SelendroidDriverTests.TEST_APP_ID);

    WebDriver driver = new SelendroidDriver("http://localhost:" + port + "/wd/hub", capa);
    WebElement inputField = driver.findElement(By.id("my_text_field"));
    Assert.assertEquals(inputField.getAttribute("enabled"), "true");

    driver.quit();
  }
}
