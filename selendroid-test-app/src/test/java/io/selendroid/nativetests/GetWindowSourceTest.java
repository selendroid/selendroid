/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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

import io.selendroid.TestGroups;
import io.selendroid.support.BaseAndroidTest;
import io.selendroid.util.JsonXmlUtil;

import java.io.StringReader;

import static io.selendroid.waiter.TestWaiter.waitFor;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Test(groups = {TestGroups.NATIVE})
public class GetWindowSourceTest extends BaseAndroidTest {
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
        "{io.selendroid.testapp/HomeScreenActivity}");
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
        "io.selendroid.testapp:id/startUserRegistration");
    Assert.assertEquals(startUserRegistration.get("type").getAsString(), "Button");
    Assert
        .assertEquals(startUserRegistration.get("value").getAsString(), "Start User Registration");

    JsonObject buttonTest = children.get(1).getAsJsonObject();
    Assert.assertEquals(buttonTest.get("name").getAsString(),
        "io.selendroid.testapp:id/buttonTest");
    Assert.assertEquals(buttonTest.get("type").getAsString(), "Button");
    Assert.assertEquals(buttonTest.get("value").getAsString(), "EN Button",
        "Depends on the device locale");

    JsonObject my_text_field = children.get(3).getAsJsonObject();
    Assert.assertEquals(my_text_field.get("name").getAsString(),
        "io.selendroid.testapp:id/my_text_field");
    Assert.assertEquals(my_text_field.get("type").getAsString(), "EditText");
    Assert.assertEquals(my_text_field.get("value").getAsString(), "");


    JsonObject buttonStartWebview = children.get(2).getAsJsonObject();
    Assert.assertEquals(buttonStartWebview.get("name").getAsString(),
        "io.selendroid.testapp:id/buttonStartWebview");
    Assert.assertEquals(buttonStartWebview.get("type").getAsString(), "Button");
    Assert.assertEquals(buttonStartWebview.get("value").getAsString(), "Start Webview");

    JsonObject textview = children.get(0).getAsJsonObject();
    Assert.assertEquals(textview.get("name").getAsString(), "");
    Assert.assertEquals(textview.get("type").getAsString(), "TextView");
    Assert.assertEquals(textview.get("value").getAsString(),
        "Hello Default Locale, Selendroid-test-app!");
  }

  @Test
  public void testShouldBeAbleToFindHiddenElementAndGetShownState() throws Exception {
    Element textView =
        findElementByXpath("//TextView[@name='id/visibleTextView']", driver.getPageSource());

    Assert.assertEquals(textView.getAttribute("shown"), "false");
  }

  private Element findElementByXpath(String expr, String source) throws Exception {
    String xml = JsonXmlUtil.toXml(new JSONObject(source));
    InputSource is = new InputSource(new StringReader(xml));
    XPath xPath = XPathFactory.newInstance().newXPath();


    XPathExpression xpathExpr = xPath.compile(expr);
    return (Element) xpathExpr.evaluate(is, XPathConstants.NODE);
  }
}
