package io.selendroid.standalone.server.model.impl;

import io.selendroid.standalone.server.model.SelendroidStandaloneDriver;

public interface InitAndroidDevicesStrategy {

    public InitAndroidDevicesConfig getInitAndroidDevicesConfig(SelendroidStandaloneDriver driver);
}
