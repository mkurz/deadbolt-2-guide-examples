package be.objectify.examples.auth0.controllers;

import javax.inject.Inject;
import be.objectify.examples.auth0.models.User;
import be.objectify.examples.auth0.security.Auth0ConfigKeys;
import be.objectify.examples.auth0.security.AuthSupport;
import be.objectify.examples.auth0.views.html.security.denied;
import be.objectify.examples.auth0.views.html.security.login;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Configuration;
import play.cache.CacheApi;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

/**
 * Interactions with Auth0.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class AuthController extends Controller {

    private final CacheApi cache;
    private final AuthSupport authSupport;

    private final String clientId;
    private final String clientSecret;
    private final String domain;
    private final String redirectUri;

    @Inject
    public AuthController(final AuthSupport authSupport,
                          final CacheApi cache,
                          final Configuration config) {
        this.cache = cache;
        this.authSupport = authSupport;

        this.clientId = config.getString(Auth0ConfigKeys.CLIENT_ID);
        this.clientSecret = config.getString(Auth0ConfigKeys.CLIENT_SECRET);
        this.domain = config.getString(Auth0ConfigKeys.DOMAIN);
        this.redirectUri = config.getString(Auth0ConfigKeys.REDIRECT_URI);
    }

    /**
     * Render the log-in view containing the Auth0 form
     */
    public F.Promise<Result> logIn() {
        return F.Promise.promise(() -> login.render(this.clientId,
                                                    this.domain,
                                                    this.redirectUri))
                        .map(Results::ok);
    }

    /**
     * Receives a callback from Auth0 with an authentication code.
     *
     * @param maybeCode the authentication code, used to obtain a user token
     * @param maybeState some examples from Auth0 give the need for a state value - it's not used here
     * @return redirects to the index
     */
    public F.Promise<Result> callback(final F.Option<String> maybeCode,
                                      final F.Option<String> maybeState) {
        return maybeCode.map(code -> getToken(code).flatMap(token -> getUser(token))
                                                   .map(userAndToken -> {
                                                       // userAndToken._1 is the user
                                                       // userAndToken._2 is the token
                                                       cache.set(authSupport.cacheKey(userAndToken._2._1),
                                                                 userAndToken._1,
                                                                 60*15); // cache for 15 minutes
                                                       session("idToken",
                                                               userAndToken._2._1);
                                                       session("accessToken",
                                                               userAndToken._2._2);
                                                       return redirect(routes.Application.index());
                                                   }))
                        .getOrElse(F.Promise.pure(badRequest("No parameters supplied")));
    }

    /**
     * Get the user token from Auth0, using the code we received earlier.
     *
     * @param code the Auth0 code
     * @return a tuple containing the id and access tokens
     */
    private F.Promise<F.Tuple<String, String>> getToken(final String code) {
        final ObjectNode root = Json.newObject();
        root.put("client_id",
                 this.clientId);
        root.put("client_secret",
                 this.clientSecret);
        root.put("redirect_uri",
                 this.redirectUri);
        root.put("code",
                 code);
        root.put("grant_type",
                 "authorization_code");
        return WS.url(String.format("https://%s/oauth/token",
                                    this.domain))
                 .setHeader(Http.HeaderNames.ACCEPT,
                            Http.MimeTypes.JSON)
                 .post(root)
                 .map(WSResponse::asJson)
                 .map(json -> new F.Tuple<>(json.get("id_token").asText(),
                                            json.get("access_token").asText()));
    }

    /**
     *
     * @param token
     * @return
     */
    private F.Promise<F.Tuple<User, F.Tuple<String, String>>> getUser(final F.Tuple<String, String> token) {
        return WS.url(String.format("https://%s/userinfo",
                                    this.domain))
                 .setQueryParameter("access_token",
                                    token._2)
                 .get()
                 .map(WSResponse::asJson)
                 .map(json -> new User(json.get("user_id").asText(),
                                       json.get("name").asText(),
                                       json.get("picture").asText()))
                 .map(localUser -> new F.Tuple<>(localUser,
                                                 token));
    }

    public F.Promise<Result> logOut() {
        return F.Promise.promise(() ->
                                 {
                                     final Http.Session session = session();
                                     final String idToken = session.remove("idToken");
                                     session.remove("accessToken");
                                     cache.remove(authSupport.cacheKey(idToken));
                                     return "ignoreThisValue";
                                 })
                        .map(id -> redirect(routes.AuthController.logIn()));
    }

    public F.Promise<Result> denied() {
        final Http.Context ctx = ctx();
        return F.Promise.promise(() -> authSupport.currentUser(ctx))
                        .map(maybeUser -> maybeUser.map(user -> (Result) forbidden(denied.render(maybeUser)))
                                                   .orElseGet(() -> redirect(routes.AuthController.logIn())));
    }
}