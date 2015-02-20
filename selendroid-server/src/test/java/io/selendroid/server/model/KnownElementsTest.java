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
package io.selendroid.server.model;


import android.view.View;
import android.webkit.WebView;
import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.android.InstrumentedKeySender;
import io.selendroid.server.android.KeySender;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KnownElementsTest {
  @Test
  public void testAddNativeElement() {
    KnownElements ke = new KnownElements();
    String id = ke.add(createNativeElement(ke));
    // verify uuid is used and is valid
    Assert.assertTrue(isValidUuid(id));
  }

  public static boolean isValidUuid(String uuid) {
    if (uuid == null) return false;
    try {
      // we have to convert to object and back to string because the built in fromString does not
      // have
      // good validation logic.
      UUID fromStringUUID = UUID.fromString(uuid);
      String toStringUUID = fromStringUUID.toString();
      return toStringUUID.equals(uuid);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Test
  public void testAddWebElement() {
    KnownElements ke = new KnownElements();
    String webElementId = ":wdc:123456789";
    String id = ke.add(createWebElement(webElementId, ke));
    Assert.assertEquals(webElementId, id);
  }

  @Test
  public void testGetIdOfNativeElement() {
    KnownElements ke = new KnownElements();
    AndroidElement element = createNativeElement(ke);
    String id = ke.add(element);

    Assert.assertEquals(id, ke.getIdOfElement(element));
  }

  @Test
  public void testGetIdOfWebElement() {
    KnownElements ke = new KnownElements();
    AndroidElement element = createWebElement(":wdc:1234", ke);
    String id = ke.add(element);

    Assert.assertEquals(id, ke.getIdOfElement(element));
  }

  @Test
  public void testGetIdOfWebElementWithMultipleElements() {
    KnownElements ke = new KnownElements();
    AndroidElement nativeElement = createNativeElement(ke);
    String nativeId = ke.add(nativeElement);
    AndroidElement element = createWebElement(":wdc:1234", ke);
    String id = ke.add(element);
    ke.add(createWebElement(":wdc:1235", ke));
    ke.add(createWebElement(":wdc:1236", ke));
    // this recreates an element with the same ID and replaces it in cache
    // which it *should* do in my opinion. commenting out for the purposes of this test now.
    // ke.add(createNativeElement(ke));
    ke.add(createWebElement(":wdc:1237", ke));
    ke.add(createWebElement(":wdc:1238", ke));
    Assert.assertEquals(nativeId, ke.getIdOfElement(nativeElement));
    Assert.assertEquals(id, ke.getIdOfElement(element));

  }

  @Test
  public void testGetIdONativeElementAddedTwice() {
    KnownElements ke = new KnownElements();
    AndroidElement element = createNativeElement(ke);
    String id = ke.add(element);
    AndroidElement anotherOTheSame = createNativeElement(ke);
    String anotherId = ke.add(anotherOTheSame);

    Assert.assertEquals(id, ke.getIdOfElement(element));
    Assert.assertEquals(anotherId, ke.getIdOfElement(anotherOTheSame));
    Assert.assertNotSame(id, anotherId);
  }

  @Test
  public void testGetIdOfWebElementAddedTwice() {
    KnownElements ke = new KnownElements();
    AndroidElement element = createWebElement(":wdc:1234", ke);
    String id = ke.add(element);
    ke.add(createWebElement(":wdc:1234", ke));

    Assert.assertEquals(id, ke.getIdOfElement(element));
  }

  @Test
  public void testGetIdONativeElement() {
    KnownElements ke = new KnownElements();
    AndroidElement element = createNativeElement(ke);
    String id = ke.add(element);

    Assert.assertEquals(id, ke.getIdOfElement(element));
  }

  private AndroidElement createNativeElement(KnownElements ke) {
    return createNativeElement(ke, 815);
  }

  private AndroidElement createNativeElement(KnownElements ke, int id) {
    View view = mock(View.class);
    when(view.getId()).thenReturn(id);

    ServerInstrumentation instrumentation = mock(ServerInstrumentation.class);
    KeySender keys = new InstrumentedKeySender(instrumentation);
    return Factories.getAndroidNativeElementFactory().createAndroidNativeElement(view, instrumentation, keys, ke);
  }

  private AndroidElement createWebElement(String id, KnownElements ke) {
    WebView view = mock(WebView.class);
    SelendroidWebDriver driver = mock(SelendroidWebDriver.class);

    return new AndroidWebElement(id, view, driver, ke);
  }
}
