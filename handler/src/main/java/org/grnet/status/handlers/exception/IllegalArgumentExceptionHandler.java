package org.grnet.status.handlers.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.grnet.status.dtos.InformativeResponse;
import org.jboss.logging.Logger;

@Provider
public class IllegalArgumentExceptionHandler implements ExceptionMapper<IllegalArgumentException> {

    private static final Logger LOG = Logger.getLogger(IllegalArgumentException.class);

    @Override
    public Response toResponse(IllegalArgumentException e) {

        LOG.error("Illegal Argument", e);

        var response = new InformativeResponse();
        response.code = 400;
        response.message = e.getMessage();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(response)
                .build();
    }
}
