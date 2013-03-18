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
package org.openqa.selendroid.server.internal;

import java.util.HashMap;
import java.util.Map;

public class Capabilities {
  public static final String AUT = "aut";
  public static final String LOCALE = "locale";
  public static final String MAX_INSTANCES = "maxInstances";
  public static final String NAME = "browserName";
  public static final String SDK_VERSION = "sdkVersion";

  public static Capabilities android(String app, String sdkVersion, String locale) {
    Capabilities res = new Capabilities();
    res.setCapability(NAME, "selendroid");
    res.setCapability(LOCALE, locale);
    res.setCapability(AUT, app);
    res.setCapability(SDK_VERSION, sdkVersion);
    res.setCapability(MAX_INSTANCES, 1);
    return res;
  }

//  public static Capabilities fromJSON(JSONObject capa) {
//    Capabilities desiredCapability = new Capabilities();
//    desiredCapability.setName(capa.get(Capabilities.NAME).getAsString());
//    desiredCapability.setMaxInstances(capa.get(Capabilities.MAX_INSTANCES).getAsInt());
//    desiredCapability.setLocale(capa.get(Capabilities.LOCALE).getAsString());
//
//    desiredCapability.setSDKVersion(capa.get(Capabilities.SDK_VERSION).getAsString());
//    desiredCapability.setAut(capa.get(Capabilities.AUT).getAsString());
//
//    return desiredCapability;
//  }

  private final Map<String, Object> raw = new HashMap<String, Object>();

  public Capabilities() {}

  public Capabilities(Map<String, Object> from) {
    raw.putAll(from);
  }

  public String getApplication() {
    Object o = raw.get(AUT);
    return ((String) o);
  }

  public String getLocale() {
    Object o = raw.get(LOCALE);
    return ((String) o);
  }

  public String getMaxInstances() {
    Object o = raw.get(MAX_INSTANCES);
    return ((String) o);
  }

  public String getSDKVersion() {
    Object o = raw.get(SDK_VERSION);
    return ((String) o);
  }

  public void setCapability(String key, Object value) {
    raw.put(key, value);
  }

  public void setLocale(String locale) {
    raw.put(LOCALE, locale);
  }

  public void setMaxInstances(Integer maxInstances) {
    raw.put(MAX_INSTANCES, maxInstances);
  }

  public void setSDKVersion(String sdkVersion) {
    raw.put(SDK_VERSION, sdkVersion);
  }

  public void setName(String name) {
    raw.put(NAME, name);
  }

  public void setAut(String aut) {
    raw.put(AUT, aut);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getLocale() == null) ? 0 : getLocale().hashCode());
    result = prime * result + ((getSDKVersion() == null) ? 0 : getSDKVersion().hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {

    if (this == obj) return true;
    if (obj == null) return false;

    if (getClass() != obj.getClass()) return false;
    Capabilities other = (Capabilities) obj;

    if (getLocale() == null) {
      if (other.getLocale() != null) return false;
    } else if (!getLocale().equals(other.getLocale())) return false;

    if (getSDKVersion() == null) {
      if (other.getSDKVersion() != null) return false;
    } else if (!getSDKVersion().equals(other.getSDKVersion())) return false;
    return true;
  }

  public Map<String, Object> asMap() {
    return raw;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Selendroid Capabilities [raw=" + raw + ", getLocale()=" + getLocale()
        + ", getSDKVersion()=" + getSDKVersion() + "]";
  }
}
