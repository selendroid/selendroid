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
package io.selendroid;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import io.selendroid.android.ActivitiesReporter;
import io.selendroid.extension.ExtensionLoader;
import io.selendroid.server.ServerDetails;
import io.selendroid.server.model.ExternalStorage;
import io.selendroid.util.SelendroidLogger;
import org.json.JSONArray;

import java.util.Set;

/**
 * A light weight instrumentation without server
 */
public class LightweightInstrumentation extends Instrumentation implements ServerDetails {
    private ActivitiesReporter activitiesReporter = new ActivitiesReporter();
    private static LightweightInstrumentation instance = null;
    public static String mainActivityName = null;

    /**
     * Arguments this instrumentation was started with.
     */
    public InstrumentationArguments args = null;
    private ExtensionLoader extensionLoader;

    public void startMainActivity() {
        finishAllActivities();
        startActivity(mainActivityName);
    }

    public void startActivity(String activityClassName) {
        finishAllActivities();

        // Start the new activity
        Intent intent = new Intent();
        intent.setClassName(getTargetContext(), activityClassName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        getTargetContext().startActivity(intent);
    }

    public void finishAllActivities() {
        Set<Activity> activities = getActivities();
        if (activities != null && !activities.isEmpty()) {
            for (Activity activity : activities) {
                activity.finish();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle arguments) {
        Handler handler = new Handler();
        this.args = new InstrumentationArguments(arguments);

        mainActivityName = arguments.getString("main_activity");

        SelendroidLogger.info("Instrumentation initialized with main activity: " + mainActivityName);
        instance = this;

        final Context context = getTargetContext();
        if (args.isLoadExtensions()) {
            extensionLoader = new ExtensionLoader(context, ExternalStorage.getExtensionDex().getAbsolutePath());
            String bootstrapClassNames = args.getBootstrapClassNames();
            if (bootstrapClassNames != null) {
                extensionLoader.runBeforeApplicationCreateBootstrap(instance, bootstrapClassNames.split(","));
            }
        } else {
            extensionLoader = new ExtensionLoader(context);
        }

        // Queue bootstrapping and starting of the main activity on the main thread.
        handler.post(new Runnable() {
            @Override
            public void run() {
                UncaughtExceptionHandling.clearCrashLogFile();
                UncaughtExceptionHandling.setGlobalExceptionHandler();

                if (args.isLoadExtensions() && args.getBootstrapClassNames() != null) {
                    extensionLoader.runAfterApplicationCreateBootstrap(instance, args.getBootstrapClassNames().split(","));
                }

                startMainActivity();
            }
        });
    }

    public static synchronized LightweightInstrumentation getInstance() {
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

    private Set<Activity> getActivities() {
        return activitiesReporter.getActivities();
    }

    @Override
    public void onDestroy() {
        instance = null;
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
}
