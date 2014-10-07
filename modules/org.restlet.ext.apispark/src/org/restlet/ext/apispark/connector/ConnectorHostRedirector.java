package org.restlet.ext.apispark.connector;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Redirector;

public class ConnectorHostRedirector extends Redirector {

    public ConnectorHostRedirector(Context context, String targetPattern,
            int mode) {
        super(context, targetPattern, mode);
    }
    
    @Override
    public void handle(Request request, Response response) {
        System.out.println();
        super.handle(request, response);
    }
}
