package io.selendroid.server;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import io.selendroid.server.common.exceptions.BaseAccessibilityExtension;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.extension.ExtensionLoader;
import io.selendroid.server.util.AccessibilityServiceManager;
import io.selendroid.server.util.SelendroidLogger;
import org.json.JSONObject;

/**
 * Bound service that allows interaction with {@link SelendroidAccesibilityService} through a {@link Messenger}
 */
public class AccessibilityServiceInteractionService extends Service {
    public static final String EXTRA_PAYLOAD = "io.selendroid.server.EXTRA_PAYLOAD";
    public static final String EXTRA_EXTENSION_DEX_PATH = "io.selendroid.server.EXTRA_EXTENSION_DEX_PATH";
    public static final String EXTRA_EXTENSION_CLASS_NAME = "io.selendroid.server.EXTRA_EXTENSION_CLASS_NAME";
    public static final String EXTRA_EXTENSION_RESULT = "io.selendroid.server.EXTRA_EXTENSION_RESULT";

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_START_ACCESSIBILITY_SERVICE = 2;
    public static final int MSG_STOP_ACCESSIBILITY_SERVICE = 3;
    public static final int MSG_STOPPED_ACCESSIBILITY_SERVICE = 4;
    public static final int MSG_STARTED_ACCESSIBILITY_SERVICE = 5;
    public static final int MSG_UNREGISTER_CLIENT = 6;
    public static final int MSG_EXECUTE_ACCESSIBILITY_EXTENSION = 7;

    private AccessibilityServiceManager mServiceManager;
    private Messenger client;
    private final Messenger messenger = new Messenger(new MessageHandler());
    private AsyncTask setupTask;
    private AsyncTask tearDownTask;
    private ExtensionLoader extensionLoader;

    /**
     * {@link Handler} implementation that manages messages received by this service's {@link Binder}
     */
    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            SelendroidLogger.info("handleMessage " + msg.what);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    registerClient(msg);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    unregisterClient(msg);
                    break;
                case MSG_START_ACCESSIBILITY_SERVICE:
                    setupTask = new AccessibilitySetupTask().execute();
                    break;
                case MSG_STOP_ACCESSIBILITY_SERVICE:
                    tearDownTask = new AccessibilityTearDownTask().execute();
                    break;
                case MSG_EXECUTE_ACCESSIBILITY_EXTENSION:
                    callAccessibilityExtension(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
            SelendroidLogger.info("done with handleMessage " + msg.what);
        }
    }

    /**
     * Registers the {@link Messenger} object for the client in {@link ServerInstrumentation} to enable
     * two-way communication, no other messages can be received before this one. We also initialize the
     * {@link ExtensionLoader} at this point.
     *
     * @param msg The {@link Message} containing the {@link Messenger} client object and, optionally the path
     *            of the dex from which we can load extensions
     */
    private void registerClient(Message msg) {
        client = msg.replyTo;
        String extensionDexPath = msg.getData().getString(EXTRA_EXTENSION_DEX_PATH);
        if (extensionDexPath != null) {
            extensionLoader = new ExtensionLoader(getApplicationContext(), extensionDexPath);
        } else {
            extensionLoader = new ExtensionLoader(getApplicationContext());
        }
    }

    /**
     * Removes the reference to the {@link Messenger} object for the client in {@link ServerInstrumentation} and stops
     * this service
     *
     * @param msg The message received
     */
    private void unregisterClient(Message msg) {
        client = null;
        if (setupTask != null) {
            setupTask.cancel(true);
        }
        if (tearDownTask != null) {
            tearDownTask.cancel(true);
        }
        stopSelf();
    }

    /**
     * Loads and calls the {@link BaseAccessibilityExtension} specified in {@code msg}'s bundle, under the
     * {@link #EXTRA_EXTENSION_CLASS_NAME} key.
     *
     * @param msg The {@link Message} containing the information required to perform this operation
     */
    private void callAccessibilityExtension(Message msg) {
        if (client == null || extensionLoader == null) {
            throw new SelendroidException("Trying to call accessibility extension without registering client");
        }

        try {
            BaseAccessibilityExtension extension =
                    extensionLoader.loadAccessibilityExtension(
                            msg.getData().getString(EXTRA_EXTENSION_CLASS_NAME));
            JSONObject payload = new JSONObject(msg.getData().getString(EXTRA_PAYLOAD));
            JSONObject result = extension.execute(payload);
            SelendroidLogger.info("Trying to load ax extension" + msg.getData().getString(EXTRA_EXTENSION_CLASS_NAME));

            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_EXTENSION_RESULT, result.toString());

            Message response = Message.obtain(null, MSG_EXECUTE_ACCESSIBILITY_EXTENSION);
            response.setData(bundle);

            client.send(response);
        } catch (Exception e) {
            throw new SelendroidException(e);
        }
    }

    /**
     * {@link AsyncTask} that performs the initialization and enabling of {@link SelendroidAccesibilityService}
     */
    class AccessibilitySetupTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mServiceManager.doAccessibiliyServiceSetup();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                client.send(Message.obtain(null, MSG_STARTED_ACCESSIBILITY_SERVICE));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * {@link AsyncTask} that performs the disabling of {@link SelendroidAccesibilityService}
     */
    class AccessibilityTearDownTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mServiceManager.doAccessibilityServiceTearDown();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                client.send(Message.obtain(null, MSG_STOPPED_ACCESSIBILITY_SERVICE));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceManager = new AccessibilityServiceManager(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }
}
