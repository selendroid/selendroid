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
package io.selendroid.server.inspector;

import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.model.internal.JsonXmlUtil;

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

public class TreeUtil {
  public static JSONObject createFromNativeWindowsSource(JSONObject from) throws JSONException {
    JSONObject node = new JSONObject();
    node.put("data", getNodeTitle(from));
    node.put("id", getNodeTitle(from));

    // add an id to the node to make them selectable by :reference
    JSONObject attr = new JSONObject();
    attr.put("id", from.getString("ref"));
    node.put("attr", attr);

    JSONObject metadata = new JSONObject();
    metadata.put("type", from.getString("type"));
    metadata.put("reference", from.getString("ref"));
    metadata.put("id", from.getString("id"));
    metadata.put("name", from.getString("name"));
    metadata.put("value", from.opt("value"));
    metadata.put("l10n", from.getJSONObject("l10n"));
    metadata.put("shown", from.getBoolean("shown"));
    metadata.put("source", from.optString("source"));
    metadata.put("error", from.optString("error"));

    node.put("metadata", metadata);
    JSONObject rect = new JSONObject();
    rect.put("x", from.getJSONObject("rect").getJSONObject("origin").getInt("x"));
    rect.put("y", from.getJSONObject("rect").getJSONObject("origin").getInt("y"));
    rect.put("h", from.getJSONObject("rect").getJSONObject("size").getInt("height"));
    rect.put("w", from.getJSONObject("rect").getJSONObject("size").getInt("width"));
    metadata.put("rect", rect);

    JSONArray children = from.optJSONArray("children");

    if (children != null && children.length() != 0) {
      JSONArray jstreeChildren = new JSONArray();
      node.put("children", jstreeChildren);
      for (int i = 0; i < children.length(); i++) {
        Object child = null;
        try {
          child = children.get(i);
        } catch (JSONException e) {
          // ignore
        }
        if (child != null) {
          JSONObject jstreenode = createFromNativeWindowsSource((JSONObject) child);
          jstreeChildren.put(jstreenode);
        }
      }
    }

    return node;
  }

  private static String getNodeTitle(JSONObject node) throws JSONException {
    StringBuilder b = new StringBuilder();
    b.append("[" + node.optString("type") + "]");
    String name = node.optString("id");
    if (name != null && name.isEmpty() == false) {
      if (name.length() > 18) {
        name = name.substring(0, 15) + "...";
      }
      b.append("-" + name);
    }
    return b.toString();
  }

  public static String getXMLSource(JSONObject source) {
    Document document = JsonXmlUtil.buildXmlDocument(source);

    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = null;
    try {
      transformer = tFactory.newTransformer();
    } catch (TransformerConfigurationException e) {
      throw new SelendroidException(e);
    }

    transformer.setParameter("encoding", "UTF-8");

    DOMSource domSource = new DOMSource(document);
    Writer outWriter = new StringWriter();

    StreamResult result = new StreamResult(outWriter);
    try {
      transformer.transform(domSource, result);
    } catch (TransformerException e) {
      throw new SelendroidException(e);
    }

    return outWriter.toString();
  }
}
