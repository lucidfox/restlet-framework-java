package org.restlet.test.ext.apispark;

import org.restlet.ext.apispark.connector.configuration.Configuration;
import org.restlet.resource.Get;

public interface ConfigurationResource {

    @Get
    public Configuration represent();
}
