package org.restlet.test.ext.apispark;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class MockApiApplication extends Application {

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach("/ping", MockApiServerResource.class);
        return router;
    };
}
