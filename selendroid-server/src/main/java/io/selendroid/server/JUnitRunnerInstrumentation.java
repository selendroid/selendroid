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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;

public class JUnitRunnerInstrumentation extends AndroidJUnitRunner implements DelegatesToServerInstrumentation {
    private ServerInstrumentation delegateInstrumentation;

    @Override
    public void onCreate(Bundle arguments) {
        delegateInstrumentation = new JUnitRunnerServerInstrumentation(this, new InstrumentationArguments(arguments));
        delegateInstrumentation.onCreate();
        super.onCreate(arguments);
    }

    public void startServer() {
        delegateInstrumentation.startServer();
    }

    public void stopServer() {
        delegateInstrumentation.stopServer();
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        UncaughtExceptionHandling.clearCrashLogFile();
        UncaughtExceptionHandling.setGlobalExceptionHandler();
        return super.newApplication(cl, className, context);
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        super.callActivityOnResume(activity);
        delegateInstrumentation.getActivitiesReporter().wasResumed(activity);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        super.callActivityOnCreate(activity, icicle);
        delegateInstrumentation.getActivitiesReporter().wasCreated(activity);
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        delegateInstrumentation.getActivitiesReporter().wasDestroyed(activity);
        super.callActivityOnDestroy(activity);
    }

    @Override
    public void onDestroy() {
        delegateInstrumentation.onDestroy();
    }

    @Override
    public ServerInstrumentation getServerInstrumentation() {
        return delegateInstrumentation;
    }
}
