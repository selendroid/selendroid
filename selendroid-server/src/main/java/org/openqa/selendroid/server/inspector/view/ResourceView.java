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
package org.openqa.selendroid.server.inspector.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.openqa.selendroid.ServerInstrumentation;
import org.openqa.selendroid.server.inspector.InspectorServlet;
import org.openqa.selendroid.server.inspector.SelendroidInspectorView;
import org.openqa.selendroid.server.model.SelendroidDriver;
import org.openqa.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class ResourceView extends SelendroidInspectorView {
  public static final String SCREENSHOT = "deviceScreenshot.png";

  public ResourceView(ServerInstrumentation serverInstrumentation, SelendroidDriver driver) {
    super(serverInstrumentation, driver);
  }

  @Override
  public void render(HttpRequest request, HttpResponse httpResponse) {
    httpResponse.charset(Charset.forName("UTF-8"));
    httpResponse.status(200);
    if (request.uri().endsWith(SCREENSHOT)) {
      httpResponse.header("Content-Type", "text/html");
      byte[] screenshot = driver.takeScreenshot();
      if (screenshot == null) {
        SelendroidLogger.log("screenshot is null");
      }
      httpResponse.content(screenshot);
    } else {
      try {
        String filename =
            "inspector" + request.uri().replaceFirst(InspectorServlet.INSPECTOR_RESSOURCE, "");
        InputStream asset = serverInstrumentation.getContext().getAssets().open(filename);

        httpResponse.content(toByteArray(asset));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    httpResponse.end();
  }

  private byte[] toByteArray(InputStream in) throws IOException {
    ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
    int next = in.read();
    while (next > -1) {
      arrayOutputStream.write(next);
      next = in.read();
    }
    arrayOutputStream.flush();
    byte[] byteArray = arrayOutputStream.toByteArray();
    arrayOutputStream.close();
    return byteArray;
  }
}
