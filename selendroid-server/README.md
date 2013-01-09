Selendroid project
==================

Using the selendroid server
---------------------------

* Change the "target_package" attribute in the AndroidApplication.xml
* Build the apk with the command: mvn clean install
* Install the apk
* start the selendroid server: 
		adb shell am instrument -e main_activity 'org.openqa.selendroid.testapp.HomeScreenActivity' org.openqa.selendroid.server/org.openqa.selendroid.server.ServerInstrumentation
* activate the port forwarding.
		adb forward tcp:8080 tcp:8080
* Now you can start one of large tests.

Running unit test in eclipse
----------------------------

If the execution of a selendroid-server junit test class fails with class not found error, try to "Disable Workspace Resolution".