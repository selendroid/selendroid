selendroid-grid-plugin
======================

Steps to use selendroid together with the Selenium Grid on a local machine:

* Download *selenium-server-standalone* from the [Selenium Project](http://code.google.com/p/selenium/downloads/)
* ```git clone https://github.com/DominikDary/selendroid.git ```
* ```cd selendroid-grid-plugin ```
* ``` mvn package ```
* copy the file *target/selendroid-grid-plugin-0.3.jar* to the same folder where the *selenium-server-standalone* is stored.

Now you can start the Grid with the selendroid-grid-plugin that contains the proxy *org.openqa.selendroid.grid.SelendroidSessionProxy* and a simple capability matcher *org.openqa.selendroid.grid.SelendroidCapabilityMatcher*:

```
java -Dfile.encoding=UTF-8 -cp "selendroid-grid-plugin-0.3.jar:selenium-server-standalone-2.31.0.jar" org.openqa.grid.selenium.GridLauncher -capabilityMatcher org.openqa.selendroid.grid.SelendroidCapabilityMatcher -role hub -host 127.0.0.1 -port 4444
```

Before registering a selendroid-node into the Grid please make sure the emulator and the selendroid test server are already started.
Registering nodes to this grid can be done using this registration request (file name selendroid-nodes-config.json):

```
{
	"capabilities": [{
		"deviceName": "emulator",
		"browserName": "selendroid",
		"version":"",
		"maxInstances": 1,
		"locale": "UK",
		"sdkVersion": "4.1",
		"screenSize": "480x800",
		"aut": "selendroid-test-app:0.3",
		"maxInstances" = 1,
		"rotatable" = true,
		"platform":"ANDROID",
		"takesScreenshot" = true
	}],
	"configuration": {
        "maxSession": 1,
        "register": true,
        "hubHost": "localhost",
        "hubPort": 4444,
        "remoteHost":"http://localhost:8080",
        "proxy":"org.openqa.selendroid.grid.SelendroidSessionProxy"
	}
}
```

The node registration can be done by sending the node config to the hub:

```
curl -H "Content-Type: application/json" -X POST --data @selendroid-nodes-config.json http://localhost:4444/grid/register
  
```
