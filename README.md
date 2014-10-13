This Fork
=========

Wraps selendroid into an npm module for easy inclusion into [appium](appium.io)

For publishing: auto-compiles all the java targets when `npm publish` is run, but **BEWARE!! the `main` key in package.json must be switched to the new version number or appium will not be able to find the .apk file.**


Selendroid
==========

[![Build Status](https://travis-ci.org/selendroid/selendroid.png?branch=master)](https://travis-ci.org/selendroid/selendroid)

Selendroid is a test automation framework which drives of the UI of Android native and hybrid applications (apps) and the mobile web with Selendroid. Tests are written using the Selenium 2 client API and for testing the application under test must not be modified.

Selendroid can be used on emulators and real devices and can be integrated as a node into the Selenium Grid for scaling and parallel testing.


You want more details?
----------------------

Check out our [documentation](http://selendroid.io).


Maven Artifacts
---------------

You can find the current version in Maven central and the latest snapshot version here: [https://oss.sonatype.org/content/repositories/snapshots/](https://oss.sonatype.org/content/repositories/snapshots/)


License
-----------
[The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
