package io.selendroid.server.util;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import io.selendroid.server.SelendroidAccesibilityService;
import io.selendroid.server.common.exceptions.SelendroidException;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains a series of utility methods to enable and sync with {@link SelendroidAccesibilityService}.
 * All the sync methods need to be called from a non-UI thread
 */
public class AccessibilityServiceManager implements SelendroidAccesibilityService.Listener {
    private static final String SELENDROID_ACCESSIBILITY_SERVICE_NAME =
            SelendroidAccesibilityService.class.getName();
    private static final String PERMISSION_WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
    private static final String PERMISSION_WRITE_SECURE_SETTINGS =
            "android.permission.WRITE_SECURE_SETTINGS";

    private static final String TOUCH_EXPLORATION_GRANTED_ACCESSIBILITY_SERVICES =
            "touch_exploration_granted_accessibility_services";

    // Values used to sync with the AccessibilityEvent queue
    private static final long SYNC_EVENT_QUEUE_TIMEOUT_MS = 2000;
    private static final long SYNC_EVENT_QUEUE_RETRY_INTERVAL_MS = 200;
    private static final int SYNC_QUEUE_EVENT_TYPE =
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
    private static final int SYNC_VIEW_EVENT_TYPE = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
    private static final Bundle SYNC_PARCELABLE = new Bundle();
    private static final String PARCELABLE_SYNC_KEY = "sync_value";
    private static final double PARCELABLE_SYNC_VALUE = Math.random() + 1.0;
    static {
        SYNC_PARCELABLE.putDouble(PARCELABLE_SYNC_KEY, PARCELABLE_SYNC_VALUE);
    }
    private final AccessibilityEventMatcher mSyncEventMatcher = new AccessibilityEventMatcher() {
        @Override
        public boolean matches(AccessibilityEvent event) {
            return isQueueSyncEvent(event);
        }
    };

    private static final int MAX_ATTEMPTS_GET_ROOT = 10;

    // Values used to wait for the accessibility event queue to be idle
    private static final long ACCESSIBILITY_IDLE_TIMEOUT_MS = 3000;
    private static final long ACCESSIBILITY_IDLE_INTERVAL_MS = 500;
    private static final long ACCESSIBILITY_IDLE_RETRY_INTERVAL_MS = 100;

    // Values used to sync obtaining and instance of SelendroidAcessiblityService
    private static final long SYNC_SERVICE_TIMEOUT_MS = 5000;
    private static final long SYNC_SERVICE_RETRY_INTERVAL_MS = 100;

    // Values used to sync with the state of the accessibility setting (enabled/disabled)
    private static final long ACCESSIBILITY_STATE_TIMEOUT_MS = 5000;
    private static final long ACCESSIBILITY_STATE_CHANGE_RETRY_INTERVAL_MS = 200;

    private Context mContext;
    private AccessibilityManager mAccessibilityManager;

    private long mLastTimeEventReceived;

    private final Object mAccessibilityStateLock = new Object();
    private final Object mAccessibilityEventLock = new Object();
    private final List<AccessibilityEvent> mReceivedEventsBuffer = new ArrayList<>();

    private boolean mRecordingEvents;

    private AccessibilityManager.AccessibilityStateChangeListener mAccessibilityStateChangeListener =
            new AccessibilityManager.AccessibilityStateChangeListener() {
                @Override
                public void onAccessibilityStateChanged(boolean enabled) {
                    synchronized (mAccessibilityStateLock) {
                        mAccessibilityStateLock.notifyAll();
                    }
                }
            };

    public interface AccessibilityEventMatcher {
        boolean matches(AccessibilityEvent event);
    }

    public enum AccessibilityState {
        ENABLED("enabled"),
        DISABLED("disabled");

        String description;
        AccessibilityState(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public AccessibilityServiceManager(Context context) {
        mContext = context;
        mAccessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    public void doAccessibiliyServiceSetup() {
        checkPermissions();

        // Try to enable service
        if (getService() == null) {
            disableAccessibilityServices();
            enableAccessibilityService();
            if (getService() == null) {
                throw new SelendroidException("Failed to obtain accessibility service");
            }
        }

        connectToService();
    }

    public void doAccessibilityServiceTearDown() {
        disconnectFromService();
        disableAccessibilityServices();
    }

    public AccessibilityNodeInfo safeGetRootInActiveWindow() {
        if (getService() == null) {
            return null;
        }

        int attempts = 0;
        AccessibilityNodeInfo root = getService().getRootInActiveWindow();
        while ((root == null || root.getChildCount() == 0) && attempts < MAX_ATTEMPTS_GET_ROOT) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            attempts++;
            root = getService().getRootInActiveWindow();
        }

        return root;
    }

    public int nodeCount(AccessibilityNodeInfo root) {
        if (root == null) {
            return 0;
        }
        int result = 1;
        for (int i = 0; i < root.getChildCount(); i++) {
            result += nodeCount(root.getChild(i));
        }
        return result;
    }

    /**
     * Try to obtain an instance of {@link SelendroidAccesibilityService}
     *
     * @return The instance of {@link SelendroidAccesibilityService} or null if it's not available
     */
    public SelendroidAccesibilityService getService() {
        return SelendroidAccesibilityService.getInstance();
    }

    /**
     * Check that our instrumentation context has the required permissions to start an
     * {@link android.accessibilityservice.AccessibilityService}
     */
    public void checkPermissions() {
        PackageManager pm = mContext.getPackageManager();
        String packageName = mContext.getPackageName();

        if ((pm.checkPermission(PERMISSION_WRITE_SECURE_SETTINGS, packageName) ==
                PackageManager.PERMISSION_GRANTED)
                && (pm.checkPermission(PERMISSION_WRITE_SETTINGS, packageName) ==
                PackageManager.PERMISSION_GRANTED)) {
            return;
        }
        throw new SelendroidException(PERMISSION_WRITE_SECURE_SETTINGS
                + " required to enable accessibility services");
    }

    /**
     * Verify that {@link #mContext}'s package contains {@link io.selendroid.server.SelendroidAccesibilityService}
     */
    public void checkServiceIsInstalled() {
        List<AccessibilityServiceInfo> installedServices =
                mAccessibilityManager.getInstalledAccessibilityServiceList();

        boolean found = false;
        for (AccessibilityServiceInfo serviceInfo : installedServices) {
            ResolveInfo resolveInfo = serviceInfo.getResolveInfo();
            if (mContext.getPackageName().equals(
                    resolveInfo.serviceInfo.applicationInfo.packageName)
                    && resolveInfo.serviceInfo.name.equals(SELENDROID_ACCESSIBILITY_SERVICE_NAME)) {
                found = true;
                break;
            }
        }

        if (!found) {
            throw new SelendroidException("SelendroidAccessibilityService not installed");
        }
    }

    /**
     * Try to disable all accessibility and touch exploration enabled services and set
     * {@link Settings.Secure#ACCESSIBILITY_ENABLED} and
     * {@link Settings.Secure#TOUCH_EXPLORATION_ENABLED} to false
     */
    public void disableAccessibilityServices() {
        ContentResolver contentResolver = mContext.getContentResolver();

        String enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        SelendroidLogger.info("disabling enabled services: " + enabledServices);

        Settings.Secure.putString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Settings.Secure.putString(contentResolver,
                    TOUCH_EXPLORATION_GRANTED_ACCESSIBILITY_SERVICES,
                    "");
        }

        Settings.Secure.putInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Settings.Secure.putInt(contentResolver, Settings.Secure.TOUCH_EXPLORATION_ENABLED, 0);
        }

        syncAccessibilityState(AccessibilityState.DISABLED);
    }

    /**
     * Wait until {@link #mAccessibilityManager} enabled matches (@code expectedState)
     * It may block for up to {@link #ACCESSIBILITY_STATE_TIMEOUT_MS} milliseconds
     *
     * @param expectedState the valued that isEnabled should return
     */
    public void syncAccessibilityState(AccessibilityState expectedState) {
        final boolean shouldBeEnabled = expectedState == AccessibilityState.ENABLED;
        final long startTime = System.currentTimeMillis();
        synchronized (mAccessibilityStateLock) {
            mAccessibilityManager.addAccessibilityStateChangeListener(mAccessibilityStateChangeListener);

            try {
                while (mAccessibilityManager.isEnabled() != shouldBeEnabled) {
                    long timeLeft = ACCESSIBILITY_STATE_TIMEOUT_MS - (System.currentTimeMillis() - startTime);
                    if (timeLeft <= 0) {
                        break;
                    }

                    mAccessibilityStateLock.wait(
                            Math.min(ACCESSIBILITY_STATE_CHANGE_RETRY_INTERVAL_MS, timeLeft));
                }
            } catch (InterruptedException ie) {
                // Don't care
            } finally {
                mAccessibilityManager.removeAccessibilityStateChangeListener(
                        mAccessibilityStateChangeListener);
            }
            if (shouldBeEnabled != mAccessibilityManager.isEnabled()) {
                throw new SelendroidException(
                        "Failed so sync accessibility service state to " + expectedState);
            }
        }
    }

    /**
     * Wait until {@link #getService()} returns a value corresponding to {@code expectedState}. If
     * {@code true}, this method will wait until {@link #getService()} returns a not null instance of
     * {@link SelendroidAccesibilityService}, otherwise, it will wait until it returns {@code null}
     *
     * @param expectedState whether {@link #getService()} should return a valid instance or
     * {@code null}
     */
    public void syncAccessibilityServiceState(AccessibilityState expectedState) {
        final boolean shouldBeNotNull = expectedState == AccessibilityState.ENABLED;
        final long startTime = System.currentTimeMillis();
        try {
            while (true) {
                AccessibilityService service = getService();

                if (shouldBeNotNull) {
                    if (service != null) {
                        break;
                    }
                } else if (service == null) {
                    break;
                }

                long timeLeft = SYNC_SERVICE_TIMEOUT_MS - (System.currentTimeMillis() - startTime);
                if (timeLeft <= 0) {
                    break;
                }

                Thread.sleep(Math.min(SYNC_SERVICE_RETRY_INTERVAL_MS, timeLeft));
            }
        } catch (InterruptedException ie) {
            // Don't care
        }

        if (shouldBeNotNull ^ (getService() != null)) {
            throw new SelendroidException("Failed to sync accessibility service state " + expectedState);
        }
    }

    /**
     * Try to enable {@link SelendroidAccesibilityService} by enabling
     * {@link android.provider.Settings.Secure#ACCESSIBILITY_ENABLED} and
     * {@link android.provider.Settings.Secure#TOUCH_EXPLORATION_ENABLED} and setting
     * {@link SelendroidAccesibilityService} to be the only service in
     * {@link Settings.Secure#ENABLED_ACCESSIBILITY_SERVICES} and
     * {@link #TOUCH_EXPLORATION_GRANTED_ACCESSIBILITY_SERVICES}
     */
    public void enableAccessibilityService() {
        checkServiceIsInstalled();

        String serviceFullName = mContext.getPackageName()
                + "/" + SELENDROID_ACCESSIBILITY_SERVICE_NAME;
        ContentResolver contentResolver = mContext.getContentResolver();

        SelendroidLogger.info("enabling disabled services: " + serviceFullName);

        Settings.Secure.putString(contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                serviceFullName);
        if (!serviceFullName.equals(
                Settings.Secure.getString(contentResolver,
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES))) {
            SelendroidLogger.error("Failed to add service to list of enabled services");
            throw new SelendroidException("Failed to enable accessibility service");

        }

        Settings.Secure.putInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 1);
        if (Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0) != 1) {
            throw new SelendroidException("Failed to enable accessibility service");
        }

        syncAccessibilityState(AccessibilityState.ENABLED);
        syncAccessibilityServiceState(AccessibilityState.ENABLED);
    }

    /**
     * Wait until at least {@link #ACCESSIBILITY_IDLE_INTERVAL_MS} milliseconds have passed since the
     * last {@link AccessibilityEvent} was received or {@link #ACCESSIBILITY_IDLE_TIMEOUT_MS} have
     * passed
     */
    public void syncAccessibilityIdle() {
        boolean success = false;
        final long startTime = System.currentTimeMillis();

        synchronized (mAccessibilityEventLock) {
            try {
                while (true) {
                    long timeSinceLastEventMs = (System.currentTimeMillis() - mLastTimeEventReceived);
                    if (timeSinceLastEventMs >= ACCESSIBILITY_IDLE_INTERVAL_MS) {
                        success = true;
                        break;
                    }

                    long timeLeft = ACCESSIBILITY_IDLE_TIMEOUT_MS - (System.currentTimeMillis() - startTime);
                    if (timeLeft <= 0) {
                        break;
                    }
                    mAccessibilityEventLock.wait(Math.min(ACCESSIBILITY_IDLE_RETRY_INTERVAL_MS, timeLeft));
                }
            } catch (InterruptedException ie) {
                // Don't care
            }
        }

        if (!success) {
            throw new SelendroidException("Failed to get accessibility idle sync");
        }
    }

    /**
     * Send a sync accessibility event and wait for it to be receive
     */
    public void syncAccessibilityEventQueue() {
        startRecordingEvents();
        final AccessibilityEvent syncEvent = AccessibilityEvent.obtain();
        syncEvent.setEnabled(false);
        syncEvent.setEventType(SYNC_QUEUE_EVENT_TYPE);
        syncEvent.setParcelableData(SYNC_PARCELABLE);

        mAccessibilityManager.sendAccessibilityEvent(syncEvent);

        if (syncAccessibilityEventQueueWithMatcher(mSyncEventMatcher) == null) {
            throw new SelendroidException("Failed to sync accessibility event queue");
        }
    }
    /**
     * Wait until we receive an {@link AccessibilityEvent} that matches the provided matcher or
     * {@link #SYNC_EVENT_QUEUE_TIMEOUT_MS} milliseconds have passed
     *
     * @param matcher the {@link AccessibilityEventMatcher} that matches {@link AccessibilityEvent}s
     * we want to wait for
     * @return the first {@link AccessibilityEvent} that matches the given
     * {@link AccessibilityEventMatcher} or {@code null} if it failed
     */
    public AccessibilityEvent syncAccessibilityEventQueueWithMatcher(
            AccessibilityEventMatcher matcher) {
        long startTime = System.currentTimeMillis();

        synchronized (mReceivedEventsBuffer) {
            try {
                int index = 0;
                while (true) {
                    while (index < mReceivedEventsBuffer.size()) {
                        final AccessibilityEvent event = mReceivedEventsBuffer.get(index++);

                        if (matcher.matches(event)) {
                            return event;
                        }
                    }

                    long timeLeft = SYNC_EVENT_QUEUE_TIMEOUT_MS - (System.currentTimeMillis() - startTime);
                    if (timeLeft <= 0) {
                        break;
                    }
                    mReceivedEventsBuffer.wait(Math.min(SYNC_EVENT_QUEUE_RETRY_INTERVAL_MS, timeLeft));
                }
            } catch (InterruptedException ie) {
                // Don't care
            } finally {
                mRecordingEvents = false;
            }
        }

        return null;
    }

    private AccessibilityEvent syncAccessibilityEventQueueWithMatcher(
            AccessibilityEventMatcher matcher,
            int retries) {
        AccessibilityEvent event = syncAccessibilityEventQueueWithMatcher(matcher);
        if (event == null && retries > 0) {
            event = syncAccessibilityEventQueueWithMatcher(matcher, retries - 1);
        }
        return event;
    }

    /**
     * Tries to retrieve the {@link AccessibilityNodeInfo} corresponding to the provide {@link View} by sending a dummy
     * sync event throught it and waiting for it. This will only work if it's called from a non-UI thread.
     *
     * @param v
     */
    public AccessibilityNodeInfo getAccessibilityNodeForView(View v) {
        if (!mRecordingEvents) {
            startRecordingEvents();
        }
        AccessibilityEventMatcher viewSyncMatcher = new AccessibilityEventMatcher() {
            @Override
            public boolean matches(AccessibilityEvent event) {
                return isViewSyncEvent(event);
            }
        };

        final AccessibilityEvent syncEvent = AccessibilityEvent.obtain();
        syncEvent.setEnabled(false);
        syncEvent.setEventType(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
        syncEvent.setParcelableData(SYNC_PARCELABLE);
        syncEvent.setSource(v);

        mAccessibilityManager.sendAccessibilityEvent(syncEvent);

        AccessibilityEvent syncedEvent = syncAccessibilityEventQueueWithMatcher(viewSyncMatcher);
        return syncedEvent == null ? null : syncedEvent.getSource();
    }

    private boolean isQueueSyncEvent(AccessibilityEvent event) {
        return (event.getEventType() == SYNC_QUEUE_EVENT_TYPE) && isSyncEvent(event);
    }

    private boolean isViewSyncEvent(AccessibilityEvent event) {
        return (event.getEventType() == SYNC_VIEW_EVENT_TYPE) && isSyncEvent(event);
    }

    private boolean isSyncEvent(AccessibilityEvent event) {
        return (event.getParcelableData() instanceof  Bundle)
                && (((Bundle) event.getParcelableData())
                .getDouble(PARCELABLE_SYNC_KEY) == PARCELABLE_SYNC_VALUE);
    }

    private void startRecordingEvents() {
        synchronized (mReceivedEventsBuffer) {
            mReceivedEventsBuffer.clear();
            mRecordingEvents = true;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        synchronized (mAccessibilityEventLock) {
            mLastTimeEventReceived = System.currentTimeMillis();
        }

        synchronized (mReceivedEventsBuffer) {
            if (mRecordingEvents) {
                mReceivedEventsBuffer.add(AccessibilityEvent.obtain(event));
                mReceivedEventsBuffer.notifyAll();
            }
        }
    }

    /**
     * Start listening to {@link AccessibilityEvent}s
     */
    public void connectToService() {
        getService().setListener(this);
    }

    /**
     * Stop listening to {@link AccessibilityEvent}s
     */
    public void disconnectFromService() {
        if (getService() != null) {
            getService().setListener(null);
        }
    }
}
