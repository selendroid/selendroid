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
package io.selendroid.util;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonXmlUtil {
  public static String toXml(JSONObject tree) {
    return buildXmlDoc(tree).asXML();
  }

  private static Document buildXmlDoc(JSONObject tree) {
    Document document = DocumentHelper.createDocument();
    document.setXMLEncoding("UTF-8");
    Element root = document.addElement("views");
    buildXmlNode(tree, root);
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

  private static void buildXmlNode(JSONObject from, Element parent) {
    if (from == null) {
      return;
    }
    Element node =
        parent.addElement(extractTagName(from.optString("type")))
            .addAttribute("name", from.optString("name"))
            .addAttribute("label", from.optString("label"))
            .addAttribute("value", from.optString("value"))
            .addAttribute("ref", from.optString("ref"))
            .addAttribute("shown", from.optString("shown"));

    JSONArray array = from.optJSONArray("children");
    if (array != null) {
      for (int i = 0; i < array.length(); i++) {
        JSONObject n = array.optJSONObject(i);
        buildXmlNode(n, node);
      }
    }
  }
}
