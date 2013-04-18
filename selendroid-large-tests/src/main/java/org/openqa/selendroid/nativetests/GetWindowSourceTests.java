package org.openqa.selendroid.nativetests;

import org.openqa.selendroid.TestGroups;
import org.openqa.selendroid.support.BaseAndroidTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
@Test(groups={TestGroups.NATIVE})
public class GetWindowSourceTests extends BaseAndroidTest {
  /**
   * TODO update test, because test app was refactored
   */
  @Test(enabled = false)
  public void nativeUiTreeIsBuildCorrectly() throws Exception {
    JsonObject root = new JsonParser().parse(driver.getPageSource()).getAsJsonObject();

    // Verify root element
    // Assert.assertEquals(root.get("name").getAsString(), "android:id/content");
    Assert.assertEquals(root.get("type").getAsString(),
        "com.android.internal.policy.impl.PhoneWindow$DecorView");
    Assert.assertEquals(root.get("activity").getAsString(),
        "{org.openqa.selendroid.testapp/org.openqa.selendroid.testapp.HomeScreenActivity}");
    JsonArray child = root.get("children").getAsJsonArray();
    Assert.assertTrue(child.size() == 1, "Child element count == 1");

    // Verify child LinearLayout
    JsonObject linearLayout = child.get(0).getAsJsonObject();
    Assert.assertEquals(linearLayout.get("type").getAsString(), "android.widget.LinearLayout");
    JsonArray children = linearLayout.get("children").getAsJsonArray();
    Assert.assertEquals(children.size(), 3, "Child element count == 5");

    System.out.println(children);
    // Verify main ui elements
    JsonObject startUserRegistration = children.get(2).getAsJsonObject();
    Assert.assertEquals(startUserRegistration.get("name").getAsString(),
        "org.openqa.selendroid.testapp:id/startUserRegistration");
    Assert.assertEquals(startUserRegistration.get("type").getAsString(), "Button");
    Assert
        .assertEquals(startUserRegistration.get("value").getAsString(), "Start User Registration");

    JsonObject buttonTest = children.get(1).getAsJsonObject();
    Assert.assertEquals(buttonTest.get("name").getAsString(),
        "org.openqa.selendroid.testapp:id/buttonTest");
    Assert.assertEquals(buttonTest.get("type").getAsString(), "Button");
    Assert.assertEquals(buttonTest.get("value").getAsString(), "EN Button",
        "Depends on the device locale");

    JsonObject my_text_field = children.get(3).getAsJsonObject();
    Assert.assertEquals(my_text_field.get("name").getAsString(),
        "org.openqa.selendroid.testapp:id/my_text_field");
    Assert.assertEquals(my_text_field.get("type").getAsString(), "EditText");
    Assert.assertEquals(my_text_field.get("value").getAsString(), "");


    JsonObject buttonStartWebview = children.get(2).getAsJsonObject();
    Assert.assertEquals(buttonStartWebview.get("name").getAsString(),
        "org.openqa.selendroid.testapp:id/buttonStartWebview");
    Assert.assertEquals(buttonStartWebview.get("type").getAsString(), "Button");
    Assert.assertEquals(buttonStartWebview.get("value").getAsString(), "Start Webview");

    JsonObject textview = children.get(0).getAsJsonObject();
    Assert.assertEquals(textview.get("name").getAsString(), "");
    Assert.assertEquals(textview.get("type").getAsString(), "TextView");
    Assert.assertEquals(textview.get("value").getAsString(),
        "Hello Default Locale, Selendroid-test-app!");
  }
}
