package io.selendroid.standalone.server.model.impl;

import io.selendroid.standalone.android.AndroidSdk;
import io.selendroid.standalone.android.DeviceManager;
import io.selendroid.standalone.android.HardwareDeviceListener;
import io.selendroid.standalone.android.impl.DefaultDeviceManager;
import io.selendroid.standalone.server.model.*;

public class DefaultInitAndroidDevicesStrategy implements InitAndroidDevicesStrategy {

    @Override
    public InitAndroidDevicesConfig getInitAndroidDevicesConfig(SelendroidStandaloneDriver driver) {
        boolean keepAdbAlive = driver.getSelendroidConfiguration().shouldKeepAdbAlive();
        DeviceManager deviceManager =
                new DefaultDeviceManager(AndroidSdk.adb().getAbsolutePath(), keepAdbAlive);

        DeviceStore deviceStore =
                new DefaultDeviceStore(driver.getSelendroidConfiguration().getEmulatorPort(), deviceManager);
        HardwareDeviceListener listener = new DefaultHardwareDeviceListener((DefaultDeviceStore)deviceStore, driver);
        return new InitAndroidDevicesConfig(deviceStore, deviceManager, listener, keepAdbAlive);
    }
}