package org.openqa.selendroid.server.model;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selendroid.ServerInstrumentation;

import android.view.View;
import android.webkit.WebView;

public class KnownElementsTest {
  @Test
  public void testAddNativeElement() {
    KnownElements ke = new KnownElements();
    Long id = ke.add(createNativeElement());
    Assert.assertEquals(new Long(1), id);
  }

  @Test
  public void testAddWebElement() {
    KnownElements ke = new KnownElements();
    Long id = ke.add(createWebElement(":wdc:1234"));
    Assert.assertEquals(new Long(1), id);
  }

  @Test
  public void testGetIdOfNativeElement() {
    KnownElements ke = new KnownElements();
    AndroidElement element = createNativeElement();
    Long id = ke.add(element);

    Assert.assertEquals(id, ke.getIdOfElement(element));
  }

  @Test
  public void testGetIdOfWebElement() {
    KnownElements ke = new KnownElements();
    AndroidElement element = createWebElement(":wdc:1234");
    Long id = ke.add(element);

    Assert.assertEquals(id, ke.getIdOfElement(element));
  }

  @Test
  public void testGetIdOfWebElementWithMultipleElements() {
    KnownElements ke = new KnownElements();
    AndroidElement nativeElement = createNativeElement();
    Long nativeId = ke.add(nativeElement);
    AndroidElement element = createWebElement(":wdc:1234");
    Long id = ke.add(element);
    ke.add(createWebElement(":wdc:1235"));
    ke.add(createWebElement(":wdc:1236"));
    ke.add(createNativeElement());
    ke.add(createWebElement(":wdc:1237"));
    ke.add(createWebElement(":wdc:1238"));
    Assert.assertEquals(nativeId, ke.getIdOfElement(nativeElement));
    Assert.assertEquals(id, ke.getIdOfElement(element));

  }

  @Test
  public void testGetIdONativeElementAddedTwice() {
    KnownElements ke = new KnownElements();
    AndroidElement element = createNativeElement();
    Long id = ke.add(element);
    ke.add(createNativeElement());

    Assert.assertEquals(id, ke.getIdOfElement(element));
  }

  @Test
  public void testGetIdOfWebElementAddedTwice() {
    KnownElements ke = new KnownElements();
    AndroidElement element = createWebElement(":wdc:1234");
    Long id = ke.add(element);
    ke.add(createWebElement(":wdc:1234"));

    Assert.assertEquals(id, ke.getIdOfElement(element));
  }

  @Test
  public void testGetIdONativeElement() {
    KnownElements ke = new KnownElements();
    AndroidElement element = createNativeElement();
    Long id = ke.add(element);

    Assert.assertEquals(id, ke.getIdOfElement(element));
  }

  private AndroidElement createNativeElement() {
    View view = mock(View.class);
    when(view.getId()).thenReturn(815);

    ServerInstrumentation instrumentation = mock(ServerInstrumentation.class);
    return new AndroidNativeElement(view, instrumentation);
  }

  private AndroidElement createWebElement(String id) {
    WebView view = mock(WebView.class);
    SelendroidWebDriver driver = mock(SelendroidWebDriver.class);

    return new AndroidWebElement(id, view, driver);
  }
}
