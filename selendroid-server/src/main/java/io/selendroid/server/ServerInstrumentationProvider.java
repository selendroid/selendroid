package io.selendroid.server;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import io.selendroid.server.common.exceptions.SelendroidException;

public class ServerInstrumentationProvider {
    public static ServerInstrumentation getServerInstrumentationInstance() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        if (!(instrumentation instanceof ServerInstrumentation)) {
            throw new SelendroidException("Instrumentation is not an instance of ServerInstrumentation");
        }
        return (ServerInstrumentation) instrumentation;
    }
}
