package org.grnet.status.api.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.endpoint.scanner.runtime.ParamRef;
import org.grnet.endpoint.scanner.runtime.ParamType;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;
import org.grnet.status.api.resolvers.CheckDateFormat;
import org.grnet.status.constraints.NotFoundEntity;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.downtime.DailyDowntimeResponse;
import org.grnet.status.dtos.tenant.status.TenantStatusDto;
import org.grnet.status.dtos.tenant.status.TenantStatusFullResponse;
import org.grnet.status.dtos.topology.IsExternalFeedTopologyResponse;
import org.grnet.status.entities.Downtime;
import org.grnet.status.enums.resources.InvitationResource;
import org.grnet.status.enums.resources.TenantResource;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.DowntimeService;
import org.grnet.status.services.TenantService;


@Path("/v1/automation")
@Authenticated
@SecurityScheme(securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
public class AutomationEndpoint {

    @Inject
    TenantService tenantService;
    @Inject
    DowntimeService downtimeService;

    @Tag(name = "Automation")
    @Operation(
            summary = "Update Tenant's status By Id .",
            description = "Returns a specific tenant's status.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding tenant's status.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantStatusFullResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Entity Not Found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @PATCH
    @Path("/tenants/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)

    @SecuredEndpoint(
            params = {
                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = TenantResource.class
                    )
            }
    )
    public Response updateStatus(
            @Parameter(
                    description = "The ID of the tenant to retrieve status.",
                    required = true,
                    example = "42c1152d-e23c-4a19-b51a-b27f1eb7f37f",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ")
            String id, TenantStatusDto request) {

        var status = tenantService.updateTenantAutoJobs(id, request);
        return Response.ok().entity(status).build();
    }

    @Tag(name = "Automation")
    @Operation(
            summary = "Get Tenant's status By Id .",
            description = "Returns a specific tenant's status.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding tenant's status.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantStatusFullResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Entity Not Found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))

    @GET
    @Path("/tenants/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @SecuredEndpoint(
            params = {


                    @ParamRef(
                            param = "id",
                            type = ParamType.PATH,
                            referTo = TenantResource.class
                    )
            }
    )
    public Response getTenantStatus(
            @Parameter(
                    description = "The ID of the tenant to update status.",
                    required = true,
                    example = "42c1152d-e23c-4a19-b51a-b27f1eb7f37f",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ")
            String id) {

        var status = tenantService.getTenantStatus(id);

        return Response.ok().entity(status).build();
    }


    @Tag(name = "Automation")
    @Operation(
            summary = "Fetch a daily downtime for a tenant.",
            description = "Returns the tenant's specific daily downtime"
    )
    @APIResponse(
            responseCode = "200",
            description = "Downtimes fetched successfully.",
            content = @Content(schema = @Schema(implementation = DailyDowntimeResponse.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid request payload.",
            content = @Content(schema = @Schema(implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "User not authenticated.",
            content = @Content(schema = @Schema(implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Downtimes not found.",
            content = @Content(schema = @Schema(implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error.",
            content = @Content(schema = @Schema(implementation = InformativeResponse.class))
    )

    @GET
    @Path("/tenants/{tenant-name}/downtimes/daily")
    @Produces(MediaType.APPLICATION_JSON)
    @SecuredEndpoint
    public Response getDailyDowntimes(
            @Parameter(
                    description = "The name of the tenant.",
                    required = true,
                    example = "TENANT-TEST",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("tenant-name")
            String tenantName,
            @Parameter(
                    name = "date",
                    description = "UTC date in yyyy-MM-dd format",
                    example = "2026-07-22",
                    required = false
            )
            @CheckDateFormat(
                    pattern = "yyyy-MM-dd",
                    message = "Valid date format is yyyy-MM-dd."
            )
            @QueryParam("date")
            String date
    ) {
        var tenant = tenantService.getTenantByName(tenantName);

        return Response.ok(
                downtimeService.fetchDailyDowntimes(tenant.id, date)
        ).build();
    }

    @Tag(name = "Automation")
    @Operation(
            summary = "Check if tenant feed topology is external.",
            description = "Returns true if the tenant uses an external feed topology, otherwise false."
    )
    @APIResponse(
            responseCode = "200",
            description = "Returns whether the tenant feed topology is external.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = IsExternalFeedTopologyResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Entity Not Found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @GET
    @Path("/tenants/{tenant-name}/feeds/topology/is-external")
    @Produces(MediaType.APPLICATION_JSON)
    @SecuredEndpoint
    public Response getIsExternalFeedTopology(
            @Parameter(
                    description = "The name of the tenant.",
                    required = true,
                    example = "TENANT-TEST",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("tenant-name")
            String tenantName) {
        var tenant = tenantService.getTenantByName(tenantName);

        var isExternal = tenantService.isExternalFeedTopology(tenant.id);

        return Response.ok(isExternal).build();
    }


}


