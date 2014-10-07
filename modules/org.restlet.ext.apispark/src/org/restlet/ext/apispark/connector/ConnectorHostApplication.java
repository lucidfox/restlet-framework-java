package org.restlet.ext.apispark.connector;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Redirector;

public class ConnectorHostApplication extends Application {

    private String path;

    private String username;

    private char[] password;

    public ConnectorHostApplication(String path, String username,
            char[] password) {
        this.path = path;
        this.username = username;
        this.password = password;
    }

    @Override
    public Restlet createInboundRoot() {
        Redirector redirector = new Redirector(getContext(), "",
                Redirector.MODE_SERVER_OUTBOUND);
        ConnectorHostFilter connectorHostFilter = new ConnectorHostFilter(
                getContext(), path, username, password, redirector);
        redirector.setTargetTemplate(connectorHostFilter.getConfiguration()
                .getApiEndpoint() + "{rr}");
        return connectorHostFilter;
    }
}
