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
import io.selendroid.util.Intents;
import io.selendroid.util.SelendroidLogger;

/**
 * A light weight instrumentation without server
 */
public class LightweightInstrumentation extends Instrumentation {
    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle arguments) {
        SelendroidLogger.info("Light weight Instrumentation initialized.");
        final String mainActivityName = arguments.getString("main_activity");
        final Context context = getTargetContext();
        InstrumentationArguments args = new InstrumentationArguments(arguments);
        Handler handler = new Handler();

        if (args.isLoadExtensions()) {
            ExtensionLoader extensionLoader = new ExtensionLoader(context, ExternalStorage.getExtensionDex().getAbsolutePath());
            String bootstrapClassNames = args.getBootstrapClassNames();
            if (bootstrapClassNames != null) {
                extensionLoader.runBeforeApplicationCreateBootstrap(this, bootstrapClassNames.split(","));
            }
        }

        // Queue bootstrapping and starting of the main activity on the main thread.
        handler.post(new Runnable() {
            @Override
            public void run() {
                UncaughtExceptionHandling.clearCrashLogFile();
                UncaughtExceptionHandling.setGlobalExceptionHandler();

                // Start the new activity
                Intent intent = Intents.CreateIntent(context, mainActivityName);
                context.startActivity(intent);
            }
        });
    }
}
