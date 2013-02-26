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
package org.openqa.selendroid.server.inspector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class TreeUtil {
  public static JsonObject createFromNativeWindowsSource(JsonObject from) {
    JsonObject node = new JsonObject();
    node.addProperty("data", getNodeTitle(from));
    node.addProperty("id", getNodeTitle(from));

    // add an id to the node to make them selectable by :reference
    JsonObject attr = new JsonObject();
    attr.addProperty("id", from.get("ref").getAsString());
    node.add("attr", attr);

    JsonObject metadata = new JsonObject();
    metadata.addProperty("type", from.get("type").getAsString());
    metadata.addProperty("reference", from.get("ref").getAsString());
    metadata.addProperty("label", from.get("label").getAsString());
    metadata.addProperty("name", from.get("name").getAsString());
    if (from.has("value")) {
      JsonElement value = from.get("value");
      if (value instanceof JsonNull) {} else {
        metadata.addProperty("value", value != null ? value.getAsString() : "");
      }
    }
    metadata.add("l10n", from.getAsJsonObject("l10n"));


    node.add("metadata", metadata);
    JsonObject rect = new JsonObject();
    rect.addProperty("x", from.getAsJsonObject("rect").getAsJsonObject("origin").get("x")
        .getAsInt());
    rect.addProperty("y", from.getAsJsonObject("rect").getAsJsonObject("origin").get("y")
        .getAsInt());
    rect.addProperty("h", from.getAsJsonObject("rect").getAsJsonObject("size").get("height")
        .getAsInt());
    rect.addProperty("w", from.getAsJsonObject("rect").getAsJsonObject("size").get("width")
        .getAsInt());
    metadata.add("rect", rect);



    JsonArray children = from.getAsJsonArray("children");

    if (children != null && children.size() != 0) {
      JsonArray jstreeChildren = new JsonArray();
      node.add("children", jstreeChildren);
      for (int i = 0; i < children.size(); i++) {
        JsonObject child = (JsonObject) children.get(i);
        JsonObject jstreenode = createFromNativeWindowsSource(child);
        jstreeChildren.add(jstreenode);
      }
    }

    return node;
  }



  private static String getNodeTitle(JsonObject node) {
    StringBuilder b = new StringBuilder();
    b.append("[" + node.get("type") + "]-");

    String name = node.get("name").getAsString();

    if (name != null) {
      if (name.length() > 18) {
        name = name.substring(0, 15) + "...";
      }
      b.append(name);
    }
    return b.toString();
  }
}
