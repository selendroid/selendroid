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
package io.selendroid.server.model;

import android.support.test.uiautomator.UiDevice;
import android.test.InstrumentationTestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import io.selendroid.server.ServerInstrumentation;
import io.selendroid.server.android.InstrumentedKeySender;
import io.selendroid.server.android.KeySender;
import io.selendroid.server.android.internal.Dimension;
import io.selendroid.server.common.exceptions.NoSuchMethodImplementationException;
import io.selendroid.server.common.exceptions.SelendroidException;
import io.selendroid.server.util.Preconditions;
import io.selendroid.server.util.SelendroidLogger;


public class DefaultAppiumUiAutomatorDriver implements SelendroidDriver {

    public static final String BROWSER_NAME = "browserName";
    public static final String PLATFORM = "platform";
    public static final String SUPPORTS_JAVASCRIPT = "javascriptEnabled";
    public static final String TAKES_SCREENSHOT = "takesScreenshot";
    public static final String SUPPORTS_ALERTS = "handlesAlerts";
    public static final String ROTATABLE = "rotatable";
    public static final String ACCEPT_SSL_CERTS = "acceptSslCerts";
    public static final String SUPPORTS_NETWORK_CONNECTION = "networkConnectionEnabled";
    public static final String AUTOMATION_NAME = "automationName";
    public static final String PLATFORM_NAME = "platformName";
    public static final String PLATFORM_VERSION = "platformVersion";
    public static final String SERVER_VERSION = "version";
    public static final String METHOD_NOT_IMPLMENTED_EXCEPTION = "Method has no Implementation";
    private ServerInstrumentation serverInstrumentation = null;
    private KeySender keySender = null;
    private Session session = null;


    private UiDevice mDevice;
    private SelendroidWebDriver selendroidWebDriver = null;

    public DefaultAppiumUiAutomatorDriver(ServerInstrumentation serverInstrumentation) {
        this.keySender = new InstrumentedKeySender(serverInstrumentation);
        this.serverInstrumentation = serverInstrumentation;
        // An exception is thrown right here
        // mDevice = UiDevice.getInstance(serverInstrumentation);
    }

    @Override
    public AndroidElement findElement(By by) {
        mDevice = UiDevice.getInstance(serverInstrumentation);
        mDevice.pressMenu();
        return null;
    }

    @Override
    public List<AndroidElement> findElements(By by) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public String getCurrentUrl() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public JSONObject getSessionCapabilities(String sessionId) {
        SelendroidLogger.info("session: " + sessionId);

        JSONObject sessionCapabilities;
        try {
            JSONObject defaultSessionCapabilities = session.getCapabilities();
            if (defaultSessionCapabilities != null) {
                sessionCapabilities = new JSONObject(defaultSessionCapabilities.toString());
            } else {
                sessionCapabilities = new JSONObject();
            }
            sessionCapabilities.put(TAKES_SCREENSHOT, true);
            sessionCapabilities.put(BROWSER_NAME, "selendroid");
            sessionCapabilities.put(AUTOMATION_NAME, "uiautomator");
            sessionCapabilities.put(PLATFORM_NAME, "android");
            sessionCapabilities.put(PLATFORM_VERSION, serverInstrumentation.getOsVersion());
            sessionCapabilities.put(ROTATABLE, true);
            sessionCapabilities.put(PLATFORM, "android");
            sessionCapabilities.put(SUPPORTS_ALERTS, true);
            sessionCapabilities.put(SUPPORTS_JAVASCRIPT, true);
            sessionCapabilities.put(SUPPORTS_NETWORK_CONNECTION, true);
            sessionCapabilities.put(SERVER_VERSION, serverInstrumentation.getServerVersion());
            sessionCapabilities.put(ACCEPT_SSL_CERTS, true);
            SelendroidLogger.info("Session capabilities: " + sessionCapabilities);
            return sessionCapabilities;
        } catch (JSONException e) {
            throw new SelendroidException(e);
        }
    }

    @Override
    public JSONObject getFullWindowTree() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public String getWindowSource() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public String initializeSession(JSONObject desiredCapabilities) {
        // Temporary solution for session creation and this needs to be modified
        Random random = new Random();
        return new UUID(random.nextLong(), random.nextLong()).toString();
    }

    @Override
    public void stopSession() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void switchContext(String type) {
        Preconditions.checkNotNull(type);
        initSelendroidWebDriver(type);
        session.getKnownElements().clear();
    }

    @Override
    public byte[] takeScreenshot() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public Keyboard getKeyboard() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public String getTitle() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void get(String url) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public TouchScreen getTouch() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void addCookie(String url, Cookie cookie) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void deleteCookie(String url) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void deleteNamedCookie(String url, String name) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public Set<Cookie> getCookies(String url) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public Object executeScript(String script, JSONArray args) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public Object executeScript(String script, Object... args) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public Object executeAsyncScript(String script, JSONArray args) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public String getContext() {
        return selendroidWebDriver.getContextHandle();
    }

    private void initSelendroidWebDriver(String type) {
        selendroidWebDriver = new SelendroidWebDriver(serverInstrumentation, type);

    }

    @Override
    public Set<String> getContexts() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public Dimension getWindowSize() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void setFrameContext(Object index) throws JSONException {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void back() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void forward() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void refresh() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public boolean isAlertPresent() {
        // actual logic that detects alert presence need to be implemented
        return false;
    }

    @Override
    public String getAlertText() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void setAlertText(CharSequence... keysToSend) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void acceptAlert() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void dismissAlert() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public ScreenOrientation getOrientation() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void rotate(ScreenOrientation orientation) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void setAsyncTimeout(long timeout) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void setPageLoadTimeout(long timeout) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public boolean isAirplaneMode() {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }

    @Override
    public void roll(int dimensionX, int dimensionY) {
        throw new NoSuchMethodImplementationException(METHOD_NOT_IMPLMENTED_EXCEPTION);
    }
}
