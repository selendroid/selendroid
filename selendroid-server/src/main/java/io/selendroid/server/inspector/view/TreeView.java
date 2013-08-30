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
package io.selendroid.server.inspector.view;

import io.selendroid.ServerInstrumentation;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.inspector.SelendroidInspectorView;
import io.selendroid.server.inspector.TreeUtil;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.server.model.internal.JsonXmlUtil;
import io.selendroid.util.SelendroidLogger;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class TreeView extends SelendroidInspectorView {
  public TreeView(ServerInstrumentation serverInstrumentation, SelendroidDriver driver) {
    super(serverInstrumentation, driver);
  }

  public void render(HttpRequest request, HttpResponse response) throws JSONException {
    JSONObject source = null;
    try {
      source = (JSONObject) driver.getWindowSource();
    } catch (SelendroidException e) {
      SelendroidLogger.log("error getting WindowSource in TreeView", e);
      response.header("Content-type", "application/x-javascript").charset(Charset.forName("UTF-8"))
          .content("{}").end();
      return;
    }


    JSONObject convertedTree = TreeUtil.createFromNativeWindowsSource(source);
    convertedTree.getJSONObject("metadata").put("xml", getXMLSource(source));
    response.header("Content-type", "application/x-javascript").charset(Charset.forName("UTF-8"))
        .content(convertedTree.toString()).end();
  }

  private String getXMLSource(JSONObject source) {
    Document document = JsonXmlUtil.toXml(source);

    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = null;
    try {
      transformer = tFactory.newTransformer();
    } catch (TransformerConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    DOMSource domSource = new DOMSource(document);
    Writer outWriter = new StringWriter();
    StreamResult result = new StreamResult(outWriter);
    try {
      transformer.transform(domSource, result);
    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return outWriter.toString();
  }
}
