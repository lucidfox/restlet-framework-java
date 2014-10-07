package org.restlet.test.ext.apispark;

import java.util.List;

import org.restlet.resource.Get;

public interface AuthenticationResource {

    @Get
    public List<String> represent(String username, char[] secret);
}
