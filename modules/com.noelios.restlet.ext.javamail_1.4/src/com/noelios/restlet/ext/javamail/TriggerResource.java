/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.javamail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.util.Resolver;
import org.restlet.util.Template;
import org.w3c.dom.Node;

/**
 * Resource that handles requests to target resources according to parameters
 * located in mails.
 * 
 * @author Jerome Louvel
 */
public class TriggerResource extends Resource {

    public static final String ATTRIBUTE_MAILBOX_CHALLENGE_SCHEME = "com.noelios.restlet.ext.javamail.mailbox.authentication.scheme";

    public static final String ATTRIBUTE_MAILBOX_LOGIN = "com.noelios.restlet.ext.javamail.mailbox.login";

    public static final String ATTRIBUTE_MAILBOX_PASSWORD = "com.noelios.restlet.ext.javamail.mailbox.password";

    public static final String ATTRIBUTE_MAILBOX_URI = "com.noelios.restlet.ext.javamail.mailbox.uri";

    private ChallengeScheme mailboxChallengeScheme;

    private String mailboxLogin;

    private String mailboxPassword;

    /** The mailbox URI template. */
    private String mailboxUri;

    /**
     * The mail URI template, optionnaly relative to the mailbox URI. The
     * template should contain a variable "{mailId}" for the insertion of the
     * mail identifier in the proper location. The default value is "/{mailId}".
     */
    private String mailUriTemplate;

    /** Resolver that resolves a name into a value. */
    private MailResolver resolver;

    /** Indicates if the target entity should be provided to the target resource. */
    private boolean targetEntityEnabled;

    /** The method to invoke on the target resource. */
    private Method targetMethod;

    /** The target URI template. */
    private String targetUri;

    /**
     * Constructor.
     * 
     * @param context
     *                The parent context.
     * @param request
     *                The request to handle.
     * @param response
     *                The response to return.
     */
    public TriggerResource(Context context, Request request, Response response) {
        super(context, request, response);
        setModifiable(true);
        this.mailboxChallengeScheme = null;
        this.mailboxLogin = null;
        this.mailboxPassword = null;
        this.mailboxUri = null;
        this.mailUriTemplate = "/{mailId}";
        this.resolver = null;
    }

    /**
     * Handles POST requests. It retrieves a list of mails and generate requests
     * to target resources.
     */
    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {

        // 1 - Get list of identifiers for the mails in the inbox
        List<String> mailIdentifiers = getMailIdentifiers();

        // 2 - Process the list of mails
        List<String> mailsSuccessful = new ArrayList<String>();
        Map<String, String> mailsUnsuccessful = new HashMap<String, String>();
        Representation mail;
        for (String mailIdentifier : mailIdentifiers) {
            try {
                mail = getMail(mailIdentifier);
                if (mail != null) {
                    resolver = getResolver(mailIdentifier, mail);
                    callTarget(resolver);
                    deleteMail(mailIdentifier);
                    mailsSuccessful.add(mailIdentifier);
                }
            } catch (ResourceException e) {
                mailsUnsuccessful.put(mailIdentifier, e.getMessage());
            }
        }

        // 3 - Set response for timer client
        getResponse().setEntity(
                getResponseRepresentation(mailsSuccessful, mailsUnsuccessful));
        getResponse().setStatus(
                getResponseStatus(mailsSuccessful, mailsUnsuccessful));
    }

    /**
     * Requests the target resource.
     * 
     * @param resolver
     *                The data model that provides parameters value.
     * @throws ResourceException
     */
    protected void callTarget(Resolver<String> resolver)
            throws ResourceException {
        // A - Build the request for the target resource
        Method method = getTargetMethod(resolver);

        Reference targetRef = getTargetRef(resolver);

        Request request = new Request(method, targetRef);
        ChallengeResponse challengeResponse = getTargetChallengeResponse(resolver);
        if (challengeResponse != null) {
            request.setChallengeResponse(challengeResponse);
        }

        if (isTargetEntityEnabled()) {
            request.setEntity(getTargetEntity(resolver));
        }

        // B - Call the target resource
        Response response = getContext().getClientDispatcher().handle(request);

        if (!response.getStatus().isSuccess()) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Call to the target resource didn't succeed");
        }
    }

    /**
     * Deletes a mail after it has been processed.
     * 
     * @param mailIdentifier
     *                The identifier of the mail
     * @throws ResourceException
     */
    protected void deleteMail(String mailIdentifier) throws ResourceException {
        // A - Build the mail URI
        Reference mailRef = getMailRef(mailIdentifier);

        if (mailRef.isRelative()) {
            mailRef.setBaseRef(getMailboxUri());
            mailRef = mailRef.getTargetRef();
        }

        // B - Delete the mail
        Request request = new Request(Method.DELETE, mailRef);
        if (getMailboxChallengeScheme() != null) {
            ChallengeResponse challengeResponse = new ChallengeResponse(
                    getMailboxChallengeScheme(), getMailboxLogin(),
                    getMailboxPassword());
            request.setChallengeResponse(challengeResponse);
        }
        Response response = getContext().getClientDispatcher().handle(request);

        if (response.getStatus().isError()) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Unable to delete the mail from the mailbox");
        }
    }

    /**
     * Get the mail representation according to its identifier.
     * 
     * @param identifier
     *                the mail identifier.
     * @throws ResourceException
     */
    protected Representation getMail(String identifier)
            throws ResourceException {
        // A - Build the mail URI
        Reference mailRef = getMailRef(identifier);

        // B - Get the mail
        Request request = new Request(Method.GET, mailRef);
        if (getMailboxChallengeScheme() != null) {
            ChallengeResponse challengeResponse = new ChallengeResponse(
                    getMailboxChallengeScheme(), getMailboxLogin(),
                    getMailboxPassword());
            request.setChallengeResponse(challengeResponse);
        }
        Response response = getContext().getClientDispatcher().handle(request);

        if (!response.getStatus().isSuccess()) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Unable to get the mail from the mailbox");
        }

        return new DomRepresentation(response.getEntity());
    }

    /**
     * Returns the scheme of the mailbox challenge.
     * 
     * @return The scheme of the mailbox challenge.
     */
    public ChallengeScheme getMailboxChallengeScheme() {
        if (this.mailboxChallengeScheme == null) {
            this.mailboxChallengeScheme = (ChallengeScheme) getContext()
                    .getAttributes().get(ATTRIBUTE_MAILBOX_CHALLENGE_SCHEME);
        }

        return this.mailboxChallengeScheme;
    }

    /**
     * Returns the login for the mailbox.
     * 
     * @return The login for the mailbox.
     */
    public String getMailboxLogin() {
        if (this.mailboxLogin == null) {
            this.mailboxLogin = (String) getContext().getAttributes().get(
                    ATTRIBUTE_MAILBOX_LOGIN);
        }

        return this.mailboxLogin;
    }

    /**
     * Returns the password for the mailbox.
     * 
     * @return The password for the mailbox.
     */
    public String getMailboxPassword() {
        if (this.mailboxPassword == null) {
            this.mailboxPassword = (String) getContext().getAttributes().get(
                    ATTRIBUTE_MAILBOX_PASSWORD);
        }

        return this.mailboxPassword;
    }

    /**
     * Returns the URI of the mailbox.
     * 
     * @return The URI of the mailbox.
     */
    public String getMailboxUri() {
        if (this.mailboxUri == null) {
            this.mailboxUri = (String) getContext().getAttributes().get(
                    ATTRIBUTE_MAILBOX_URI);
        }

        return this.mailboxUri;
    }

    /**
     * Returns the list of identifiers for the mails in the inbox
     * 
     * @return The list of identifiers.
     * @throws ResourceException
     */
    protected List<String> getMailIdentifiers() throws ResourceException {
        List<String> result = new ArrayList<String>();

        // 1 - Get to mailbox content
        Request request = new Request(Method.GET, getMailboxUri());
        if (getMailboxChallengeScheme() != null) {
            ChallengeResponse challengeResponse = new ChallengeResponse(
                    getMailboxChallengeScheme(), getMailboxLogin(),
                    getMailboxPassword());
            request.setChallengeResponse(challengeResponse);
        }
        Response response = getContext().getClientDispatcher().handle(request);

        if (!response.getStatus().isSuccess()) {
            throw new ResourceException(response.getStatus(),
                    "Cannot get the mail iddentifiers.");
        }

        // 2 - Parse the list of mails
        if (response.isEntityAvailable()) {
            DomRepresentation rep = response.getEntityAsDom();
            for (Node node : rep.getNodes("/emails/email/@href")) {
                String href = node.getNodeValue();
                if (href.startsWith("/")) {
                    result.add(href.substring(1));
                } else {
                    result.add(href);
                }
            }
        }

        return result;
    }

    /**
     * Returns the reference of a mail according to its identifier.
     * 
     * @param identifier
     *                The identifier of a mail.
     * @return The URI of the mail.
     * @throws ResourceException
     */
    protected Reference getMailRef(String identifier) throws ResourceException {
        Template mailTemplate = new Template(getMailUriTemplate());
        Reference result = new Reference(mailTemplate.format(new MailResolver(
                identifier)));

        if (result.isRelative()) {
            result.setBaseRef(getMailboxUri());
            result = result.getTargetRef();
        }

        return result;
    }

    /**
     * Returns the template of the mail's URI.
     * 
     * @return the template of the mail's URI.
     */
    public String getMailUriTemplate() {
        return mailUriTemplate;
    }

    /**
     * Returns the resolver based on a mail.
     * 
     * @return The resolver.
     */
    public MailResolver getResolver() {
        return resolver;
    }

    /**
     * Returns a new resolver based on a mail.
     * 
     * @param mailIdentifier
     *                Identifier of the mail.
     * @param email
     *                The mail.
     * @return A resolver.
     */
    protected MailResolver getResolver(String mailIdentifier,
            Representation email) {
        return new MailResolver(mailIdentifier, email);
    }

    /**
     * Returns the response's representation according to the list of
     * successfull and unsuccessfull mails.
     * 
     * @param mailsSuccessful
     *                The list of successfull mails.
     * @param mailsUnsuccessful
     *                The list of successfull mails and related error message.
     * @return The response's representation.
     */
    protected Representation getResponseRepresentation(
            List<String> mailsSuccessful, Map<String, String> mailsUnsuccessful) {
        Representation representation = null;

        return representation;
    }

    /**
     * Returns the response's status according to the list of successfull and
     * unsuccessfull mails.
     * 
     * @param mailsSuccessful
     *                The list of successfull mails.
     * @param mailsUnsuccessful
     *                The list of successfull mails and related error message.
     * @return The response's status.
     */
    protected Status getResponseStatus(List<String> mailsSuccessful,
            Map<String, String> mailsUnsuccessful) {
        Status status = null;
        if (mailsUnsuccessful.size() > 0) {
            status = Status.CLIENT_ERROR_NOT_FOUND;
        } else {
            status = Status.SUCCESS_OK;
        }
        return status;
    }

    /**
     * Returns the authentication data sent by client to the target according to
     * the a list of properties. By default, this method returns checks the
     * variable "challengeScheme", "login", "password" in order to build the
     * ChallengeResponse object. It can be overriden.
     * 
     * @param resolver
     *                The resolver.
     * @return The target challengeResponse object.
     * @throws ResourceException
     */
    protected ChallengeResponse getTargetChallengeResponse(
            Resolver<String> resolver) throws ResourceException {
        ChallengeScheme challengeScheme = ChallengeScheme.valueOf(resolver
                .resolve("challengeScheme"));
        String login = resolver.resolve("login");
        String password = resolver.resolve("password");

        ChallengeResponse result = null;
        if (challengeScheme != null && login != null && password != null) {
            result = new ChallengeResponse(challengeScheme, login, password);
        }

        return result;
    }

    /**
     * Returns the entity sent to the target. By default, it sends the mail
     * message.
     * 
     * @param resolver
     *                the resolver.
     * @return The entity to be sent to the target.
     */
    protected Representation getTargetEntity(Resolver<String> resolver) {
        return new StringRepresentation(resolver.resolve("message"));
    }

    /**
     * Returns the default target method.
     * 
     * @return The default target method.
     */
    public Method getTargetMethod() {
        return targetMethod;
    }

    /**
     * Returns the target method according to a list of properties.
     * 
     * @param resolver
     *                The resolver.
     * @return The target method.
     */
    protected Method getTargetMethod(Resolver<String> resolver) {
        Method method = Method.valueOf(resolver.resolve("method"));
        if (method == null) {
            method = getTargetMethod();
        }

        return method;
    }

    /**
     * Returns the reference of the target according to the a list of
     * properties.
     * 
     * @param resolver
     *                The resolver.
     * @return The target reference.
     * @throws ResourceException
     */
    protected Reference getTargetRef(Resolver<String> resolver)
            throws ResourceException {
        Template targetTemplate = new Template(getTargetUri());
        Reference result = new Reference(targetTemplate.format(resolver));

        if (result.isRelative()) {
            result.setBaseRef(getMailboxUri());
            result = result.getTargetRef();
        }

        return result;
    }

    /**
     * Returns the target URI template.
     * 
     * @return The template that represents a target URI.
     */
    public String getTargetUri() {
        return targetUri;
    }

    /**
     * Indicate whether or not the target supports entity in the request.
     * 
     * @return True if the target supports entity in the request, false,
     *         otherwise.
     */
    public boolean isTargetEntityEnabled() {
        return targetEntityEnabled;
    }

    /**
     * Sets the scheme of the mailbox challenge.
     * 
     * @param mailboxChallengeScheme
     *                The scheme of the mailbox challenge.
     */
    public void setMailboxChallengeScheme(ChallengeScheme mailboxChallengeScheme) {
        this.mailboxChallengeScheme = mailboxChallengeScheme;
    }

    /**
     * Sets the login for the mailbox access.
     * 
     * @param mailboxLogin
     *                The login for the mailbox access.
     */
    public void setMailboxLogin(String mailboxLogin) {
        this.mailboxLogin = mailboxLogin;
    }

    /**
     * Sets the password for the mailbox access.
     * 
     * @param mailboxPassword
     *                The password for the mailbox access.
     */
    public void setMailboxPassword(String mailboxPassword) {
        this.mailboxPassword = mailboxPassword;
    }

    /**
     * Sets the URI of the mailbox.
     * 
     * @param mailboxUri
     *                the URI of the mailbox.
     */
    public void setMailboxUri(String mailboxUri) {
        this.mailboxUri = mailboxUri;
    }

    /**
     * Sets the URI template for the target.
     * 
     * @param mailUriTemplate
     *                the URI template for the target.
     */
    public void setMailUriTemplate(String mailUriTemplate) {
        this.mailUriTemplate = mailUriTemplate;
    }

    /**
     * Indicate whether or not the target supports entity in the request.
     * 
     * @param targetEntityEnabled
     *                True if the target supports entity in the request, false,
     *                otherwise.
     */
    public void setTargetEntityEnabled(boolean targetEntityEnabled) {
        this.targetEntityEnabled = targetEntityEnabled;
    }

    /**
     * Sets the default target method.
     * 
     * @param targetMethod
     *                The default target method.
     */
    public void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    /**
     * Sets the target URI template.
     * 
     * @param targetUri
     *                The target URI template.
     */
    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }

}
