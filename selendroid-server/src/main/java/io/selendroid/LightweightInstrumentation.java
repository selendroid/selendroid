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

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import io.selendroid.extension.ExtensionLoader;
import io.selendroid.server.model.ExternalStorage;
import io.selendroid.util.SelendroidLogger;

/**
 * A light weight instrumentation without server
 */
public class LightweightInstrumentation extends Instrumentation {
    /**
     * Arguments this instrumentation was started with.
     */
    public InstrumentationArguments args = null;
    private ExtensionLoader extensionLoader;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle arguments) {
        SelendroidLogger.info("Light weight Instrumentation initialized.");
        final String mainActivityName = arguments.getString("main_activity");
        final LightweightInstrumentation instance = this;

        Handler handler = new Handler();
        this.args = new InstrumentationArguments(arguments);
        final Context context = getTargetContext();

        if (args.isLoadExtensions()) {
            extensionLoader = new ExtensionLoader(context, ExternalStorage.getExtensionDex().getAbsolutePath());
            String bootstrapClassNames = args.getBootstrapClassNames();
            if (bootstrapClassNames != null) {
                extensionLoader.runBeforeApplicationCreateBootstrap(this, bootstrapClassNames.split(","));
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

                // Start the new activity
                Intent intent = new Intent();
                intent.setClassName(getTargetContext(), mainActivityName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                getTargetContext().startActivity(intent);
            }
        });
    }
}
