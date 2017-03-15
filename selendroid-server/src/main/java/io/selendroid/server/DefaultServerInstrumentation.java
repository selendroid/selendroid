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
import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.CallLog;
import android.view.View;
import io.selendroid.server.android.ActivitiesReporter;
import io.selendroid.server.android.AndroidWait;
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
import java.util.Map;
import java.util.Set;

public class DefaultServerInstrumentation implements ServerInstrumentation {
    public static final int DEFAULT_SERVER_PORT = 8080;
    private Instrumentation instrumentation;
    protected InstrumentationArguments args;
    private AndroidWait androidWait;
    private ActivitiesReporter activitiesReporter;
    protected int serverPort;
    private HttpdThread serverThread;
    protected PowerManager.WakeLock wakeLock;
    private ExtensionLoader extensionLoader;

    public DefaultServerInstrumentation(Instrumentation instrumentation,
                                        InstrumentationArguments args) {
        this.instrumentation = instrumentation;
        this.args = args;

        if (args.isLoadExtensions()) {
            extensionLoader = new ExtensionLoader(instrumentation.getTargetContext(),
                    ExternalStorage.getExtensionDex().getAbsolutePath());
        } else {
            extensionLoader = new ExtensionLoader(instrumentation.getTargetContext());
        }

        activitiesReporter = new ActivitiesReporter();
        androidWait = new AndroidWait();
    }

    @Override
    public void onCreate() {
        Handler mainThreadHandler = new Handler();
        serverPort = parseServerPort(args.getServerPort());

        callBeforeApplicationCreateBootstraps();

        // Queue bootstrapping and starting of the main activity on the main thread.
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                callAfterApplicationCreateBootstraps();
                if (args.getServiceClassName() != null) {
                    startService();
                } else {
                    startMainActivity();
                }
                try {
                    startServer();
                } catch (Exception e) {
                    SelendroidLogger.error("Failed to start Selendroid server", e);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        try {
            if (wakeLock != null) {
                wakeLock.release();
                wakeLock = null;
            }
        } catch (Exception e) {
            SelendroidLogger.error("Error shutting down: ", e);
        }
        stopServer();
    }

    @Override
    public void startService() {
        if (args.getServiceClassName() != null) {
            startService(args.getServiceClassName(), args.getIntentAction());
        }
    }

    @Override
    public void startService(String serviceClassName, String intentAction) {
        instrumentation.getTargetContext().startService(
                Intents.createStartServiceIntent(instrumentation.getTargetContext(), serviceClassName, intentAction));
    }

    @Override
    public void startMainActivity() {
        doFinishAllActivities();
        if (args.getActivityClassName() != null) {
            startActivity(args.getActivityClassName());
        } else {
            instrumentation
                    .getTargetContext()
                    .startActivity(Intents.createUriIntent(args.getIntentAction(), args.getIntentUri()));
        }
    }

    @Override
    public void startActivity(String activityClassName) {
        doFinishAllActivities();

        // Start the new activity
        Intent intent = Intents.createStartActivityIntent(instrumentation.getTargetContext(), activityClassName);
        instrumentation.getTargetContext().startActivity(intent);
    }

    @Override
    public void startServer() {
        if (serverThread != null && serverThread.isAlive()) {
            return;
        }

        if (serverThread != null) {
            SelendroidLogger.info("Stopping selendroid http server");
            stopServer();
        }

        serverThread = new HttpdThread(this, serverPort);
        serverThread.startServer();
    }

    @Override
    public void stopServer() {
        if (serverThread == null) {
            return;
        }
        if (!serverThread.isAlive()) {
            serverThread = null;
            return;
        }

        SelendroidLogger.info("Stopping selendroid http server");
        serverThread.stopLooping();
        serverThread.interrupt();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        serverThread = null;
    }

    @Override
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
    public ActivitiesReporter getActivitiesReporter() {
        return activitiesReporter;
    }

    @Override
    public Activity getCurrentActivity() {
        return activitiesReporter.getCurrentActivity();
    }

    @Override
    public void setImplicitWait(long millis) {
        androidWait.setTimeoutInMillis(millis);
    }

    @Override
    public void finishAllActivities() {
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                doFinishAllActivities();
            }
        });
    }

    @Override
    public AndroidWait getAndroidWait() {
        return androidWait;
    }

    @Override
    public Map<String, String> getExtraArgs() {
      return args.getExtraArgs();
    }

    @Override
    public String getExtraArg(String key) {
      return args.getExtraArg(key);
    }

    @Override
    public void backgroundActivity() {
        activitiesReporter.setBackgroundActivity(activitiesReporter.getCurrentActivity());
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        instrumentation.getTargetContext().startActivity(homeIntent);
    }

    @Override
    public void resumeActivity() {
        Activity activity = activitiesReporter.getBackgroundActivity();
        SelendroidLogger.info("got background activity");
        if (activity == null) {
            SelendroidLogger
                    .error("activity class is empty", new NullPointerException(
                            "Activity class to start is null."));
            return;
        }
        // start now the new activity
        SelendroidLogger.info("background activity is not null");
        Intent intent = new Intent(instrumentation.getTargetContext(), activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        SelendroidLogger.info("created intent and got target context");
        instrumentation.getTargetContext().startActivity(intent);
        SelendroidLogger.info("got target context and started activity");
        activitiesReporter.setBackgroundActivity(null);
    }

    @Override
    public void addCallLog(CallLogEntry log) {
        String permission = Manifest.permission.WRITE_CALL_LOG;
        if (instrumentation.getTargetContext().checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            ContentValues values = new ContentValues();
            values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
            values.put(CallLog.Calls.TYPE, log.getDirection());
            values.put(CallLog.Calls.DATE, log.getDate().getTime());
            values.put(CallLog.Calls.DURATION, log.getDuration());
            values.put(CallLog.Calls.NUMBER, log.getNumber());
            instrumentation.getTargetContext().getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
        } else {
            throw new PermissionDeniedException("Application Under Test does not have the required WRITE_CALL_LOGS permission for this feature..");
        }
    }

    @Override
    public List<CallLogEntry> readCallLog() {
        if (instrumentation.getTargetContext().checkCallingOrSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            List<CallLogEntry> logs = new ArrayList<CallLogEntry>();
            Cursor managedCursor = instrumentation.getTargetContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
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

    @Override
    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    @Override
    public ExtensionLoader getExtensionLoader() {
        return extensionLoader;
    }

    @Override
    public String getServerVersion() {
        Context context = instrumentation.getContext();
        String versionName = "0.3";
        try {
            versionName =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return versionName;
    }

    @Override
    public String getCpuArch() {
        return android.os.Build.CPU_ABI;
    }

    @Override
    public String getOsName() {
        return "Android";
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

    private void doFinishAllActivities() {
        Set<Activity> activities = activitiesReporter.getActivities();
        if (activities != null && !activities.isEmpty()) {
            for (Activity activity : activities) {
                activity.finish();
            }
        }
    }

    public void callBeforeApplicationCreateBootstraps() {
        if (!args.isLoadExtensions() || args.getBootstrapClassNames() == null) {
            return;
        }
        extensionLoader.runBeforeApplicationCreateBootstrap(instrumentation, args.getBootstrapClassNames().split(","));
    }

    public void callAfterApplicationCreateBootstraps() {
        if (!args.isLoadExtensions() || args.getBootstrapClassNames() == null) {
            return;
        }
        extensionLoader.runAfterApplicationCreateBootstrap(instrumentation, args.getBootstrapClassNames().split(","));
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
                PowerManager pm = (PowerManager) instrumentation.getInstrumentation().getContext().getSystemService(Context.POWER_SERVICE);
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

        public void stopLooping() {
            if (looper == null) {
                return;
            }
            looper.quit();
        }
    }

    protected int parseServerPort(String port) {
        int parsedServerPort;
        try {
                parsedServerPort = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                SelendroidLogger.info("Failed to parse server port, defaulting to 8080");
                parsedServerPort = DEFAULT_SERVER_PORT;
            }
                if (!isValidPort(parsedServerPort)) {
            SelendroidLogger.info("Invalid port " + parsedServerPort + ", defaulting to " + DEFAULT_SERVER_PORT);
        }
        return parsedServerPort;
    }

    private boolean isValidPort(int port) {
        return port >= 1024 && port <= 65535;
    }
}
