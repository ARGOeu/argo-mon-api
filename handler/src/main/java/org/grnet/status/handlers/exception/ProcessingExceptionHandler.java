package org.grnet.status.handlers.exception;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.grnet.status.dtos.InformativeResponse;
import org.jboss.logging.Logger;

@Provider
public class ProcessingExceptionHandler implements ExceptionMapper<ProcessingException> {

    private static final Logger LOG = Logger.getLogger(ProcessingExceptionHandler.class);

    @Override
    public Response toResponse(ProcessingException e) {

        LOG.error("ProcessingException Error" + e);

        var response = new InformativeResponse();
        response.code = 502;
        response.message = "Unable to connect to the external service.";

        return Response.status(Response.Status.BAD_GATEWAY)
                .entity(response)
                .build();
    }
}
