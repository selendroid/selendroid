package io.selendroid.server.e2e;

import io.selendroid.SelendroidLauncher;
import io.selendroid.server.model.SelendroidDriverTests;
import io.selendroid.server.util.HttpClientUtil;

import java.net.URL;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selendroid.SelendroidCapabilities;
import org.openqa.selendroid.device.DeviceTargetPlatform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.android.AndroidDriver;

public class SessionCreationE2ETest {
  final String testAppApk = "../selendroid-test-app/target/selendroid-test-app-0.4-SNAPSHOT.apk";

  @Test
  public void assertThatSessionCanBeStartedAndStopped() throws Exception {
    String[] args = new String[] {"-aut", testAppApk};
    SelendroidLauncher.main(args);
    Thread.sleep(2000);

    SelendroidCapabilities capa =
        SelendroidCapabilities.emulator(DeviceTargetPlatform.ANDROID16,
            SelendroidDriverTests.TEST_APP_ID);
    WebDriver driver = new AndroidDriver(new URL("http://localhost:4444/wd/hub"), capa);
    JSONObject response =
        HttpClientUtil.parseJsonResponse(HttpClientUtil.executeRequest(
            "http://localhost:4444/wd/hub/status", HttpMethod.GET));
    Assert.assertEquals(0, response.getInt("status"));
    driver.quit();
  }
}
