package be.objectify.examples.auth0.controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import be.objectify.examples.auth0.security.AuthSupport;
import be.objectify.examples.auth0.views.html.index;
import com.google.inject.Inject;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

/**
 * A very simple controller whose sole action requires a subject to be
 * present, i.e. the user be authenticated.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class Application extends Controller {

    private final AuthSupport authSupport;

    @Inject
    public Application(final AuthSupport authSupport) {

        this.authSupport = authSupport;
    }

    @SubjectPresent
    public F.Promise<Result> index() {
        return F.Promise.promise(() -> index.render("Protected content",
                                                    authSupport.currentUser(ctx())))
                        .map(Results::ok);
    }
}
