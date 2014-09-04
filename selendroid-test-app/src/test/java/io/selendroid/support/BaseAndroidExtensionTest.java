package io.selendroid.support;

import io.selendroid.android.AndroidSdk;
import io.selendroid.io.ShellCommand;
import io.selendroid.server.util.HttpClientUtil;
import org.apache.commons.exec.CommandLine;
import org.junit.BeforeClass;

import java.io.File;

public class BaseAndroidExtensionTest extends AbstractAndroidTest{
  @BeforeClass
  public static void startSelendroidServer() throws Exception {
    CommandLine externalStorageLocator = new CommandLine(AndroidSdk.adb());
    externalStorageLocator.addArgument("shell");
    externalStorageLocator.addArgument("echo");
    externalStorageLocator.addArgument("$EXTERNAL_STORAGE");
    String externalStorageDir = ShellCommand.exec(externalStorageLocator).trim();

    CommandLine pushExtensions = new CommandLine(AndroidSdk.adb());
    pushExtensions.addArgument("push");
    pushExtensions.addArgument("src/test/resources/extension.dex");
    pushExtensions.addArgument(new File(externalStorageDir, "extension.dex").getAbsolutePath());
    ShellCommand.exec(pushExtensions);

    CommandLine startSelendroid = new CommandLine(AndroidSdk.adb());
    startSelendroid.addArgument("shell");
    startSelendroid.addArgument("am");
    startSelendroid.addArgument("instrument");
    startSelendroid.addArgument("-e");
    startSelendroid.addArgument("main_activity");
    startSelendroid.addArgument("io.selendroid.testapp.HomeScreenActivity");
    startSelendroid.addArgument("-e");
    startSelendroid.addArgument("load_extensions");
    startSelendroid.addArgument("true");
    startSelendroid.addArgument("io.selendroid/.ServerInstrumentation");
    ShellCommand.exec(startSelendroid);

    CommandLine forwardPort = new CommandLine(AndroidSdk.adb());
    forwardPort.addArgument("forward");
    forwardPort.addArgument("tcp:8080");
    forwardPort.addArgument("tcp:8080");

    ShellCommand.exec(forwardPort);
    // instrumentation needs a beat to come up before connecting right away
    // without this the first test often will fail, there's a similar wait
    // in the selendroid-standalone
    HttpClientUtil.waitForServer(8080);
  }
}
