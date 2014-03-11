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
package io.selendroid;

import io.selendroid.device.DeviceTargetPlatform;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class SelendroidCapabilitiesTest {

  @Test
  public void testInstanstiateFromJSON() throws Exception {
    JSONObject jsonSource = new JSONObject();
    jsonSource.put("browserName", "selendroid");
    jsonSource.put("platformVersion", DeviceTargetPlatform.ANDROID16.getApi());
    jsonSource.put("locale", "de_DE");
    jsonSource.put("aut", "io.selendroid.testapp:0.9.0");
    jsonSource.put("screenSize", "320x480");

    SelendroidCapabilities capa = new SelendroidCapabilities(jsonSource);
    // it is not set by default
    Assert.assertEquals(null, capa.getEmulator());
    Assert.assertEquals("selendroid", capa.getBrowserName());
    Assert.assertEquals("16", capa.getPlatformVersion());
    Assert.assertEquals("de_DE", capa.getLocale());
    Assert.assertEquals("io.selendroid.testapp:0.9.0", capa.getAut());
    Assert.assertEquals("320x480", capa.getScreenSize());
    Assert.assertEquals(5, capa.asMap().size());
  }

  @Test
  public void testDefaultInitialize() {
    SelendroidCapabilities capa = new SelendroidCapabilities();
    Assert.assertEquals(null, capa.getEmulator());
    Assert.assertEquals(2, capa.asMap().size());
  }
}
