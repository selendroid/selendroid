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

import android.app.Instrumentation;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.util.SelendroidLogger;

public class JUnitRunnerServerInstrumentation extends DefaultServerInstrumentation {
    private AndroidServer server;

    public JUnitRunnerServerInstrumentation(Instrumentation instrumentation, InstrumentationArguments args) {
        super(instrumentation, args);
    }

    @Override
    public void onCreate() {
        serverPort = parseServerPort(args.getServerPort());
        callBeforeApplicationCreateBootstraps();
    }

    @Override
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
            PowerManager pm = (PowerManager) getInstrumentation().getContext().getSystemService(Context.POWER_SERVICE);
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

    @Override
    public void stopServer() {
        if (server == null) {
            return;
        }
        server.stop();
    }
}
