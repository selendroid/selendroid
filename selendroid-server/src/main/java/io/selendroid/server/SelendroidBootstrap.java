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

import io.selendroid.server.extension.ExtensionLoader;

public abstract class SelendroidBootstrap {
    protected ServerInstrumentation serverInstrumentation;
    protected InstrumentationArguments args;
    protected ExtensionLoader extensionLoader;

    public SelendroidBootstrap(ServerInstrumentation serverInstrumentation,
                               InstrumentationArguments args,
                               ExtensionLoader extensionLoader) {
        this.serverInstrumentation = serverInstrumentation;
        this.args = args;
        this.extensionLoader = extensionLoader;
    }

    public abstract void onCreate();

    public abstract void onDestroy();

    public abstract void startServer();
    public abstract void stopServer();

    public void callBeforeApplicationCreateBootstraps() {
        if (!args.isLoadExtensions() || args.getBootstrapClassNames() != null) {
            return;
        }
        extensionLoader.runBeforeApplicationCreateBootstrap(serverInstrumentation.getInstrumentation(), args.getBootstrapClassNames().split(","));
    }

    public void callAfterApplicationCreateBootstraps() {
        if (!args.isLoadExtensions() || args.getBootstrapClassNames() != null) {
            return;
        }
        extensionLoader.runAfterApplicationCreateBootstrap(serverInstrumentation.getInstrumentation(), args.getBootstrapClassNames().split(","));
    }
}
