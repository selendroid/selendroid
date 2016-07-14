package io.selendroid.server;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class SelendroidAccesibilityService extends AccessibilityService {
    private static SelendroidAccesibilityService instance;
    private Listener listener;

    public interface Listener {
        void onAccessibilityEvent(AccessibilityEvent event);
    }

    public static synchronized SelendroidAccesibilityService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (listener != null) {
            listener.onAccessibilityEvent(event);
        }
    }

    @Override
    public void onInterrupt() {

    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
