package org.restlet.test.ext.apispark;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class MockApiServerResource extends ServerResource {

    @Get
    public String represent() {
        return "GET OK!";
    }

    @Post
    public String handlePost(String payload) {
        return "POST OK!";
    }
}
