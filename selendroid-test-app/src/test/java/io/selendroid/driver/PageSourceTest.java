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
package io.selendroid.driver;

import io.selendroid.support.BaseAndroidTest;
import io.selendroid.webviewdrivertests.HtmlTestData;

import java.io.StringReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.tree.tiny.TinyElementImpl;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;


public class PageSourceTest extends BaseAndroidTest {

  @Test()
  public void shouldGetNativePageSourceAsXML() throws Exception {
    openWebdriverTestPage(HtmlTestData.SAY_HELLO_DEMO);
    driver().switchTo().window(NATIVE_APP);
    String source = driver().getPageSource();
    Assert.assertTrue("source should contain a xml tag",
        source.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    TinyElementImpl node =
        findElementByXpath(
            "//DecorView/LinearLayout/FrameLayout/TableLayout/TableRow/SelendroidSpinner", source);
    Assert.assertEquals("should find selendroid spinner", "spinner_webdriver_test_data",
        node.getAttributeValue("", "id"));
  }


  @Test()
  public void shouldGetWebViewPageSourceAsHtml() throws Exception {
    openWebdriverTestPage(HtmlTestData.SAY_HELLO_DEMO);
    String source = driver().getPageSource();
    Assert.assertTrue("source should contain a xml tag", source.startsWith("<html>"));

    Assert.assertTrue("should have input", source.contains("<input"));
    Assert.assertTrue("should have audi option",
        source.contains("<option value=\"audi\">Audi</option>"));
  }

  private TinyElementImpl findElementByXpath(String expr, String xml) throws Exception {
    InputSource is = new InputSource(new StringReader(xml));
    XPath xPath = XPathFactory.newInstance().newXPath();

    XPathExpression xpathExpr = xPath.compile(expr);
    return (TinyElementImpl) xpathExpr.evaluate(is, XPathConstants.NODE);
  }
}
