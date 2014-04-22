package io.selendroid;

/**
 * Setting the Airplane mode (aka no network connectivity)
 */
public interface HasAirplaneMode {

    /**
     * @return true if airplane mode is currently enabled
     */
    public boolean isAirplaneModeEnabled();

    /**
     * set the device's airplane mode
     * @param enabled - true to enable airplane mode (disable network connectivity)
     */
    public void setAirplaneMode(boolean enabled);
}
