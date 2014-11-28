package io.selendroid.server.model.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import io.selendroid.server.model.internal.JsonXmlUtil;
import io.selendroid.server.util.SelendroidLogger;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JsonXmlUtilTest {
  private Document document;

  @Before
  public void setup() throws Exception {
    String json =
        new FileUtils().readFileToString(new File("src/test/resources/source.json"), "UTF-8");
    JSONObject source = new JSONObject(new JSONTokener(json));
    JsonXmlUtil util = new JsonXmlUtil();
    document = util.buildXmlDocument(source);
  }

  @Test
  public void shouldFindSingleElement() throws Exception {
    List<Node> nodes = validateXPath("(//InternalListView)[1]", document);
    Assert.assertEquals(nodes.size(), 1);
  }

  /**
   * the <code>source.json</code> file contains two entries where the type contains the dollar sign.
   * Both scenarios (simple and full class name e.g.
   * <code>PullToRefreshListView$InternalListView</code>) are supported and both can be used.
   * 
   * @throws Exception
   */
  @Test
  public void shouldFindElementWithFullClassName() throws Exception {
    List<Node> nodes = validateXPath("//InternalListView", document);
    Assert.assertEquals(nodes.size(), 2);
  }

  private List<Node> validateXPath(String expression, Document xmlDocument) {
    List<Node> elements = new ArrayList<Node>();
    XPath xPath = XPathFactory.newInstance().newXPath();

    NodeList nodeList;
    try {
      // read a nodelist using xpath
      nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      SelendroidLogger.error("Failed to get NodeList from xPath", e);
      return elements;
    }

    if (nodeList != null && nodeList.getLength() > 0) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);
        if (node.getAttributes() == null) {
          continue;
        }
        elements.add(node);
      }
    }
    return elements;
  }
}
