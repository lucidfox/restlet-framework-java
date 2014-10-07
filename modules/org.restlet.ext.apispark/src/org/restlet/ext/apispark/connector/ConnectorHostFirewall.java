package org.restlet.ext.apispark.connector;

import org.restlet.ext.apispark.FirewallFilter;
import org.restlet.ext.apispark.connector.configuration.FirewallConfiguration;

public class ConnectorHostFirewall extends FirewallFilter {

    private FirewallConfiguration configuration;

    public ConnectorHostFirewall(FirewallConfiguration configuration) {
        this.configuration = configuration;
        configure();
    }

    public FirewallConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(FirewallConfiguration configuration) {
        this.configuration = configuration;
    }

    public void configure() {
        // TODO copy APISpark's implementation
    }

}
