package org.grnet.status.api.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;
import org.grnet.endpoint.scanner.runtime.resolvers.DynamicResolver;
import org.grnet.endpoint.scanner.runtime.resolvers.TestResolver;
import org.grnet.status.api.resolvers.CheckDateFormat;
import org.grnet.status.constraints.NotFoundEntity;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.TopologyService;

import java.util.List;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/v1/tests")
@Authenticated
@Tag(name = "Test")
@SecurityScheme(
        securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
//@CheckEntitlements(group = "tenants")
//@RegisterProvider(EntitlementAuthorizationFilter.class)

public class TestEndpoint {

    @Inject
    TopologyService topologyService;

    @Tag(name = "Test")
    @Operation(summary = "Fetch ARGO test topologies",
            description = "Retrieves tenant's endpoint topologies from the ARGO Web API.")
    @APIResponse(
            responseCode = "200",
            description = "List of available topologies",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = List.class)))
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
            responseCode = "409",
            description = "Topology already exists.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "501",
            description = "Not Implemented.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "502",
            description = "Connection error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/tenants/{id}/topology/{topology-id}/endpoints")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
//     @SecuredEndpoint(resolvers = {
//                    @TestResolver(idResolver = DynamicResolver.class, pathId = "id")
//            })

    // @SecuredEndpoint(resolvers = {})
    public void fetchEndpointTopologies(
            @Parameter(description = "The ID of the tenant.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid
            String id,
            @Parameter(description = "The topology id.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("topology-id")
            String topologyid,
            @Parameter(name = "date", in = QUERY, description = "Target date to retrieve a endpoint topology ") @QueryParam("date")
            @Valid @CheckDateFormat(pattern = "yyyy-mm-dd", message = "Valid date format is yyyy-mm-dd.") String date
    ) {

        topologyService.findTheRules();
    }

}