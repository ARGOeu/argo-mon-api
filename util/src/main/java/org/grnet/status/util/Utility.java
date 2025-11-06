package org.grnet.status.util;

import io.quarkus.oidc.TokenIntrospection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class Utility {

    /**
     * Injection point for the Token Introspection
     */
    @Inject
    TokenIntrospection tokenIntrospection;

    @ConfigProperty(name = "api.oidc.user.unique.id")
    String key;

    public String getUserUniqueIdentifier() {

        String id;

        try {
            id = tokenIntrospection.getJsonObject().getString(key);
        } catch (Exception e) {

            String message = String.format("The User's unique identifier {%s} is missing from the access token.", key);
            throw new BadRequestException(message);
        }

        return id;
    }

    public String getUsername() {
        try {
            return tokenIntrospection.getJsonObject().getString("preferred_username");
        } catch (Exception e) {
            throw new BadRequestException("Missing 'preferred_username' in access token.");
        }
    }

    public String getUserEmail() {
        return tokenIntrospection.getJsonObject().getString("email", null);
    }

    public String getUserName() {
        return tokenIntrospection.getJsonObject().getString("given_name", null);
    }

    public String getUserSurname() {
        return tokenIntrospection.getJsonObject().getString("family_name", null);
    }
}
