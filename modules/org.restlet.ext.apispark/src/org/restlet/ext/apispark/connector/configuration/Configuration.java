package org.restlet.ext.apispark.connector.configuration;

public class Configuration {

    private AuthenticationConfiguration authenticationConfiguration;

    private AuthorizationConfiguration authorizationConfiguration;

    private FirewallConfiguration firewallConfiguration;
    
    private String apiEndpoint;
    
    private String connectorHostLogin;
    
    private char[] connectorHostSecret;

    public AuthenticationConfiguration getAuthenticationConfiguration() {
        return authenticationConfiguration;
    }

    public void setAuthenticationConfiguration(
            AuthenticationConfiguration authenticationConfiguration) {
        this.authenticationConfiguration = authenticationConfiguration;
    }

    public AuthorizationConfiguration getAuthorizationConfiguration() {
        return authorizationConfiguration;
    }

    public void setAuthorizationConfiguration(
            AuthorizationConfiguration authorizationConfiguration) {
        this.authorizationConfiguration = authorizationConfiguration;
    }

    public FirewallConfiguration getFirewallConfiguration() {
        return firewallConfiguration;
    }

    public void setFirewallConfiguration(FirewallConfiguration firewallConfiguration) {
        this.firewallConfiguration = firewallConfiguration;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getConnectorHostLogin() {
        return connectorHostLogin;
    }

    public void setConnectorHostLogin(String connectorHostLogin) {
        this.connectorHostLogin = connectorHostLogin;
    }

    public char[] getConnectorHostSecret() {
        return connectorHostSecret;
    }

    public void setConnectorHostSecret(char[] connectorHostSecret) {
        this.connectorHostSecret = connectorHostSecret;
    }
}
