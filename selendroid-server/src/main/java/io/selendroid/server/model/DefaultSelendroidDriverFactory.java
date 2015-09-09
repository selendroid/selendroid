package io.selendroid.server.model;

import io.selendroid.server.ServerInstrumentation;

public class DefaultSelendroidDriverFactory implements SelendroidDriverFactory {

    @Override
    public SelendroidDriver createSelendroidDriver( ServerInstrumentation androidInstrumentation ) {
        return new DefaultSelendroidDriver(androidInstrumentation);
    }

}
