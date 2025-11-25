package org.grnet.status.services.clients;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.grnet.status.dtos.argo.ArgoReportsResponse;
import org.grnet.status.dtos.argo.ArgoStatusGroupsResponse;
import org.grnet.status.dtos.tenant.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ArgoWebApiClient {

    @GET
    @Path("/api/v2/reports")
    ArgoReportsResponse fetchReports(@HeaderParam("x-api-key") String apiKey) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v3/status/{report}")
    ArgoStatusGroupsResponse fetchStatusGroups(
            @HeaderParam("x-api-key") String apiKey,
            @PathParam("report") String report
    ) throws WebApplicationException, ProcessingException;

    @POST
    @Path("/api/v2/admin/tenants")
    TenantWebApiCreateResponse createTenant(
            @HeaderParam("x-api-key") String apiKey,
            TenantRequestDto request
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/admin/tenants/{id}")
    TenantWebApiGetResponse getTenant(
            @HeaderParam("x-api-key") String apiKey,
            String id
    ) throws WebApplicationException, ProcessingException;

    @PUT
    @Path("/api/v2/admin/tenants/{id}")
    TenantWebApiUpdateResponse updateTenant(String id,
                                            @HeaderParam("x-api-key") String apiKey,
                                            TenantRequestDto request
    ) throws WebApplicationException, ProcessingException;

    @DELETE
    @Path("/api/v2/admin/tenants/{id}")
    TenantWebApiResponse deleteTenant(
            String id, @HeaderParam("x-api-key") String apiKey) throws WebApplicationException, ProcessingException;

}
