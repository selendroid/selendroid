0.3 (under development)
----
- Moved from google gson library to org.json
- Added first support of Selenium Advanced User Interactions API
- Added to the selendroid-gem new commands to support
   - build-and-start: automatically build the selendroid-server
     and resigns the given apk with the debug certificate and
     starts afterwards the selendroid server.
   - start: Starts the selendroid server on the first available Android device.
- Fix for bug #6 that solves send key to an element.
- Fixed a bug in getCapabilities handler that adds now e.g. support for javascript.
- Fixed a  bug in getElementLocation handler
- Added support for:
   - Get size of an element
   - Get attribute of an element
   - Get the info if an element is displayed or enabled
   - execute script (only supported for webview mode)
- Added css locator support in webviews

0.2 
----
- fixed to major bugs #1 and #2
- Created an inspector that allows to inspect the application under test and makes it easier to write test cases: http://localhost:8080/inspector
- Added support for pressing keys like the Android menu button.
- Fixed a bug in taking screenshots. Now pop up dialogs are included as well.  
- Added /sessionId/keys support and a client library that contains already selenium as dependency and the interface SelendroidKeys.
- The gem contains an interactive shell that starts automatically selendroid-server and a Ruby webdriver client that allows to interactively try commands out.
- Added locator strategy by class for native and web view context
- added support for all available locators findElement, findElements and corresponding find child element and din child elements.

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
      
