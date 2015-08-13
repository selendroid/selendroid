package io.selendroid.standalone.server.handler;

import io.netty.handler.codec.http.HttpMethod;

import io.selendroid.standalone.android.impl.AbstractAndroidDeviceEmulator;
import io.selendroid.standalone.server.BaseSelendroidStandaloneHandler;
import org.json.JSONException;
import org.json.JSONObject;

import io.selendroid.server.common.Response;
import io.selendroid.server.common.SelendroidResponse;
import io.selendroid.server.common.http.HttpRequest;
import io.selendroid.standalone.android.AndroidDevice;
import io.selendroid.standalone.android.KeyEvent;
import io.selendroid.standalone.android.impl.DefaultAndroidEmulator;
import io.selendroid.standalone.server.model.ActiveSession;
import io.selendroid.standalone.server.util.HttpClientUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkConnectionHandler extends BaseSelendroidStandaloneHandler {
  private static final Logger log = Logger.getLogger(NetworkConnectionHandler.class.getName());

  public NetworkConnectionHandler(String mappedUri) {
    super(mappedUri);
  }

  @Override
  public Response handleRequest(HttpRequest request, JSONObject payload) throws JSONException {
    // Currently we only support changing the Airplane Mode setting
    // so we're going to only look at that bit of the ConnectionType.

    // check that we actually want to toggle the setting by getting the current airplane mode setting
    String sessionId = getSessionId(request);
    ActiveSession session = getActiveSession(request);
    // the URL happens to be the same, except we need to use GET instead of POST
    String url = "http://" + session.getHostname() + ":" + session.getSelendroidServerPort() + request.uri();
    Integer connectionType = getPayload(request).getJSONObject("parameters").getInt("type");
    try {
      JSONObject r = HttpClientUtil.parseJsonResponse(HttpClientUtil.executeRequest(url, HttpMethod.GET));
      // Airplane mode is the first bit, so even is disabled, odd is disabled
      if (r.getInt("value") % 2 == connectionType % 2) {
        // airplane modes are the same
        return new SelendroidResponse(sessionId, null);
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Cannot change network connection", e);
      return new SelendroidResponse(sessionId, null);
    }

    Boolean airplaneMode = connectionType % 2 == 1;

    AndroidDevice device = getSelendroidDriver(request).getActiveSession(getSessionId(request)).getDevice();

    int deviceAPILevel = Integer.parseInt(device.getTargetPlatform().getApi());

    device.invokeActivity("android.settings.AIRPLANE_MODE_SETTINGS");

    // tap to make sure none of the selections are currently highlighted
    device.runAdbCommand("shell input tap 600 100");

    device.inputKeyevent(KeyEvent.KEYCODE_DPAD_DOWN);

    device.inputKeyevent(KeyEvent.KEYCODE_DPAD_CENTER);

    if (airplaneMode) {
      // adb connection needs a recycle when switching airplane mode on
      device.restartADB();
      // killing the server kills all the tcp forwards, need to re-establish them.
      for (ActiveSession activeSession : getSelendroidDriver(request).getActiveSessions()) {
        device.forwardPort(activeSession.getSelendroidServerPort(), activeSession.getSelendroidServerPort());
      }
    } else if (deviceAPILevel == 17 && device instanceof AbstractAndroidDeviceEmulator) {
      // data doesn't automatically get re-enabled when toggling it back on
      device.runAdbCommand("shell svc data disable");
      device.runAdbCommand("shell svc data enable");
    }


    // nice and magical, this will 'close' the settings view and reopen the last application, the one under test! :)
    if (deviceAPILevel >= 21) {
        //click default back button 3 times to get back to the app under test.  app > Settings > Network Settings
        device.inputKeyevent(KeyEvent.KEYCODE_BACK);
        device.inputKeyevent(KeyEvent.KEYCODE_BACK);
        device.inputKeyevent(KeyEvent.KEYCODE_BACK);
    } else {
        //click back button at the top of the Network Settings Activity
        device.inputKeyevent(KeyEvent.KEYCODE_DPAD_UP);
        device.inputKeyevent(KeyEvent.KEYCODE_DPAD_CENTER);
    }
    // airplane mode is 1, Data + WIFI is 6
    return new SelendroidResponse(sessionId, airplaneMode ? 1 : 6);
  }
}
