/*
 * Copyright 2013 eBay Software Foundation and selendroid committers.
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
package io.selendroid.standalone.builder;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AndroidDriverAPKBuilder {

  private static final String PREBUILD_WEBVIEW_APP_PATH_PREFIX = "/prebuild/android-driver-app-";

  public File extractAndroidDriverAPK() {
    InputStream is = AndroidDriverAPKBuilder.class.getResourceAsStream(PREBUILD_WEBVIEW_APP_PATH_PREFIX +
        SelendroidServerBuilder.getJarVersionNumber() + ".apk");

    try {
      File tmpAndroidDriver = File.createTempFile("android-driver", ".apk");
      IOUtils.copy(is, new FileOutputStream(tmpAndroidDriver));
      IOUtils.closeQuietly(is);
      return tmpAndroidDriver;
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
