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
package io.selendroid.standalone.server.model;


import com.google.common.base.Predicate;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.AndroidDevice;
import io.selendroid.standalone.android.AndroidEmulator;
import io.selendroid.standalone.android.HardwareDeviceListener;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.exceptions.DeviceStoreException;

import java.util.List;

public interface DeviceStore {
    Integer nextEmulatorPort();

    void release(AndroidDevice device, AndroidApp aut);

    /* package */ void initAndroidDevices(HardwareDeviceListener hardwareDeviceListener,
                                          boolean shouldKeepAdbAlive) throws AndroidDeviceException;

    void addDevice(AndroidDevice androidDevice) throws AndroidDeviceException;

    void updateDevice(AndroidDevice device) throws AndroidDeviceException;

    void addEmulators(List<AndroidEmulator> emulators) throws AndroidDeviceException;

    AndroidDevice findAndroidDevice(SelendroidCapabilities caps) throws DeviceStoreException;

    List<AndroidDevice> getDevices();

    void removeAndroidDevice(AndroidDevice device) throws DeviceStoreException;

    void setClearData(boolean clearData);

    void setKeepEmulator(boolean keepEmulator);

}
