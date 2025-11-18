package org.grnet.status.handlers.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.grnet.status.dtos.InformativeResponse;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.logging.Logger;

@Provider
public class DatabaseConstraintViolationHandler implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = Logger.getLogger(DatabaseConstraintViolationHandler.class);

    @Override
    public Response toResponse(ConstraintViolationException e) {

        LOG.error("Database constraint violation occurred", e);

        var response = new InformativeResponse();
        response.code = Response.Status.CONFLICT.getStatusCode();
        response.message = e.getMessage();

        return Response.status(Response.Status.CONFLICT)
                .entity(response)
                .build();
    }
}
