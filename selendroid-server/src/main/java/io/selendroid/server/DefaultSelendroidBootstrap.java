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

import android.content.Context;
import android.os.Looper;
import android.os.PowerManager;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.extension.ExtensionLoader;
import io.selendroid.server.util.SelendroidLogger;

public class DefaultSelendroidBootstrap extends SelendroidBootstrap {
    public static final int DEFAULT_SERVER_PORT = 8080;

    private int serverPort;
    private PowerManager.WakeLock wakeLock;
    private HttpdThread serverThread;

    public DefaultSelendroidBootstrap(ServerInstrumentation serverInstrumentation,
                                      InstrumentationArguments args,
                                      ExtensionLoader extensionLoader) {
        super(serverInstrumentation, args, extensionLoader);
    }

    private int parseServerPort(String port) {
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

    @Override
    public void onCreate() {
        serverPort = parseServerPort(args.getServerPort());
        callBeforeApplicationCreateBootstraps();
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

        serverThread = new HttpdThread(serverInstrumentation, serverPort);
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
}
