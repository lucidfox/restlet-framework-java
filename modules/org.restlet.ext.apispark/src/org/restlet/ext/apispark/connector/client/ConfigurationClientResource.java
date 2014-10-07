package org.restlet.ext.apispark.connector.client;

import org.restlet.data.ChallengeScheme;
import org.restlet.ext.apispark.connector.configuration.Configuration;
import org.restlet.ext.apispark.connector.resource.ConfigurationResource;
import org.restlet.resource.ClientResource;

public class ConfigurationClientResource implements ConfigurationResource {

    private ConfigurationResource delegate;

    public ConfigurationClientResource(String path, String username,
            char[] password) {
        ClientResource cr = new ClientResource(path);
        String pw = "";
        for (char c : password) {
            pw += c;
        }
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, username, pw);
        this.delegate = cr.wrap(ConfigurationResource.class);
    }

    @Override
    public Configuration represent() {
        return delegate.represent();
    }

}
