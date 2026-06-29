package org.grnet.status.filters;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class PublicCorsFilter implements ContainerResponseFilter {

    @ConfigProperty(name = "quarkus.http.cors.origins")
    List<String> allowedOrigins;

    @ConfigProperty(name = "cors.public-paths")
    List<String> publicPaths;

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {

        String path = requestContext.getUriInfo().getPath();
        String origin = requestContext.getHeaderString("Origin");

        if (isPublicPath(path)) {
            responseContext.getHeaders()
                    .putSingle("Access-Control-Allow-Origin", "*");
        } else if (origin != null && isAllowedOrigin(origin)) {
            responseContext.getHeaders()
                    .putSingle("Access-Control-Allow-Origin", origin);
        }

        responseContext.getHeaders()
                .putSingle("Access-Control-Allow-Methods",
                        "GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD");
        responseContext.getHeaders()
                .putSingle("Access-Control-Allow-Headers",
                        "Origin, Content-Type, Accept, Authorization");

        responseContext.getHeaders()
                .putSingle("Access-Control-Max-Age", "86400");
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream()
                .anyMatch(path::startsWith);
    }

    private boolean isAllowedOrigin(String origin) {
        return allowedOrigins.stream().anyMatch(allowedOrigin -> {

            // Treat values enclosed in / as regexes
            if (allowedOrigin.startsWith("/") && allowedOrigin.endsWith("/")) {
                String regex = allowedOrigin.substring(1, allowedOrigin.length() - 1);
                return origin.matches(regex);
            }

            String normalizedAllowed = removeTrailingSlash(allowedOrigin);
            String normalizedOrigin = removeTrailingSlash(origin);

            return normalizedAllowed.equalsIgnoreCase(normalizedOrigin);
        });
    }

    private String removeTrailingSlash(String value) {
        if (value == null) {
            return null;
        }
        return value.endsWith("/")
                ? value.substring(0, value.length() - 1)
                : value;
    }
}