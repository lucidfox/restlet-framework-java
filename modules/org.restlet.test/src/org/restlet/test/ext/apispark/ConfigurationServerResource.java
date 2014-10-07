package org.restlet.test.ext.apispark;

import org.restlet.ext.apispark.connector.configuration.AuthenticationConfiguration;
import org.restlet.ext.apispark.connector.configuration.Configuration;
import org.restlet.ext.apispark.connector.configuration.FirewallConfiguration;
import org.restlet.resource.ServerResource;

public class ConfigurationServerResource extends ServerResource implements
        ConfigurationResource {

    @Override
    public Configuration represent() {
        Configuration result = new Configuration();
        result.setApiEndpoint("");
        result.setConnectorHostLogin("");
        result.setConnectorHostSecret("".toCharArray());

        AuthenticationConfiguration authConf = new AuthenticationConfiguration();
        authConf.setEnabled(true);
        authConf.setEndpoint("http://localhost:8182/users");
        authConf.setUsername("owner");
        authConf.setPassword("owner");
        authConf.setRefreshRate(5);
        result.setAuthenticationConfiguration(authConf);

        FirewallConfiguration firewallConf = new FirewallConfiguration();
        firewallConf.setEnabled(false);
        result.setFirewallConfiguration(firewallConf);

        return result;
    }

}
