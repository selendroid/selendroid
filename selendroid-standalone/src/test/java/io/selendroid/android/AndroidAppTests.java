package io.selendroid.android;

import io.selendroid.android.impl.DefaultAndroidApp;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selendroid.exceptions.SelendroidException;

public class AndroidAppTests {
	private static final String apkFile = "src/test/resources/selendroid-test-app.apk";
	private static final String inValidApkFile = "src/test/resources/selendroid-test-app-invalid.apk";

	@Test
	public void testShouldBeAbleToExtractBasePackage() throws Exception {
		AndroidApp app = new DefaultAndroidApp(new File(apkFile));
		Assert.assertEquals(app.getBasePackage(),
				"org.openqa.selendroid.testapp");
	}

	@Test
	public void testShouldBeAbleToExtractMainAcivity() throws Exception {
		AndroidApp app = new DefaultAndroidApp(new File(apkFile));
		Assert.assertEquals(app.getMainActivity(),
				"org.openqa.selendroid.testapp.HomeScreenActivity");
	}

	@Test()
	public void testShouldNotBeAbleToExtractBasePackage() throws Exception {
		AndroidApp app = new DefaultAndroidApp(new File(inValidApkFile));
		try {
			app.getBasePackage();
			Assert.fail("On an invalid apk the base package should not be found.");
		} catch (SelendroidException e) {
			// expected
		}
	}

	@Test()
	public void testShouldNotBeAbleToExtractMainAcivity() throws Exception {
		AndroidApp app = new DefaultAndroidApp(new File(inValidApkFile));
		try {
			app.getMainActivity();
			Assert.fail("On an invalid apk the main activity should not be found.");
		} catch (SelendroidException e) {
			// expected
		}
	}
}
