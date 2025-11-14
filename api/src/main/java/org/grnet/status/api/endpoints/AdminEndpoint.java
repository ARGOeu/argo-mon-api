package org.grnet.status.api.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
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
import org.grnet.status.authorizations.interceptors.CheckEntitlements;
import org.grnet.status.constraints.NotFoundEntity;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.statuspage.StatusPageResponseDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.user.UserProfileDto;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.StatusPageService;
import org.grnet.status.services.TenantService;
import org.grnet.status.services.UserService;
import org.grnet.status.util.Utility;

import java.util.List;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/v1/admin")
@Authenticated
@SecurityScheme(
        securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
@CheckEntitlements(requireSuperAdmin = true)
public class AdminEndpoint {

    @Inject
    StatusPageService statusPageService;

    @Inject
    UserService userService;

    @Inject
    TenantService tenantService;

    @Inject
    Utility utility;

    @Tag(name = "Admin")
    @Operation(
            summary = "List all status pages",
            description = "Returns a list of all status pages."
    )
    @APIResponse(
            responseCode = "200",
            description = "List of all status pages",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = PageableStatusPages.class)))
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
            description = "Assessment already exists.",
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
    @Path("/pages")
    @Produces(MediaType.APPLICATION_JSON)
    @SecurityRequirement(name = "Authentication")
    public Response getAllStatusPages(
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.")
            @DefaultValue("1") @Min(value = 1, message = "Page number must be >= 1.") @QueryParam("page") int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.")
            @DefaultValue("10") @Min(value = 1, message = "Page size must be between 1 and 100.")
            @Max(value = 100, message = "Page size must be between 1 and 100.") @QueryParam("size") int size,
            @Context UriInfo uriInfo) {

        var pages = statusPageService.getStatusPageByPage(page - 1, size, uriInfo);

        return Response.ok(pages).build();
    }

    @Tag(name = "Admin")
    @Operation(summary = "Fetch all users registered to Status Page",
            description = "Returns a list of all registered users to the Status Page DB.")
    @APIResponse(
            responseCode = "200",
            description = "List of all users",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = UserProfileDto.class)))
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
            description = "Assessment already exists.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {

        var users = userService.fetchAllUsers();

        return Response.ok(users).build();
    }



    public static class PageableStatusPages extends PageResource<StatusPageResponseDto> {

        private List<StatusPageResponseDto> content;

        @Override
        public List<StatusPageResponseDto> getContent() {
            return content;
        }

        @Override
        public void setContent(List<StatusPageResponseDto> content) {
            this.content = content;
        }
    }


    @Tag(name = "Admin")
    @Operation(summary = "Create Tenant",
            description = "Creates a tenant to service and web-api")
    @APIResponse(
            responseCode = "200",
            description = "Secret encrypted successfully",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantResponseDto.class)))
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
            description = "Tenant already exists.",
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
    @SecurityRequirement(name = "Authentication")
    @POST
    @Path("/tenants")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Valid TenantRequestDto request) {

        var response = tenantService.create(request, utility.getUserUniqueIdentifier());
        return Response.ok(response).build();
    }


    @Tag(name = "Admin")
    @Operation(
            summary = "Get Tenant By Id .",
            description = "Returns a specific tenant assessment.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding tenant.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantResponseDto.class)))
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
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/tenants/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response getTenant(@Parameter(
                                      description = "The ID of the tenant to retrieve.",
                                      required = true,
                                      example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                                      schema = @Schema(type = SchemaType.STRING)) @PathParam("id")
                              @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id) {

        var tenant = tenantService.getTenantById(id);

        return Response.ok().entity(tenant).build();
    }@Tag(name = "Admin")

    @Operation(
            summary = "Update a tenant.",
            description = "Updates a specific tenant."
    )
    @APIResponse(
            responseCode = "200",
            description = "Tenant updated successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantResponseDto.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Page not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "409",
            description = "Tenant already exists.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )

    @PUT
    @Path("/tenants/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response updateTenant(
            @Parameter(description = "The ID of the status page to update.",
                    required = true,
                    example = "e7ab046c-8544-47e6-bd8f-e8aa8b83acb0",
                    schema = @Schema(type = SchemaType.STRING))
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,

            @Valid @NotNull(message = "The request body is empty.") TenantRequestDto request) {

        var updated = tenantService.updateTenant(id, request);
        return Response.ok().entity(updated).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Delete Tenant By Id .",
            description = "Deletes a specific tenant assessment.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding tenant.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantResponseDto.class)))
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
    @SecurityRequirement(name = "Authentication")
    @DELETE
    @Path("/tenants/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response deleteTenant(@Parameter(
            description = "The ID of the tenant to be deleted.",
            required = true,
            example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
            schema = @Schema(type = SchemaType.STRING)) @PathParam("id")
                              @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id) {

        tenantService.deleteTenantById(id);

        var informativeResponse = new InformativeResponse();
        informativeResponse.code = 200;
        informativeResponse.message = "Tenant has been successfully deleted.";
        return Response.ok().entity(informativeResponse).build();
    }
}
