package io.selendroid.standalone.server.model.impl;


import io.selendroid.standalone.android.DeviceManager;
import io.selendroid.standalone.android.HardwareDeviceListener;
import io.selendroid.standalone.server.model.DeviceStore;

public class InitAndroidDevicesConfig {

    private DeviceStore  deviceStore;
    private DeviceManager deviceManager;
    private HardwareDeviceListener listener;
    private boolean shouldKeepAdbAlive;

    public InitAndroidDevicesConfig(DeviceStore deviceStore, DeviceManager deviceManager, HardwareDeviceListener listener,
                                    boolean shouldKeepAdbAlive) {
        this.deviceManager = deviceManager;
        this.deviceStore = deviceStore;
        this.listener = listener;
        this.shouldKeepAdbAlive = shouldKeepAdbAlive;
    }

    public DeviceStore getDeviceStore() {
        return deviceStore;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public boolean isShouldKeepAdbAlive() {
        return shouldKeepAdbAlive;
    }

    public HardwareDeviceListener getListener() {
        return listener;
    }
}
