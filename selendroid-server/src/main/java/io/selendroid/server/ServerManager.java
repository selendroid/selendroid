package io.selendroid.server;

import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;
import io.selendroid.server.util.SelendroidLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ServerManager {
    private ServerInstrumentation instrumentation;
    private Looper looper = null;

    @Before
    public void setup() {
        SelendroidLogger.info("ServiceManager setup");
        Looper.prepare();
        looper = Looper.myLooper();

        instrumentation = ServerInstrumentation.getInstance();
        if (instrumentation.isWithAccessibilityService()) {
            SelendroidLogger.info("ServiceManager a11y setup");
            instrumentation.doAccessibilityServiceSetup();
        }
    }

    @Test
    public void startServer() {
        try {
            instrumentation.startServer();
            Looper.loop();
        } catch (Exception e) {
            if (looper != null) {
                looper.quit();
                looper = null;
            }
        }
    }

    @After
    public void tearDown() {
        SelendroidLogger.info("ServiceManager tearDown");
        if (looper != null) {
            looper.quit();
            looper = null;
        }

        instrumentation.stopServer();

        if (instrumentation.isWithAccessibilityService()) {
            SelendroidLogger.info("ServiceManager a11y tearDown");
            instrumentation.doAccessibilityTearDown();
        }
    }
}
