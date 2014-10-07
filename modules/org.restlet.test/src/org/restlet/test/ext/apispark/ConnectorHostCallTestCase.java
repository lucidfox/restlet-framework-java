package org.restlet.test.ext.apispark;

import java.io.IOException;

import org.restlet.Component;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.ext.apispark.connector.ConnectorHostApplication;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.test.RestletTestCase;

public class ConnectorHostCallTestCase extends RestletTestCase {

    public void test() throws Exception {

        /*
         * Create the mock Web API
         */
        Component mockApi = new Component();
        mockApi.getServers().add(Protocol.HTTP, 1338);
        mockApi.getDefaultHost().attach("", new MockApiApplication());
        mockApi.start();

        /*
         * Create the Mock APISpark
         */
        Component mockApispark = new Component();
        mockApispark.getServers().add(Protocol.HTTP, 8182);
        mockApispark.getDefaultHost().attach("", new MockApisparkApplication());
        mockApispark.start();

        /*
         * Create the ConnectorHost
         */
        Component connectorHost = new Component();
        connectorHost.getServers().add(Protocol.HTTP, 1337);
        connectorHost.getDefaultHost().attach(
                "/v1",
                new ConnectorHostApplication(
                        "http://localhost:8182/configuration", "owner", "owner"
                                .toCharArray()));
        connectorHost.start();

        callApi();
    }

    private void callApi() throws ResourceException, IOException {
        ClientResource cr1 = new ClientResource("http://localhost:1337/v1/ping");
        cr1.setQueryValue("media", "text");

        // Test successful call
        cr1.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "guru", "guru");
        assertEquals("GET OK!", cr1.get().getText());

        // Test failed call
        ClientResource cr2 = new ClientResource("http://localhost:1337/v1/ping");
        cr2.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "guru", "");
        assertNotSame("GET OK!", cr2.get().getText());
    }
}
