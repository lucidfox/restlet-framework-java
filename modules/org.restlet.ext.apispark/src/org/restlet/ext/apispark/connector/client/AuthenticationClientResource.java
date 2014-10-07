package org.restlet.ext.apispark.connector.client;

import java.util.List;

import org.restlet.data.ChallengeScheme;
import org.restlet.ext.apispark.connector.resource.AuthenticationResource;
import org.restlet.resource.ClientResource;

public class AuthenticationClientResource implements AuthenticationResource {

    private AuthenticationResource delegate;

    public AuthenticationClientResource(String path, String username,
            String password) {
        ClientResource cr = new ClientResource(path);
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, username, password);
        this.delegate = cr.wrap(AuthenticationResource.class);
    }

    @Override
    public List<String> represent(String username, char[] secret) {
        return delegate.represent(username, secret);
    }

}
