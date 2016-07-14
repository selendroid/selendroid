package io.selendroid.server;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;

public class SelendroidInstrumentation extends Instrumentation implements DelegatesToServerInstrumentation {
    private ServerInstrumentation delegateInstrumentation;

    @Override
    public void onCreate(Bundle arguments) {
        delegateInstrumentation = new DefaultServerInstrumentation(this, new InstrumentationArguments(arguments));
        delegateInstrumentation.onCreate();
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
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
