package org.restlet.test.ext.apispark;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MemoryRealm;
import org.restlet.security.User;

public class MockApisparkApplication extends Application {

    public MockApisparkApplication() {
        setName("Mock APISpark");
    }

    @Override
    public Restlet createInboundRoot() {
        Router baseRouter = new Router();
        baseRouter.setDefaultMatchingMode(Router.MODE_BEST_MATCH);
        baseRouter.attach("/configuration", ConfigurationServerResource.class);
        baseRouter
                .attach("/authentication", AuthenticationServerResource.class);
        return createApiGuard(baseRouter);
    }

    private ChallengeAuthenticator createApiGuard(Router router) {

        ChallengeAuthenticator apiGuard = new ChallengeAuthenticator(
                getContext(), ChallengeScheme.HTTP_BASIC, "realm");

        MemoryRealm realm = new MemoryRealm();
        User owner = new User("owner", "owner");
        realm.getUsers().add(owner);
        apiGuard.setVerifier(realm.getVerifier());
        apiGuard.setEnroler(realm.getEnroler());
        apiGuard.setNext(router);
        return apiGuard;
    }

}
