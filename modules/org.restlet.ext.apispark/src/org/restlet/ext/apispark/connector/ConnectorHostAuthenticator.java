package org.restlet.ext.apispark.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.apispark.connector.client.AuthenticationClientResource;
import org.restlet.ext.apispark.connector.configuration.AuthenticationConfiguration;
import org.restlet.resource.ResourceException;
import org.restlet.security.Authenticator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ConnectorHostAuthenticator extends Authenticator {

    private AuthenticationConfiguration configuration;

    private LoadingCache<Credentials, List<String>> cache;

    public AuthenticationConfiguration getAuthenticationConfiguration() {
        return configuration;
    }

    public void setAuthenticationConfiguration(
            AuthenticationConfiguration configuration) {
        this.configuration = configuration;
    }

    public ConnectorHostAuthenticator(Context context,
            AuthenticationConfiguration configuration) {
        super(context);
        this.configuration = configuration;
        initializeCache();
    }

    private void initializeCache() {
        // TODO see to this
        CacheLoader<Credentials, List<String>> loader = new CacheLoader<Credentials, List<String>>() {
            public List<String> load(Credentials key) {
                return new ArrayList<String>();
            }
        };
        this.cache = CacheBuilder
                .newBuilder()
                .maximumSize(configuration.getCacheSize())
                .expireAfterWrite(configuration.getRefreshRate(),
                        TimeUnit.SECONDS).build(loader);
    }

    @Override
    protected boolean authenticate(Request request, Response response) {
        Credentials credentials = new Credentials(request
                .getChallengeResponse().getIdentifier(), request
                .getChallengeResponse().getSecret());
        if (cache.asMap().containsKey(credentials)) {
            return true;
        } else {
            AuthenticationClientResource cr = new AuthenticationClientResource(
                    configuration.getEndpoint(), configuration.getUsername(),
                    configuration.getPassword());
            try {
                List<String> roles = cr.represent(request
                        .getChallengeResponse().getIdentifier(), request
                        .getChallengeResponse().getSecret());
                if (roles != null) {
                    cache.put(credentials, roles);
                    return true;
                }
            } catch (ResourceException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
