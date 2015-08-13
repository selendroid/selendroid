package io.selendroid.standalone.server.model;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.common.device.DeviceTargetPlatform;
import io.selendroid.standalone.android.AndroidApp;
import io.selendroid.standalone.android.AndroidDevice;
import io.selendroid.standalone.android.AndroidEmulator;
import io.selendroid.standalone.android.AndroidEmulatorPowerStateListener;
import io.selendroid.standalone.android.impl.AbstractAndroidDeviceEmulator;
import io.selendroid.standalone.exceptions.AndroidDeviceException;
import io.selendroid.standalone.exceptions.DeviceStoreException;

import java.util.*;
import java.util.logging.Logger;

public abstract class AbstractDeviceStore implements DeviceStore {
    protected AndroidEmulatorPowerStateListener emulatorPowerStateListener = null;
    private static final Logger log = Logger.getLogger(AbstractDeviceStore.class.getName());
    protected Map<DeviceTargetPlatform, List<AndroidDevice>> androidDevices =
            new HashMap<DeviceTargetPlatform, List<AndroidDevice>>();
    protected EmulatorPortFinder androidEmulatorPortFinder = null;
    protected List<AndroidDevice> devicesInUse = new ArrayList<AndroidDevice>();
    protected boolean clearData = true;
    protected boolean keepEmulator = false;

    @Override
    public synchronized void addDevice(AndroidDevice androidDevice) throws AndroidDeviceException {
      if (androidDevice == null) {
        log.info("No Android devices were found.");
        return;
      }
      if (androidDevice instanceof AndroidEmulator) {
        throw new AndroidDeviceException(
                "For adding emulator instances please use #addEmulator method.");
      }
      if (androidDevice.isDeviceReady() == true) {
        log.info("Adding: " + androidDevice);
        addDeviceToStore(androidDevice);
      }
    }

    /**
     * Internal method to add an actual device to the store.
     *
     * @param device The device to add.
     * @throws AndroidDeviceException
     */
    protected synchronized void addDeviceToStore(AndroidDevice device) throws AndroidDeviceException {
      if (androidDevices.containsKey(device.getTargetPlatform())) {
        List<AndroidDevice> platformDevices = androidDevices.get(device.getTargetPlatform());
        if (!platformDevices.contains(device)) {
          platformDevices.add(device);
        }
      } else {
        androidDevices.put(device.getTargetPlatform(), Lists.newArrayList(device));
      }
    }

    @Override
    public void addEmulators(List<AndroidEmulator> emulators) throws AndroidDeviceException {
      if (emulators == null || emulators.isEmpty()) {
        log.info("No emulators has been found.");
        return;
      }
      for (AndroidEmulator emulator : emulators) {
        log.info("Adding: " + emulator);
        addDeviceToStore((AndroidDevice) emulator);
      }
    }

    @Override
    public synchronized void updateDevice(AndroidDevice device) throws AndroidDeviceException {
      boolean deviceRemoved = false;
      for (DeviceTargetPlatform targetPlatform : androidDevices.keySet()) {
        List<AndroidDevice> platformDevices = androidDevices.get(targetPlatform);
        // Attempt to remove the device from this target platform;
        deviceRemoved |= platformDevices.remove(device);
      }

      if (deviceRemoved) {
        addDeviceToStore(device);
      } else {
        log.warning("Attempted to update device which did could not be found in the device store");
      }
    }

    @Override
    public List<AndroidDevice> getDevices() {
      List<AndroidDevice> devices = new ArrayList<AndroidDevice>();
      for (Map.Entry<DeviceTargetPlatform, List<AndroidDevice>> entry : androidDevices.entrySet()) {
        devices.addAll(entry.getValue());
      }
      return devices;
    }

    @Override
    public Integer nextEmulatorPort() {
      return androidEmulatorPortFinder.next();
    }

    /**
     * Finds a device for the requested capabilities. <b>important note:</b> if the device is not any
     * longer used, call the {@link #release(AndroidDevice, AndroidApp)} method.
     *
     * @param caps The desired test session capabilities.
     * @return Matching device for a test session.
     * @throws DeviceStoreException
     * @see {@link #release(AndroidDevice, AndroidApp)}
     */
    @Override
    public synchronized AndroidDevice findAndroidDevice(SelendroidCapabilities caps) throws DeviceStoreException {

      Preconditions.checkArgument(caps != null, "Error: capabilities are null");

      if (androidDevices.isEmpty()) {
        throw new DeviceStoreException("Fatal Error: Device Store does not contain any Android Device.");
      }

      String platformVersion = caps.getPlatformVersion();

      Iterable<AndroidDevice> candidateDevices = Strings.isNullOrEmpty(platformVersion) ?
              Iterables.concat(androidDevices.values()) : androidDevices.get(DeviceTargetPlatform.fromPlatformVersion(platformVersion));

      candidateDevices = Objects.firstNonNull(candidateDevices, Collections.EMPTY_LIST);

      FluentIterable<AndroidDevice> allMatchingDevices = FluentIterable.from(candidateDevices)
              .filter(deviceNotInUse())
              .filter(deviceSatisfiesCapabilities(caps));

      if (!allMatchingDevices.isEmpty()) {

        AndroidDevice matchingDevice = allMatchingDevices.filter(deviceRunning()).first()
                .or(allMatchingDevices.first()).get();

        if (!deviceRunning().apply(matchingDevice)) {
          log.info("Using potential match: " + matchingDevice);
        }

        devicesInUse.add(matchingDevice);
        return matchingDevice;
      } else {
          throw new DeviceStoreException("No devices are found. "
              + "This can happen if the devices are in use or no device screen "
              + "matches the required capabilities.");
      }
    }

    private Predicate<AndroidDevice> deviceNotInUse() {
      return new Predicate<AndroidDevice>() {
        @Override
        public boolean apply(AndroidDevice candidate) {
          return !devicesInUse.contains(candidate);
        }
      };
    }

    private Predicate<AndroidDevice> deviceRunning() {
      return new Predicate<AndroidDevice>() {
        @Override
        public boolean apply(AndroidDevice candidate) {
            if (candidate instanceof AndroidEmulator) {
                AndroidEmulator emulator = (AndroidEmulator) candidate;

            }
            if (candidate instanceof AbstractAndroidDeviceEmulator) {
                AbstractAndroidDeviceEmulator emulator = (AbstractAndroidDeviceEmulator) candidate;
                try {
                    return emulator.isEmulatorStarted();
                } catch (AndroidDeviceException e) {
                    return false;
                }
            } else {
                return true;
            }
        }
      };
    }

    @Override
    public void setClearData(boolean clearData) {
        this.clearData = clearData;
    }

    @Override
    public void setKeepEmulator(boolean keepEmulator) {
        this.keepEmulator = keepEmulator;
    }

    protected abstract Predicate<AndroidDevice> deviceSatisfiesCapabilities(SelendroidCapabilities capabilities);
}
