/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
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
package io.selendroid.server;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.CallLog;
import android.util.Log;
import android.support.test.runner.AndroidJUnitRunner;
import android.view.View;
import io.selendroid.server.android.ActivitiesReporter;
import io.selendroid.server.android.AndroidWait;
import io.selendroid.server.common.ServerDetails;
import io.selendroid.server.common.exceptions.PermissionDeniedException;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.common.utils.CallLogEntry;
import io.selendroid.server.extension.ExtensionLoader;
import io.selendroid.server.model.ExternalStorage;
import io.selendroid.server.util.Intents;
import io.selendroid.server.util.SelendroidLogger;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ServerInstrumentation extends AndroidJUnitRunner implements ServerDetails {
  private ActivitiesReporter activitiesReporter = new ActivitiesReporter();
  private static ServerInstrumentation instance = null;
  public static String mainActivityName = null;
  public static String intentUri = null;
  private AndroidWait androidWait = new AndroidWait();
  private AndroidServer server;
  private PowerManager.WakeLock wakeLock;
  private int serverPort = 8080;

  /**
   * Arguments this instrumentation was started with.
   */
  public InstrumentationArguments args = null;
  private ExtensionLoader extensionLoader;

  public void startMainActivity() {
    doFinishAllActivities();
    if (mainActivityName != null) {
      startActivity(mainActivityName);
    } else if (args.getIntentUri() != null) {
      getTargetContext().startActivity(Intents.createUriIntent(intentUri));
    }
  }

  public void startActivity(String activityClassName) {
    doFinishAllActivities();

    Context context = getTargetContext();
    // Start the new activity
    Intent intent = Intents.createStartActivityIntent(context, activityClassName);
    context.startActivity(intent);
  }

  private void doFinishAllActivities() {
    Set<Activity> activities = activitiesReporter.getActivities();
    if (activities != null && !activities.isEmpty()) {
      for (Activity activity : activities) {
        activity.finish();
      }
    }
  }

  /**
   * Finishes all activities on the main thread.
   */
  public void finishAllActivities() {
    runOnMainSync(new Runnable() {
      @Override
      public void run() {
        ServerInstrumentation.this.doFinishAllActivities();
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle arguments) {
    this.args = new InstrumentationArguments(arguments);

    String destination = null;
    if (args.getActivityClassName()!= null) {
      destination = "main activity: " + args.getActivityClassName();
    } else if (args.getIntentUri() != null) {
      destination = "URI: " + args.getIntentUri();
    }

    if (destination != null) {
      SelendroidLogger.info("Instrumentation initialized with " + destination);
    } else {
      SelendroidLogger.error("Instrumentation initialized without destination");
    }

    parseServerPort(args.getServerPort());
    instance = this;
    callBeforeApplicationCreateBootstraps();

    super.onCreate(arguments);
  }

  private boolean isValidPort(int port) {
    return port >= 1024 && port <= 65535;
  }

  public static synchronized ServerInstrumentation getInstance() {
    return instance;
  }

  @Override
  public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException,
      IllegalAccessException, ClassNotFoundException {
    UncaughtExceptionHandling.clearCrashLogFile();
    UncaughtExceptionHandling.setGlobalExceptionHandler();
    return super.newApplication(cl, className, context);
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
      SelendroidLogger.error("Error searching for root view: ", e);
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
      if (server != null) {
        server.stop();
        server = null;
      }
    } catch (Exception e) {
      SelendroidLogger.error("Error shutting down: ", e);
    }
    instance = null;
  }


  public void startServer() {
    Handler handler = new Handler();
    handler.post(new Runnable() {
      @Override
      public void run() {
        UncaughtExceptionHandling.clearCrashLogFile();
        UncaughtExceptionHandling.setGlobalExceptionHandler();

        callAfterApplicationCreateBootstraps();

        startMainActivity();
        try {
          doStartServer();
          SelendroidLogger.info("Started Selendroid HTTP server on port " + server.getPort());
        } catch (Exception e) {
          SelendroidLogger.error("Failed to start Selendroid server", e);
        }
      }
    });
  }

  private void doStartServer() {
    if (server != null) {
      server.stop();
    }

    try {
      if (server == null) {
        server = new AndroidServer(this, serverPort);
      }
      // Get a wake lock to stop the cpu going to sleep
      PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
      wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Selendroid");
      try {
        wakeLock.acquire();
      } catch (SecurityException e) {
      }

      server.start();

      SelendroidLogger.info("Started selendroid http server on port " + server.getPort());
    } catch (Exception e) {
      SelendroidLogger.error("Error starting httpd.", e);
      throw new SelendroidException("Httpd failed to start!");
    }
  }

  public AndroidWait getAndroidWait() {
    return androidWait;
  }

  public void setImplicitWait(long millies) {
    androidWait.setTimeoutInMillis(millies);
  }

  @Override
  public String getServerVersion() {
    Context context = getContext();
    String versionName = "0.3";
    try {
      versionName =
          context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
    }
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

  public ExtensionLoader getExtensionLoader() {
    return extensionLoader;
  }

  public void backgroundActivity() {
    activitiesReporter.setBackgroundActivity(activitiesReporter.getCurrentActivity());
    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
    homeIntent.addCategory(Intent.CATEGORY_HOME);
    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getTargetContext().startActivity(homeIntent);
  }

  public void resumeActivity() {
    Activity activity = activitiesReporter.getBackgroundActivity();
    Log.d("TAG", "got background activity");
    if (activity == null) {
      SelendroidLogger
          .error("activity class is empty", new NullPointerException(
              "Activity class to start is null."));
      return;
    }
    // start now the new activity
    Log.d("TAG", "background activity is not null");
    Intent intent = new Intent(getTargetContext(), activity.getClass());
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
        | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    Log.d("TAG", "created intent and got target context");
    getTargetContext().startActivity(intent);
    Log.d("TAG", "got target context and started activity");
    activitiesReporter.setBackgroundActivity(null);
  }

  public void addCallLog(CallLogEntry log) throws PermissionDeniedException {
    String permission = Manifest.permission.WRITE_CALL_LOG;
    if (getTargetContext().checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
      ContentValues values = new ContentValues();
      values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
      values.put(CallLog.Calls.TYPE, log.getDirection());
      values.put(CallLog.Calls.DATE, log.getDate().getTime());
      values.put(CallLog.Calls.DURATION, log.getDuration());
      values.put(CallLog.Calls.NUMBER, log.getNumber());
      getTargetContext().getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
    } else {
      throw new PermissionDeniedException("Application Under Test does not have the required WRITE_CALL_LOGS permission for this feature..");
    }
  }

  public List<CallLogEntry> readCallLog() throws PermissionDeniedException {
    if (getTargetContext().checkCallingOrSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
      List<CallLogEntry> logs = new ArrayList<CallLogEntry>();
      Cursor managedCursor = getTargetContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
      int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
      int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
      int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
      int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
      while (managedCursor.moveToNext()) {
        String phNumber = managedCursor.getString(number);
        String callType = managedCursor.getString(type);
        String callDate = managedCursor.getString(date);
        Date callDayTime = new Date(Long.valueOf(callDate));
        String callDuration = managedCursor.getString(duration);
        logs.add(new CallLogEntry(phNumber, Integer.parseInt(callDuration), callDayTime, Integer.parseInt(callType)));
      }
      managedCursor.close();
      return logs;
    } else {
      throw new PermissionDeniedException("Application under test does not have required READ_CALL_LOG permission for this feature.");
    }

  }

  private void parseServerPort(String port) {
    int parsedServerPort;
    try {
      parsedServerPort = Integer.parseInt(port);
    } catch (NumberFormatException e) {
      SelendroidLogger.info("Failed to parse server port, defaulting to 8080");
      parsedServerPort = serverPort;
    }

    if (isValidPort(parsedServerPort)) {
      serverPort = parsedServerPort;
    } else {
      SelendroidLogger.info("Invalid port " + parsedServerPort + ", defaulting to 8080");
    }
  }

  private void callBeforeApplicationCreateBootstraps() {
    final Context context = getTargetContext();
    if (args.isLoadExtensions()) {
      extensionLoader = new ExtensionLoader(context, ExternalStorage.getExtensionDex().getAbsolutePath());
      if (args.getBootstrapClassNames() != null) {
        extensionLoader.runBeforeApplicationCreateBootstrap(this, args.getBootstrapClassNames().split(","));
      }
    } else {
      extensionLoader = new ExtensionLoader(context);
    }
  }

  private void callAfterApplicationCreateBootstraps() {
    if (args.isLoadExtensions() && args.getBootstrapClassNames() != null) {
      extensionLoader.runAfterApplicationCreateBootstrap(this, args.getBootstrapClassNames().split(","));
    }
  }
}
