package be.objectify.examples.auth0.security;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;

/**
 * Supplies either a default or a named {@link DeadboltHandler} instance.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class MyHandlerCache implements HandlerCache {

    private final MyDeadboltHandler defaultHandler;

    private final Map<String, DeadboltHandler> handlers = new HashMap<>();

    @Inject
    public MyHandlerCache(final MyDeadboltHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
        handlers.put(defaultHandler.handlerName(),
                     defaultHandler);
    }

    @Override
    public DeadboltHandler apply(String handlerKey) {
        // This simple example only uses one DeadboltHandler, so we don't really need to support keyed handlers
        return handlers.get(handlerKey);
    }

    @Override
    public DeadboltHandler get() {
        return defaultHandler;
    }
}
