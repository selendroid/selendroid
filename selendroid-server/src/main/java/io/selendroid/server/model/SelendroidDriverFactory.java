package io.selendroid.server.model;

import io.selendroid.server.ServerInstrumentation;

public interface SelendroidDriverFactory {
     SelendroidDriver createSelendroidDriver( ServerInstrumentation androidInstrumentation );

}
