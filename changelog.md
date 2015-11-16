0.18.0-SNAPSHOT (under current development)
---
- uninstall before installing, since adb install -r doesn't appear to work correctly all the time [#994](../../issues/994)
- Adding gradle build system for selendroid [#998](../../isues/998)

0.17.0
---
- wrapping webviews in a better way, to handle hybrid apps that override methods on the webview(s)
- native element getAttribute now run on UI Thread, to access certain attributes on a WebView (like getScale)
- marshmallow support [#987](../../issues/987)
- add factory to allow custom drivers [#975](../../pull/975)
- merge device config into node capability for grid auto re-register [#963](../../pull/963)
- kill orphaned processes after test has finished [#961](../../pull/961)
- update selenium maven dependency [#946](../../issues/946)

0.16.0
---
- gets app version from pom.xml if class path doesn't start with jar [#835](../../issues/835)
- Clear data before app launch [#844](../../issues/844) 
- Drop explicit 'implements TakesScreenshot, JavascriptExecutor' declarations [#846](../../issues/746) 
- Trigger DOM event of type 'INPUT' after changing value of INPUT element [#746](../../issues/746)
- Grid auto re-register [#756](../../issues/756)
- Added activities to test touch gestures in the selendroid-test app [#848](../../issues/848) 
- implemented driver.manage().timeouts().PageLoadTimeout() [#849](../../issues/849)
- Added cordova-android 4.0.0 support [#859](../../issues/859)
- Append error details to page source [#850](../../issues/850) 
- Delete temporary created by Selendroid Server [#862](../../issues/862) 
- Refactored SafeHandle (Action chain) for future multitouch implementation. [#860](../../issues/860) 
- Changed the implicit wait timeout from 5s to 0 as per webdriver spec [#895](../../pull/895)
- Add WXGA720 skin for detection of screen resolution [#904](../../pull/904)
- Optimize screenshots from standalone server [#904](../../pull/904)
- Fix bug SelendroidServerBuilder when executing tests and using default keystore alias and passwords and androidKeyStore file exists [#904](../../pull/904)
- Don't add "offline" hardware devices on startup [#900](../../pull/900)
- [client] Fix SelendroidDriver#readCallLog() [#916](../../pull/916)
- [server] Fix finding a collection of elements by class name for non existing classes [#918](../../pull/918)
- [standalone] Fix `..session/log/types` endpoint [#927](../../pull/927)
- [standalone] Fix HTTP response for unknown commands [#928](../../pull/928)
- [server] Fix NPE in ViewHierarchyAnalyzer [#929](../../pull/929)
- [server] Do not try to get the resource name if the view has no id [#930](../../pull/930)
- [server] Do not search in multiple top level views [#931](../../pull/931)
- Update AndroidAtoms [#925](../../issues/925)

0.15.0
---
- Fix native view search with multiple top level views [#799](../../issues/799) 
- Reverted the 'adb.terminate()' call (back into if condition). [#803](../../issues/803) 
- Log click position [#801](../../issues/801)
- fix registration of selendroid server into a selenium grid [#802](../../issues/802) 
- fix NPE on native apps driver.switchTo().defaultContent() [#776](../../issues/776) 
- Fixing maven issues by using latest maven-android-plugin. [#804](../../issues/804)  
- Introduce Predicate and AndroidElement factories [#811](../../issues/811)  
- Fixing setAirplaneMode(true) for Android API 21 [#816](../../issues/816)  
- Using latest selenium release. [#818](../../issues/818) 
- Add assets for inspector [#820](../../issues/820)
- Added apiTargetType as an option to SelendroidCapabilities [#823](../../issues/823)

0.14.0
---
- Don't log exception when waiting for the Selendroid server to start
- fixed support for Cordova 3.6.x apps

0.13.0
---
- In case of an shutting down selendroid-standalone (using an already started emulator) this fix is fixing the NPE. 
- Read adb output. [#695](../../issues/695) 
- Selendroid-standalone argument -emulatorOptions to handle multiple options for emulator [#697](../../issues/697)
- Selendroid-standalone: Fix to ScreenSize detection of emulators, and a feature - configurable maxSession and maxInstances. [#703](../../issues/703) 
- Corrections to JSON to register at Grid hub. [#713](../../issues/713)
- Added folder monitoring capability and fixed emulator launching problem. [#712](../../issues/712)
- Introduced package per component. E.g. client bindings are located in ```io.selendroid.client```  [#725](../../issues/725)
- Correctly wait for Selendroid standalone server to come up 
- Consider 'emulator' capability when matching
- For each device, register as "android" for WebView tests and also selendroid if an aut is specified
- Elements don't need to be enabled to be visible. 
- Removed the adb command implementation, now done via automation
- Automatically selects the latest version of the app to test if it is not specified by users.
- terminate must be called on AndroidDebugBridge on shutdown. 
- Removed Gson dependency from server and client
- fixes #767 stop internal ExecutorService of TrafficCounter on shutdown
- Only return stack traces for unknown errors
- Adding "model" as a configurable parameter option.

0.12.0
---
- Support to custom keystore, password, alias. [#536](../../issues/536)
- Added freeing on selendroidPort on driver.quit() [#557](../../issues/557)
- The activity class is not any longer immediately loaded. [#558](../../issues/558)
- Fixes bug in findElementsByName() [#559](../../issues/559)
- Optimize searching for elements in selendroid [#560](../../issues/560)
- Allowed disabling of adb logcat device logging with flag [#561](../../issues/561)
- Cleaned up repetitive error handling in selendroid-server [#562](../../issues/562)
- Stop swallowing exception when waiting for instrumentation [#563](../../issues/563)
- Allow selendroid-server start timeout to be passed on command line [#564](../../issues/564)
- Added "-h" and" --help" options to selendroid-standalone-server.
- Split up SelendroidLauncher main().
- Added capability to load in extension handlers and a test bootstrap class
- Assume AUT is already installed if LaunchActivity specified & no matching appsStore entry [#567](../../issues/567)
- Propagate unhandled exceptions in AUT as AppCrashedException
- Added server-side support for low-level actions API
- Added client side implementation of the multi touch screen
- Added empty()/copyOf()/withMerged() to SelendroidCapabilities
- Added ability to run GC and set system properties.
- GetElementAttribute returns null if attribute is not set. Fixes [#568](../../issues/568)
- Removed manual looper as we can now get the device to dump crash logs to a file
- Added SelendroidStandaloneDriverEventListener to allow hooking into events during Selendroid startup
- Run bootstrap and launch main activity on the UI thread in ServerInstrumentation
- Fixed cloning of existing session capabilities in GetCapabilities()
- Added logging of selendroid traffic statistics
- Prevent hardware devices from showing up multiple times in the DeviceStore
- getWindowSize() now supports API levels < 13
- Changed getScreenSize() to use Dimension instead of string,
  added support for emulators with string skin names
- Fixed loading of keystores with no password
- Send connection: close header in responses. Fixes [#458](../../issues/458)
- Removed 'SelendroidKeys.ANDROID_HOME' because emulating the home key using Instrumentation is not supported in Android. Use adb to emulate the home key in your tests.
- Return capabilities immediately after session creation instead of redirecting
- Lowered severity of logs in isSelendroidRunning(), made messages more informative.
- Made E2E tests use SelendroidStandaloneServer
- findElements() returns an empty list instead of throwing an exception.
- Standardises URLs / makes them compliant with the WebDriver specification
- Fixed crashlog detection on older devices
- Improved cleanup of devices on failed server startup
- Marked 'execute_native' scripts as deprecated. The native script `TwoPointerGestureAction` was removed, please use the new multi touch implementation.
- Upgrading to latest Selenium 2.43.1
- Upgrading to latest Android Atoms

0.11.0
---
- Upgrading to latest Selenium 2.42.2
- Fixed http response encoding header
- Support for 'activity-alias' manifest tag by adding a new optional capability -launchActivity
- Adding new background / resume custom api endpoints, available in the client: 
  SelendroidDriver.backgroundApp() and SelendroidDriver.resumeApp()
- Avoid returning duplicate elements for findElementsBy 
- Migrated http-server from webbit to netty
- Adding new call log feature: SelendroidDriver.addCallLog / readCallLog
- Fixes: [#487](../../issues/487)

0.10.0
---
- Bumping selenium dependency to version 2.41.0
- Added support for switching contexts (NATIVE_APP/WEBVIEW)
- Removed deprecated property 'androidTarget' in selendroid capabilities.
- Removed default locale 'en_US' in selendroid capabilities
- Fixes: [#362](../../issues/362), [#371](../../issues/371), [#338](../../issues/338)
- Added command line option `-forceReinstall` to force installation of Selendroid Server & app under test
- Logging Selendroid Standalone configured options
- Element.tagName() should return lower-case string
- Fixed wrong mapping of StaleElementException (was WebDriverException)
- Added a unified and configurable logging system for standalone and selendroid server 
- adding support for new 'network_connection' endpoint which will be in the client API for selenium in 2.42

0.9.0
---
- Rotation Support
- Fixes: [#268](../../issues/268), [#284](../../issues/284), [#309](../../issues/309), [#275](../../issues/275)
- Adding support for disabling native events for sendKeys command to support e.g. German umlauts ([#7](../../issues/7),[#110](../../issues/110))
- Added support for sending key events, send text, tap and execute shell command via adb connection
- removed -installedApp command line parameter of the selendroid-standalone jar
- Selendroid standalone supports by default already started emulators. Even if they are started manually after selendroid-standalone has been started, they are identified and can be used for test sessions.
- Minimized create session time by skipping installation of the app under test or the selendroid-server if they are already installed.
- Support execute async script ([#254](../../issues/254))
- Add native execute script action TwoPointerGesture. this is temporary solution until the mobile WebDriver multi touch  spec is implemented [#292](../../issues/292)
- Introducing a sessionTimeout (in seconds) that will automatically stop a session. Default value is 30 minutes.
- Added support for using 'platformVersion', 'platformName' and 'automationName' in capabilities in order to support Selenium 3.0
- Added support for using switch context endpoints to support Selenium 3.0. Swith to window mechanism is still available.
- In order to support Selenium 3, Page source is now returned as an XML (or HTML in the case of HTML-based platforms) document representing the UI hierarchy.
- Until multi touch support based on the new w3c spec is implemented, there is a native script that can be used for multi touch gestures.
- Added a more detailed error message when instrumentation fails #328

0.8.0
---
- Default selendroid-standalone port is changed from 5555 to 4444
- Added support for switching to a frame in webview
- Fixes:  [#155](../../issues/155), [#163](../../issues/163), [#177](../../issues/177), [#179](../../issues/179), [#184](../../issues/184), [#188](../../issues/188), [#202](../../issues/202), [#209](../../issues/209), [#210](../../issues/210), [#211](../../issues/211), [#213](../../issues/213), [#214](../../issues/214),[#216](../../issues/216), [#223](../../issues/223), [#231](../../issues/231)
- Refactoring of our end-to-end tests
- Support for navigating back, forward and refresh
- removed -restartAdb option
- Adding Alert api support while in a webview
- adding -noClearData option to avoid calling adb shell pm clear  when calling driver.quit()


0.7.0
---
- Support for running AndroidDriver to test mobile web pages.
- Introduce an API to control the screen brightness and whether it's on.
- making screenshots work again for emulators and using the -installedApp option
- Avoid retaining references to Views that have been disposed.
- Prevent temporary APKs from cluttering the cwd.


0.6.0
-----
- [#114](../../issues/114) Optimizing logcat handling and retry handling for selendroid-server start
- Emulators are started on display configured in capabilities
- Device logcats are available as logging type "logcat"
- The feature to restart adb has been removed
- Selendroid-standalone can register himself as a node to a Selenium Grid hub by using command line parameters (-hub and -host)
- Selenium Automation Atoms has been updated to fix [#127](../../issues/127)
- Support for Android KitKat (Api Level 19)
- Full support for Windows [#146](../../issues/146)
- Support for running multiple instrumentation servers [#112](../../issues/112)
- Better support for different platform names Android Sdk [#128](../../issues/128)
- Smaller refactorings
- Selendroid-Standalone can be configured to use specific emulator starting options
- Selendroid-Standalone can be configured to use a specific keystore for signing apks

0.5.1
-----
- fix for 'Arbitrary port number binding is broken' [#105](../../issues/105)

0.5.0
---
- Added xpath locator for native context
- selendroid can now handle multiple web views on the same activity
- new inspector with the ability to display html source code of a web view.
- multiple bug fixes
- Support for Set, Get, Delete Cookies for WebViews
- hardware device handling is now based on the ddmlib library which means devices can be now hot plugged.
- selendroid-standalone can is now taking screenshots by default using ddmlib library.
- selendroid-shell has been removed from the core project
- selendroid-server port can now be configured
- Updated to use the lastest Selenium automation Atoms.
- Official support for windows
- Emulator and instrumentation start handling was massively improved
- selendroid-standalone can handle installed apps which is can speed up especially test case development.
- Selenium Log API is now supported


0.4.2
-----
- small bugfixes


0.4.1
-----
- several smaller bug fixes: [#52](../../issues/52), [#51](../../issues/51), [#50](../../issues/50)
- In the capabilities locale, emulator and androidTarget are now optional.

0.4.0
-------------

- moved all packages to domain selendroid.io
- added new component: selendroid-standalone
- moved the documentation from wiki to web page: http://selendroid.io
- selendroid gem has been refactored to support only starting selendroid-shell
- Removed dependencies: Guava, commons-io, slf4j-android
- Find By Tag Name refactored to find by the class now, not by text
- L10n Locators not longer available, the translation can be done executing the script: ((JavascriptExecutor) driver).executeScript("getL10nKeyTranslation", "l10nKey");
- Several bug fixes


0.3.2
-----
- Gem does support the new location of aapt.

0.3.1
-----
- Gem does require Ruby version >= 1.9.2



0.3
----
- Moved from google gson library to org.json
- Added first support of Selenium Advanced User Interactions API
- Added to the selendroid-gem new commands to support
   - build-and-start: automatically build the selendroid-server
     and resigns the given apk with the debug certificate and
     starts afterwards the selendroid server.
   - start: Starts the selendroid server on the first available Android device.
- Fix for bug [#6](../../issues/6) that solves send key to an element.
- Fixed a bug in getCapabilities handler that adds now e.g. support for javascript.
- Fixed a  bug in getElementLocation handler
- Added support for:
   - Get size of an element
   - Get attribute of an element
   - Get the info if an element is displayed or enabled
   - execute script (only supported for webview mode)
- Added css locator support in webviews
- *selendroid-grid-plugin* that adds support to use the selenium grid for parallel testing
- Fix for native UI hierarchy handling
- Several smaller bugfixes with regards to element handling
- Gem can now be use on Windows
- Gem source has been added to the main project
- Added FindByPartialLinkText locator support for native and webview elements
- Adding (experimental) mechanism to add 'executeScript' in Native


0.2
----
- fixed to major bugs [#1](../../issues/1) and [#2](../../issues/2)
- Created an inspector that allows to inspect the application under test and makes it easier to write test cases: http://localhost:8080/inspector
- Added support for pressing keys like the Android menu button.
- Fixed a bug in taking screenshots. Now pop up dialogs are included as well.
- Added /sessionId/keys support and a client library that contains already selenium as dependency and the interface SelendroidKeys.
- The gem contains an interactive shell that starts automatically selendroid-server and a Ruby webdriver client that allows to interactively try commands out.
- Added locator strategy by class for native and web view context
- added support for all available locators findElement, findElements and corresponding find child element and find child elements.

0.1
-----
- initial Selendroid version including support for android native and hybrid apps.
  - Driver support:
      - takeScreenshot
      - getPageSource
      - get current url/ activity and open activity
  - Driver supports following find element locators:
      - for web views: by id, by xpath, by text, by name
      - for Android native: by id, by l10n key, by text
  - Found elements features:
      - click, clear, enter text, getValue, is selected and submit

