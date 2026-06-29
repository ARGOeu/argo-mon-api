package org.grnet.status.handlers.exception;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.grnet.status.dtos.InformativeResponse;
import org.jboss.logging.Logger;

@Provider
public class InternalServerExceptionHandler implements ExceptionMapper<InternalServerErrorException> {

    private static final Logger LOG = Logger.getLogger(InternalServerErrorException.class);

    @Override
    public Response toResponse(InternalServerErrorException e) {

        LOG.error("Internal Server Error", e);

        var response = new InformativeResponse();
        response.code = 500;
        response.message = "An internal server error occurred: "+e.getMessage();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(response)
                .build();
    }
}
