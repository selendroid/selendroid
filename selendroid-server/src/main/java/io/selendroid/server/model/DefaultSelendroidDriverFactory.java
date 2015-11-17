package io.selendroid.server.model;

import io.selendroid.server.ServerInstrumentation;

public class DefaultSelendroidDriverFactory implements SelendroidDriverFactory {

    private static SelendroidDriver selendroidDriver = null;
    private static final String SELENDROID = "selendroid";
    private static final String UIAUTOMATOR = "uiautomator";

    @Override
    public SelendroidDriver createSelendroidDriver(ServerInstrumentation androidInstrumentation, String automationName) {
        if (selendroidDriver == null) {
            if (automationName.equals(SELENDROID)) {
                selendroidDriver = new DefaultSelendroidDriver(androidInstrumentation);
            } else if (automationName.equals(UIAUTOMATOR)) {
                // custom appium Uiautomatorv2 driver
                selendroidDriver = new DefaultAppiumUiAutomatorDriver(androidInstrumentation);
            }
        }
        return selendroidDriver;
    }

}
