Selendroid
==========

Selendroid is an attempt to implement the Selenium JSON Wire Protocol for Android native and hybrid apps.

Getting started
---------------

Selendroid is based on the Android instrumentation framework, so therefor only testing one specific app is supported.
Selendroid-server in combination with the application under test (aut) must be installed on the device in order to be able to run automated end-to-end tests.

To write and run tests with selendroid, first a customized selendroid-server for your aut must be created. To simplify this process I have created a Ruby gem:

		sudo gem install selendroid
		selendroid build pathToYour.apk
		
Now you will find your customized selendroid-server. To run the server:

		# start the selendroid server: 
		adb shell am instrument -e main_activity 'org.openqa.selendroid.testapp.HomeScreenActivity' org.openqa.selendroid.server/org.openqa.selendroid.server.ServerInstrumentation
		# activate port forwarding.
		adb forward tcp:8080 tcp:8080

A sample test looks like:

		driver = new AndroidDriver(new URL("http://localhost:8080/wd/hub"), getDefaultCapabilities());
		driver.findElement(By.id("startUserRegistration")).click();
		
		WebDriverWait wait = new WebDriverWait(driver, 5);
		WebElement inputUsername =
    	wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inputUsername")));
		inputUsername.sendKeys(user.getUsername());
		Assert.assertEquals(nameInput.getText(), "Mr. Burns");
		nameInput.clear();
		nameInput.sendKeys(user.getName());
		takeScreenShot("User data entered.");
		driver.findElement(By.id("btnRegisterUser")).click();

More details about selendroid can be found in the [wiki](https://github.com/DominikDary/selendroid/wiki/).

Current state
-------------

Please keep in mind that this project is in very early stages. Help is appreciated. Reach us out in the issues or via email.

You can track the current progress on the following [link](https://github.com/DominikDary/selendroid/wiki/JSON-Wire-Protocol)


