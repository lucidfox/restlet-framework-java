package org.restlet.ext.apispark.connector;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.apispark.connector.client.ConfigurationClientResource;
import org.restlet.ext.apispark.connector.configuration.Configuration;
import org.restlet.routing.Filter;

public class ConnectorHostFilter extends Filter {

    private String path;

    private String username;

    private char[] password;

    private Configuration configuration;

    public ConnectorHostFilter(Context context, String path, String username,
            char[] password, Restlet next) {
        super(context, next);
        this.path = path;
        this.username = username;
        this.password = password;
        configure();
    }

    private void configure() {
        configuration = new ConfigurationClientResource(path, username,
                password).represent();

        if (configuration != null) {
            ConnectorHostAuthenticator guard = null;
            ConnectorHostFirewall firewall = null;
            if (configuration.getAuthenticationConfiguration().isEnabled()) {
                guard = new ConnectorHostAuthenticator(getContext(),
                        configuration.getAuthenticationConfiguration());
            }
            if (configuration.getFirewallConfiguration().isEnabled()) {
                firewall = new ConnectorHostFirewall(
                        configuration.getFirewallConfiguration());
            }

            if (guard == null && firewall != null) {
                firewall.setNext(getNext());
                setNext(firewall);
            } else if (guard != null && firewall == null) {
                guard.setNext(getNext());
                setNext(guard);
            } else {
                firewall.setNext(getNext());
                setNext(guard);
                guard.setNext(firewall);
            }
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
