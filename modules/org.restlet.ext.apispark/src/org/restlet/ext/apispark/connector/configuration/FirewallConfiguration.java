package org.restlet.ext.apispark.connector.configuration;

import java.util.List;

public class FirewallConfiguration {

    private List<RateLimitation> rateLimitations;

    private boolean enabled;

    public List<RateLimitation> getRateLimitations() {
        return rateLimitations;
    }

    public void setRateLimitations(List<RateLimitation> rateLimitations) {
        this.rateLimitations = rateLimitations;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
