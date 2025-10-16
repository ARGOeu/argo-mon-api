package org.grnet.status.handlers.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.exceptions.BadRequestException;
import org.jboss.logging.Logger;

@Provider
public class BadRequestExceptionHandler implements ExceptionMapper<BadRequestException> {

    private static final Logger LOG = Logger.getLogger(BadRequestExceptionHandler.class);

    @Override
    public Response toResponse(BadRequestException e) {

        LOG.error("Bad Request Error", e);

        var response = new InformativeResponse();
        response.code = 400;

        // Prefer error details if they exist, otherwise fallback to main message
        if (e.getErrors() != null && !e.getErrors().isEmpty()) {
            response.message = String.join("; ", e.getErrors());
        } else {
            response.message = e.getMessage();
        }

        response.errors = null;

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(response)
                .build();
    }
}
