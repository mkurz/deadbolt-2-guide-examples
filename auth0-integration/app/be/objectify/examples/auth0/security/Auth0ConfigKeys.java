package be.objectify.examples.auth0.security;

/**
 * Configuration keys for the Auth0 integration.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class Auth0ConfigKeys {

    public static final String CLIENT_ID = "authentication.auth0.clientId";
    public static final String CLIENT_SECRET = "authentication.auth0.clientSecret";
    public static final String DOMAIN = "authentication.auth0.domain";
    public static final String REDIRECT_URI = "authentication.auth0.redirectURI";

    private Auth0ConfigKeys(){
        // no-op
    }
}
