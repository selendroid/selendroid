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
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.util.SelendroidLogger;

public class JUnitRunnerServerInstrumentation extends DefaultServerInstrumentation {
    private AndroidServer server;

    public JUnitRunnerServerInstrumentation(Instrumentation instrumentation, InstrumentationArguments args) {
        super(instrumentation, args);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void startServer() {
        super.startServer();
    }

    @Override
    protected void startServerImpl() {
        SelendroidLogger.info("*** ServerInstrumentation#startServerImpl() ***");
        if (server != null) {
            server.stop();
        }

        try {
            if (server == null) {
                server = new AndroidServer(this, serverPort);
            }

            DefaultServerInstrumentation.startAndroidServer(
              server,
              wakeLock);
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
