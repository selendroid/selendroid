package org.openqa.selendroid.server.model;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selendroid.ServerInstrumentation;

import android.view.View;
import android.webkit.WebView;

import java.util.UUID;

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
    String id = ke.add(createWebElement(":wdc:1234", ke));
    Assert.assertTrue(UUID.fromString(id) instanceof UUID);
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
    return new AndroidNativeElement(view, instrumentation, ke);
  }

  private AndroidElement createWebElement(String id, KnownElements ke) {
    WebView view = mock(WebView.class);
    SelendroidWebDriver driver = mock(SelendroidWebDriver.class);

    return new AndroidWebElement(id, view, driver, ke);
  }
}
