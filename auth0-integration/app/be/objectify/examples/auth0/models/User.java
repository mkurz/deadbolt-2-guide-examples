package be.objectify.examples.auth0.models;

import java.util.Collections;
import java.util.List;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;

/**
 * A very simple implementation of {@link Subject}.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class User implements Subject {

    private final String userId;
    private final String name;
    private final String avatarUrl;

    public User(final String userId,
                final String name,
                final String avatarUrl) {
        this.userId = userId;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    @Override
    public List<? extends Role> getRoles() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends Permission> getPermissions() {
        return Collections.emptyList();
    }

    @Override
    public String getIdentifier() {
        return userId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }
}
