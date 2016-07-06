package io.selendroid.server;

import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;
import io.selendroid.server.ServerInstrumentation;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ServerManager {
    @Test
    public void startServer() {
        Looper looper = null;
        try {
            Looper.prepare();
            looper = Looper.myLooper();
            ServerInstrumentation.getInstance().startServer();
            Looper.loop();
        } catch (Exception e) {
            if (looper != null) {
                looper.quit();
                looper = null;
            }
        }

        if (looper != null) {
            looper.quit();
        }
    }
}
