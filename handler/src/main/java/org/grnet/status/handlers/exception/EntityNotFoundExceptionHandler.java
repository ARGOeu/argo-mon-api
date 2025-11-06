package org.grnet.status.handlers.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.grnet.status.dtos.InformativeResponse;
import org.jboss.logging.Logger;

@Provider
public class EntityNotFoundExceptionHandler implements ExceptionMapper<EntityNotFoundException> {

    private static final Logger LOG = Logger.getLogger(EntityNotFoundException.class);

    @Override
    public Response toResponse(EntityNotFoundException e) {

        LOG.error("Entity Not Found Error", e);

        var response = new InformativeResponse();
        response.code = 409;
        response.message = e.getMessage();

        return Response.status(409).entity(response).build();
    }
}
