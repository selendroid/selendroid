/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.selendroid;

import io.selendroid.android.ActivitiesReporter;
import io.selendroid.android.AndroidWait;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.AndroidServer;
import io.selendroid.server.ServerDetails;
import io.selendroid.util.SelendroidLogger;

import java.util.Set;

import org.json.JSONArray;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.view.View;

public class ServerInstrumentation extends Instrumentation implements ServerDetails {
  private ActivitiesReporter activitiesReporter = new ActivitiesReporter();
  private static ServerInstrumentation instance = null;
  public static Class<? extends Activity> mainActivity = null;
  private HttpdThread serverThread = null;
  private AndroidWait androidWait = new AndroidWait();
  private PowerManager.WakeLock wakeLock;
  private int serverPort = 8080;

  public void startMainActivity() {
    finishAllActivities();
    startActivity(mainActivity);
  }

  public void startActivity(Class activity) {
    if (activity == null) {
      SelendroidLogger.log("activity class is empty", new NullPointerException(
          "Activity class to start is null."));
      return;
    }

    // start now the new activity
    Intent intent = new Intent(getTargetContext(), activity);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.setAction(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    Activity a = startActivitySync(intent);
  }

  public void finishAllActivities() {
    runOnMainSync(new Runnable() {
      @Override
      public void run() {
        Set<Activity> activities = getActivities();
        if (activities != null && !activities.isEmpty()) {
          for (Activity activity : activities) {
            activity.finish();
          }
        }
      }
    });

  }

  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle arguments) {

    String activityClazzName = arguments.getString("main_activity");

    int parsedServerPort = 0;

    try {
      String port = arguments.getString("server_port");
      if (port != null && port.isEmpty() == false) {
        parsedServerPort = Integer.parseInt(port);
      }
    } catch (NumberFormatException ex) {
      SelendroidLogger.log("Unable to parse the value of server_port key.");
      parsedServerPort = this.serverPort;
    }

    if (isValidPort(parsedServerPort)) {
      this.serverPort = parsedServerPort;
    }

    Class<? extends Activity> clazz = null;
    try {
      clazz = (Class<? extends Activity>) Class.forName(activityClazzName);
    } catch (ClassNotFoundException exception) {
      SelendroidLogger.log("The class with name '" + activityClazzName + "' does not exist.",
          exception);
    }
    mainActivity = clazz;
    SelendroidLogger.log("Instrumentation initialized with main activity: " + activityClazzName);
    if (clazz == null) {
      SelendroidLogger.log("Clazz is null - but should be an instance of: " + activityClazzName);
    }
    instance = this;

    start();
  }

  private boolean isValidPort(int port) {
    return port >= 1024 && port <= 65535;
  }

  @Override
  public void onStart() {
    synchronized (ServerInstrumentation.class) {
      try {
        startServer();
      } catch (Exception e) {
        SelendroidLogger.log("Exception when starting selendroid.", e);
      }
    }
    // make sure this is always displayed
    System.out.println("Selendroid started on port " + serverThread.getServer().getPort());
  }

  public static synchronized ServerInstrumentation getInstance() {
    return instance;
  }

  public void runOnUiThread(Runnable runner) {
    runOnMainSync(runner);
  }

  @Override
  public void callActivityOnResume(Activity activity) {
    super.callActivityOnResume(activity);

    activitiesReporter.wasResumed(activity);
  }

  @Override
  public void callActivityOnCreate(Activity activity, Bundle icicle) {
    super.callActivityOnCreate(activity, icicle);

    activitiesReporter.wasCreated(activity);
  }

  @Override
  public void callActivityOnDestroy(Activity activity) {
    activitiesReporter.wasDestroyed(activity);

    super.callActivityOnDestroy(activity);
  }


  public Activity getCurrentActivity() {
    return activitiesReporter.getCurrentActivity();
  }

  private Set<Activity> getActivities() {
    return activitiesReporter.getActivities();
  }

  public View getRootView() {
    try {
      View decorView = getCurrentActivity().getWindow().getDecorView();
      if (decorView != null) {
        View rootView = null;// decorView.findViewById(android.R.id.content);
        if (rootView != null) {
          return rootView;
        }
      }
      return decorView;
    } catch (Exception e) {
      SelendroidLogger.log("Error occured while searching for root view: ", e);
    }

    throw new SelendroidException("Could not find any views");
  }

  @Override
  public void onDestroy() {
    try {
      if (wakeLock != null) {
        wakeLock.release();
        wakeLock = null;
      }
      stopServer();
    } catch (Exception e) {
      SelendroidLogger.log("Error occured while shutting down: ", e);
    }
    instance = null;
  }


  public void startServer() {
    if (serverThread != null && serverThread.isAlive()) {
      return;
    }

    if (serverThread != null) {
      SelendroidLogger.log("Stopping selendroid http server");
      stopServer();
    }

    serverThread = new HttpdThread(this, this.serverPort);
    serverThread.start();
  }

  protected void stopServer() {
    if (serverThread == null) {
      return;
    }
    if (!serverThread.isAlive()) {
      serverThread = null;
      return;
    }

    SelendroidLogger.log("Stopping selendroid http server");
    serverThread.stopLooping();
    serverThread.interrupt();
    try {
      serverThread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    serverThread = null;
  }


  public AndroidWait getAndroidWait() {
    return androidWait;
  }

  public void setImplicitWait(long millies) {
    androidWait.setTimeoutInMillis(millies);
    serverThread.getServer().setConnectionTimeout(millies);
  }

  @Override
  public String getServerVersion() {
    Context context = getContext();
    String versionName = "0.3";
    try {
      versionName =
          context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {}
    return versionName;
  }

  @Override
  public String getCpuArch() {
    return android.os.Build.CPU_ABI;
  }

  @Override
  public String getOsVersion() {
    return String.valueOf(android.os.Build.VERSION.SDK_INT);
  }

  private class HttpdThread extends Thread {

    private final AndroidServer server;
    private ServerInstrumentation instrumentation = null;
    private Looper looper;

    public HttpdThread(ServerInstrumentation instrumentation, int serverPort) {
      this.instrumentation = instrumentation;
      // Create the server but absolutely do not start it here
      server = new AndroidServer(this.instrumentation, serverPort);
    }

    @Override
    public void run() {
      Looper.prepare();
      looper = Looper.myLooper();
      startServer();
      Looper.loop();
    }

    public AndroidServer getServer() {
      return server;
    }

    private void startServer() {
      try {
        // Get a wake lock to stop the cpu going to sleep
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Selendroid");
        try {
          wakeLock.acquire();
        } catch (SecurityException e) {}

        server.start();

        SelendroidLogger.log("Started selendroid http server on port " + server.getPort());
      } catch (Exception e) {
        SelendroidLogger.log("Error starting httpd.", e);

        throw new SelendroidException("Httpd failed to start!");
      }
    }

    public void stopLooping() {
      if (looper == null) {
        return;
      }
      looper.quit();
    }
  }

  @Override
  public JSONArray getSupportedApps() {
    return new JSONArray();
  }

  @Override
  public JSONArray getSupportedDevices() {
    return new JSONArray();
  }

  @Override
  public String getOsName() {
    return "Android";
  }
}
