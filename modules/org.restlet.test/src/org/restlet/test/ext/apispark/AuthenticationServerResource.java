package org.restlet.test.ext.apispark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.ext.apispark.connector.Credentials;
import org.restlet.ext.apispark.connector.resource.AuthenticationResource;
import org.restlet.resource.ServerResource;

public class AuthenticationServerResource extends ServerResource implements
        AuthenticationResource {

    private static Map<Credentials, List<String>> users;

    static {
        users = new HashMap<Credentials, List<String>>();

        List<String> userRoles = new ArrayList<String>();
        userRoles.add("user");

        List<String> devRoles = new ArrayList<String>();
        devRoles.add("user");
        devRoles.add("dev");

        List<String> ownerRoles = new ArrayList<String>();
        ownerRoles.addAll(devRoles);
        ownerRoles.add("owner");

        List<String> guruRoles = new ArrayList<String>();
        guruRoles.addAll(ownerRoles);
        guruRoles.add("guru");

        users.put(new Credentials("guru", "guru".toCharArray()), guruRoles);

        users.put(new Credentials("owner", "owner".toCharArray()), ownerRoles);

        users.put(new Credentials("dev", "dev".toCharArray()), devRoles);

        users.put(new Credentials("user", "user".toCharArray()), userRoles);
    }

    @Override
    public List<String> represent(String username, char[] secret) {
        Credentials credentials = new Credentials(username, secret);
        if (!users.containsKey(credentials)) {
            return null;
        }
        return users.get(credentials);
    }

}
