package be.objectify.examples.auth0.security;

import be.objectify.deadbolt.java.cache.HandlerCache;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

/**
 * Bindings for Deadbolt integration.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CustomDeadboltHook extends Module {

    @Override
    public Seq<Binding<?>> bindings(final Environment environment,
                                    final Configuration configuration) {
        return this.seq(bind(MyDeadboltHandler.class).toSelf().eagerly(),
                        bind(HandlerCache.class).to(MyHandlerCache.class).eagerly());
    }
}