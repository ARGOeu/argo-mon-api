package org.grnet.status.services.clients;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.grnet.status.dtos.argo.ArgoReportsResponse;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ArgoWebApiClient {

    @GET
    @Path("/api/v2/reports")
    ArgoReportsResponse fetchReports(@HeaderParam("x-api-key") String apiKey) throws WebApplicationException, ProcessingException;
}
