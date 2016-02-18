package be.objectify.examples.auth0.security;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.examples.auth0.models.User;
import be.objectify.examples.auth0.views.html.security.denied;
import be.objectify.examples.auth0.views.html.security.login;
import play.Configuration;
import play.cache.CacheApi;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

/**
 *
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class MyDeadboltHandler implements DeadboltHandler {

    private final AuthSupport authSupport;
    private final CacheApi cache;

    private final String clientId;
    private final String domain;
    private final String redirectUri;

    @Inject
    public MyDeadboltHandler(final AuthSupport authSupport,
                             final CacheApi cache,
                             final Configuration config) {
        this.authSupport = authSupport;
        this.cache = cache;

        this.clientId = config.getString(Auth0ConfigKeys.CLIENT_ID);
        this.domain = config.getString(Auth0ConfigKeys.DOMAIN);
        this.redirectUri = config.getString(Auth0ConfigKeys.REDIRECT_URI);
    }

    @Override
    public F.Promise<Optional<Result>> beforeAuthCheck(final Http.Context context) {
        return F.Promise.pure(Optional.empty());
    }

    @Override
    public F.Promise<Optional<Subject>> getSubject(final Http.Context context) {
        return F.Promise.promise(() -> Optional.ofNullable(cache.get(authSupport.cacheKey(context.session().get("idToken")))));
    }

    @Override
    public F.Promise<Result> onAuthFailure(final Http.Context context,
                                           final String s) {
        return getSubject(context)
                .map(maybeSubject ->
                             maybeSubject.map(subject -> Optional.of((User)subject))
                                         .map(user -> new F.Tuple<>(true,
                                                                    denied.render(user)))
                                         .orElseGet(() -> new F.Tuple<>(false,
                                                                        login.render(clientId,
                                                                                     domain,
                                                                                     redirectUri))))
                .map(subjectPresentAndContent -> subjectPresentAndContent._1
                                                 ? Results.forbidden(subjectPresentAndContent._2)
                                                 : Results.unauthorized(subjectPresentAndContent._2));
    }

    @Override
    public F.Promise<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context) {
        return F.Promise.pure(Optional.empty());
    }

    @Override
    public String handlerName() {
        return ConfigKeys.DEFAULT_HANDLER_KEY;
    }
}
