package org.openqa.selendroid;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class SelendroidCapabilitiesTests {

  @Test
  public void testInstanstiateFromJSON() throws Exception {
    JSONObject jsonSource = new JSONObject();
    jsonSource.put("language", "de");
    jsonSource.put("browserName", "selendroid");
    jsonSource.put("androidTarget", "16");
    jsonSource.put("locale", "de_DE");
    jsonSource.put("aut", "org.openqa.selendroid.testapp:0.4");
    jsonSource.put("screenSize", "320x480");

    SelendroidCapabilities capa = new SelendroidCapabilities(jsonSource);
    //it is not set by default
    Assert.assertEquals(null, capa.getEmulator());
    Assert.assertEquals("de", capa.getLanguage());
    Assert.assertEquals("selendroid", capa.getBrowserName());
    Assert.assertEquals("16", capa.getAndroidTarget());
    Assert.assertEquals("de_DE", capa.getLocale());
    Assert.assertEquals("org.openqa.selendroid.testapp:0.4", capa.getAut());
    Assert.assertEquals("320x480", capa.getScreenSize());
    Assert.assertEquals(6, capa.asMap().size());
  }
  
  @Test
  public void testDefaultInitialize(){
    SelendroidCapabilities capa=new SelendroidCapabilities();
    Assert.assertEquals(true, capa.getEmulator());
    Assert.assertEquals(1, capa.asMap().size());
  }
}
