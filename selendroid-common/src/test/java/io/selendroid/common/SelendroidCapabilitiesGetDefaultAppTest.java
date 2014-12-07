package io.selendroid.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.selendroid.common.SelendroidCapabilities;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Makes sure that the new function "getDefaultApp" will return the correct application under test from the apps store
 * or peacefully returns null if the app is not in the apps store. If the laucch activity is also specified with appName
 * then just return the appName so it can be installed to the apps store.
 *
 * The tests will cover these scenarios:
 * <ul>
 *
 * <li>If app does not exists in the apps store and the launch activity is specified, then just return
 * the request string as app under test so it can be installed to the device by the SelendroidStandaloneDriver. </li>
 * <li>If app does not exists in the appstore, and the launch activity is not specified then return null.</li>
 * <li>If appName and appVersion are specified then return the app under test as "appName:appVersion" if it is found in the
 * apps store or null if it is not.</li>
 * <li>If appName is specified but not the appVersion then return the latest version of the apps in the apps store as app
 * under test in the format of "appName:appVersion" or null if app is not found in apps store.</li>
 * </li>
 * <ul>
 */
public class SelendroidCapabilitiesGetDefaultAppTest {
  // Unsorted List of String represents supported apps in the apps store.
  private static final String elements[] = { "selendroid",
            "io.selendroid.test", "io.selendroid.test:0.12.1",
            "io.selendroid.test:0.11.1", "io.selendroid.test:0.11.0",
            "io.selendroid.test:0.12.0", };
  private static final Set supportedApps = new HashSet(Arrays.asList(elements));

  @Test
  public void testShouldReturnAppWithSpecifiedLaunchActivityEvenIfNotInStore() throws Exception {
    JSONObject jsonSource = new JSONObject();
    // app is not in store
    jsonSource.put("aut", "io.selendroid.test:0.13.0");
    jsonSource.put("launchActivity", "HomeScreenActivity");

    SelendroidCapabilities capa = new SelendroidCapabilities(jsonSource);

    String defaultApp = capa.getDefaultApp(supportedApps);
    Assert.assertEquals("io.selendroid.test:0.13.0", defaultApp);
  }

  @Test
  public void testReturnsTheLatestVersionOfApp() throws Exception {
    JSONObject jsonSource = new JSONObject();
    jsonSource.put("aut", "io.selendroid.test");

    SelendroidCapabilities capa = new SelendroidCapabilities(jsonSource);

    String defaultApp = capa.getDefaultApp(supportedApps);
    Assert.assertEquals("io.selendroid.test:0.12.1", defaultApp);
  }

  @Test
  public void testReturnsAnExactMatchWithoutSpecifiedVersion() throws Exception {
    JSONObject jsonSource = new JSONObject();
    jsonSource.put("aut", "selendroid");

    SelendroidCapabilities capa = new SelendroidCapabilities(jsonSource);

    String defaultApp = capa.getDefaultApp(supportedApps);
    Assert.assertEquals("selendroid", defaultApp);
  }

  @Test
  public void testReturnsAnExactMatchWithSpecifiedVersion() throws Exception {
    JSONObject jsonSource = new JSONObject();
    jsonSource.put("aut", "io.selendroid.test:0.11.0");

    SelendroidCapabilities capa = new SelendroidCapabilities(jsonSource);

    String defaultApp = capa.getDefaultApp(supportedApps);
    Assert.assertEquals("io.selendroid.test:0.11.0", defaultApp);
  }

  @Test
  public void testReturnsNullIfNoMatchingAppFound() throws Exception {
    JSONObject jsonSource = new JSONObject();
    jsonSource.put("aut", "io.selendroid.test2");

    SelendroidCapabilities capa = new SelendroidCapabilities(jsonSource);

    String defaultApp = capa.getDefaultApp(supportedApps);
    Assert.assertNull(defaultApp);
  }

  @Test
  public void testReturnsNullIfOnlyVersionIsSpecified() throws Exception {
    JSONObject jsonSource = new JSONObject();
    jsonSource.put("aut", "0.12.0");

    SelendroidCapabilities capa = new SelendroidCapabilities(jsonSource);

    String defaultApp = capa.getDefaultApp(supportedApps);
    Assert.assertNull(defaultApp);
  }

  @Test
  public void testResturnsNullIfAppAndVersionNotFound() throws Exception {
    JSONObject jsonSource = new JSONObject();
    jsonSource.put("aut", "io.selendroid.test:0.13.0");

    SelendroidCapabilities capa = new SelendroidCapabilities(jsonSource);

    String defaultApp = capa.getDefaultApp(supportedApps);
    Assert.assertNull(defaultApp);
  }

}
