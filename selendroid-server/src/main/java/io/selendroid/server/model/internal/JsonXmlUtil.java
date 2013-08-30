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
package io.selendroid.server.model.internal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JsonXmlUtil {
  public static Document toXml(JSONObject tree) {
    return buildXmlDoc(tree);
  }

  private static Document buildXmlDoc(JSONObject tree) {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try {
      builder = builderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    Document document = builder.newDocument();

    // document.setXMLEncoding("UTF-8");
    Element root = document.createElement("views");
    document.appendChild(root);
    buildXmlNode(tree, root, document);
    return document;
  }


  private static String extractTagName(String clazz) {
    if (clazz.contains(".")) {
      String[] elements = clazz.split("\\.");
      String simpleClassName = elements[elements.length - 1];
      if (simpleClassName.contains("$")) {
        String[] subElememts = simpleClassName.split("\\$");
        return subElememts[subElememts.length - 1];
      }
      return simpleClassName;
    }
    return clazz;
  }

  private static void buildXmlNode(JSONObject from, Element parent, Document document) {
    if (from == null) {
      return;
    }
    Element node = document.createElement(extractTagName(from.optString("type")));
    parent.appendChild(node);

    node.setAttribute("name", from.optString("name"));
    node.setAttribute("label", from.optString("label"));
    node.setAttribute("value", from.optString("value"));
    node.setAttribute("ref", from.optString("ref"));
    node.setAttribute("id", from.optString("id"));
    node.setAttribute("shown", from.optString("shown"));

    JSONArray array = from.optJSONArray("children");
    if (array != null) {
      for (int i = 0; i < array.length(); i++) {
        JSONObject n = array.optJSONObject(i);
        buildXmlNode(n, node, document);
      }
    }
  }
}
