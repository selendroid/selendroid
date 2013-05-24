Selendroid
==========

[![Build Status](https://api.travis-ci.org/DominikDary/selendroid.png)](https://travis-ci.org/DominikDary/selendroid)

Selendroid is a test automation framework which drives of the UI of Android native and hybrid applications (apps). Tests are written using the Selenium 2 client API and for testing the application under test must not be modified. 

Selendroid can be used on emulators and real devices and can be integrated as a node into the Selenium Grid for scaling and parallel testing. 


Latest News:
------------

* Selendroid Version 0.3 is released ([List of Features](https://github.com/DominikDary/selendroid/blob/master/changelog.md))

Getting started
---------------

Selendroid is based on the Android instrumentation framework, so therefor only testing one specific app is supported.

Like Selenium we offer as well a standalone server.

Please follow this steps to setup your machine:

1. Install Java and configure JAVA_HOME

2. Install [Android-Sdk](http://developer.android.com/sdk/index.html) and configure ANDROID_HOME

3. Create the emulators that you want to use

4. Download Selendroid standalone [here](here)

5. start the selendroid server:
   - `java -jar selendroid-standalone-0.4-standalone.jar -app selendroid-test-app-0.4.apk`
   
Selendroid-standalone is able to start already existing Android emulators during the test session. 
		
Run your tests
--------------

A test in Java using JUnit4 looks like this:

```java
    SelendroidCapabilities capa =
        SelendroidCapabilities.emulator(DeviceTargetPlatform.ANDROID16, "io.selendroid.testapp:0.4");

    WebDriver driver = new SelendroidDriver("http://localhost:5555/wd/hub", capa);
    WebElement inputField = driver.findElement(By.id("my_text_field"));
    Assert.assertEquals("true", inputField.getAttribute("enabled"));
    inputField.sendKeys("Selendroid");
    Assert.assertEquals("Selendroid", inputField.getText());
    driver.quit();
```
When running the test selendroid standalone is starting an Android emulator, the apps are installed on the device and the tests are executed and when the test is over the emulator will be closed.

You want more details?
----------------------

Check out our [wiki](https://github.com/DominikDary/selendroid/wiki/).

