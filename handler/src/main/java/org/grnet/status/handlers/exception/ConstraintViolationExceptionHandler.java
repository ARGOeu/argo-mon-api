package org.grnet.status.handlers.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.grnet.status.dtos.InformativeResponse;
import org.jboss.logging.Logger;

import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionHandler implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = Logger.getLogger(ConstraintViolationExceptionHandler.class);

    @Override
    public Response toResponse(ConstraintViolationException e) {
        LOG.error("Constraint validation failed", e);

        var errors = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        var response = new InformativeResponse();
        response.code = Response.Status.BAD_REQUEST.getStatusCode();
        response.message = "Validation failed.";
        response.errors = errors;

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(response)
                .build();
    }
}
