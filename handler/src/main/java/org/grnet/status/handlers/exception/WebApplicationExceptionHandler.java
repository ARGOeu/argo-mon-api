package org.grnet.status.handlers.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.grnet.status.dtos.InformativeResponse;
import org.jboss.logging.Logger;

@Provider
public class WebApplicationExceptionHandler implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = Logger.getLogger(WebApplicationExceptionHandler.class);

    @Override
    public Response toResponse(WebApplicationException e) {

        LOG.error("WebApplicationException Error ", e);

        int statusCode = e.getResponse() != null ? e.getResponse().getStatus() : 502;

        var response = new InformativeResponse();
        response.code = statusCode;
        response.message = "External service responded with an error (" + statusCode + ").";

        return Response.status(statusCode).entity(response).build();
    }
}
