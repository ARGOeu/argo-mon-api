package org.grnet.status.api.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class RequestFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(RequestFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        var pathParams = requestContext.getUriInfo().getPathParameters()
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));

        LOG.info("Path params: " + pathParams);
    }
}