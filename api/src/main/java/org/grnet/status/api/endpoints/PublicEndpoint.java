package org.grnet.status.api.endpoints;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.status.api.resolvers.CheckDateFormat;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.report.PartialReportResponseDto;
import org.grnet.status.dtos.statuspage.StatusPageConfigDto;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGroupResultsResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGroupStatusResponse;
import org.grnet.status.services.ReportService;
import org.grnet.status.services.StatusService;
import org.grnet.status.services.TenantService;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Tag(name = "Public")
@Path("/v1/public")
public class PublicEndpoint {

    @Inject
    StatusService statusService;

    @Inject
    TenantService tenantService;

    @Inject
    ReportService reportService;

    @Operation(
            summary = "Get status page configuration by slug",
            description = "Returns only the public configuration (config field) for the given slug."
    )
    @APIResponse(
            responseCode = "200",
            description = "Configuration found",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = StatusPageConfigDto.class,
                    description = "The stored public configuration object"
            ))
    )
    @APIResponse(
            responseCode = "404",
            description = "Status page not found",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @GET
    @Path("/pages/{slug}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getStatusPageConfig(@PathParam("slug") String slug) {

        var statusPage = statusService.getConfigBySlug(slug);

        return Response.ok(statusPage).build();
    }

    @Tag(name = "Public")
    @Operation(
            summary = "Retrieve tenant group results.",
            description = "Retrieves latest availability and uptime results for all tenant groups."
    )
    @APIResponse(
            responseCode = "200",
            description = "Tenant group results.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantWebApiGroupResultsResponse.class)))
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
            description = "Tenant not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @GET
    @Path("/tenants/{tenant-name}/results/groups")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getPublicGroupResults(
            @Parameter(
                    description = "The name of the tenant.",
                    required = true,
                    example = "TENANT-GRNET",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("tenant-name")
            String tenantName,
            @Parameter(name = "group", in = QUERY,
                    description = "Optional group name.",
                    example = "WIKI")
            @QueryParam("group")
            String groupName,
            @Parameter(name = "date", in = QUERY,
                    description = "UTC date in YYYY-MM-DD format.",
                    example = "2026-05-21")
            @QueryParam("date")
            @CheckDateFormat(pattern = "yyyy-MM-dd",
                    message = "Valid date format is yyyy-MM-dd.")
            String date,
            @Parameter(name = "period", in = QUERY,
                    description = "Specify the lookback window in days or weeks (e.g. 7d or 2w).",
                    example = "7d")
            @QueryParam("period")
            String period,
            @Parameter(name = "start_time", in = QUERY,
                    description = "UTC time in W3C format.",
                    example = "2026-05-21T12:00:00Z")
            @QueryParam("start_time")
            String startTime,
            @Parameter(name = "end_time", in = QUERY,
                    description = "UTC time in W3C format.",
                    example = "2026-05-22T12:00:00Z")
            @QueryParam("end_time")
            String endTime,
            @Parameter(name = "start_date", in = QUERY,
                    description = "UTC date in YYYY-MM-DD format.",
                    example = "2026-05-20")
            @QueryParam("start_date")
            @CheckDateFormat(pattern = "yyyy-MM-dd",
                    message = "Valid date format is yyyy-MM-dd.")
            String startDate,
            @Parameter(name = "end_date", in = QUERY,
                    description = "UTC date in YYYY-MM-DD format.",
                    example = "2026-05-22")
            @QueryParam("end_date")
            @CheckDateFormat(pattern = "yyyy-MM-dd",
                    message = "Valid date format is yyyy-MM-dd.")
            String endDate,
            @Parameter(name = "granularity", in = QUERY,
                    description = "Granularity of time that will be used to present data. Possible values are monthly, daily.",
                    example = "daily")
            @QueryParam("granularity")
            String granularity,
            @Parameter(name = "report", in = QUERY,
                    description = "Target report name. Optional when the tenant has only one report. " +
                            "Required when the tenant has multiple reports.",
                    example = "BASIC")
            @QueryParam("report")
            String report) {

        var tenant = tenantService.getTenantByName(tenantName);

        var response = tenantService.getGroupResults(tenant.id, groupName, date, period, startTime, endTime, startDate, endDate, granularity, report);

        return Response.ok().entity(response).build();
    }


    @Tag(name = "Public")
    @Operation(
            summary = "Retrieve tenant group status.",
            description = "Retrieves latest status results for a specific tenant group."
    )
    @APIResponse(
            responseCode = "200",
            description = "Tenant group status results.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantWebApiGroupStatusResponse.class)))
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
            description = "Tenant not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/tenants/{tenant-name}/status/groups")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getGroupStatusByGroup(
            @Parameter(
                    description = "The name of the tenant.",
                    required = true,
                    example = "TENANT-GRNET",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("tenant-name")
            String tenantName,
            @Parameter(name = "group", in = QUERY,
                    description = "Optional group name.",
                    example = "WIKI")
            @QueryParam("group")
            String groupName,
            @Parameter(name = "start_time", in = QUERY,
                    description = "UTC time in W3C format.",
                    example = "2026-05-21T12:00:00Z")
            @QueryParam("start_time")
            String startTime,
            @Parameter(name = "end_time", in = QUERY,
                    description = "UTC time in W3C format.",
                    example = "2026-05-22T12:00:00Z")
            @QueryParam("end_time")
            String endTime,
            @Parameter(name = "history", in = QUERY,
                    description = "Show full history of status timelines.",
                    example = "true")
            @QueryParam("history")
            Boolean history,
            @Parameter(name = "report", in = QUERY,
                    description = "Target report name. Optional when the tenant has only one report. " +
                            "Required when the tenant has multiple reports.",
                    example = "BASIC")
            @QueryParam("report")
            String report) {

        var tenant = tenantService.getTenantByName(tenantName);

        var response = tenantService.getGroupStatus(tenant.id, groupName, startTime, endTime, history, report);

        return Response.ok().entity(response).build();
    }


    @Tag(name = "Public")
    @Operation(summary = "Fetch Public ARGO reports",
            description = "Retrieves public reports from the ARGO Web API.")
    @APIResponse(
            responseCode = "200",
            description = "List of available public reports",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = PartialReportResponseDto.class)))
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
    @GET
    @Path("/tenants/{tenant-name}/reports/public")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response fetchPublicReports(
            @Parameter(
                    description = "The name of the tenant.",
                    required = true,
                    example = "TENANT-GRNET",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("tenant-name")
            String tenantName,
            @Parameter(name = "search", in = QUERY,
                    description = "Search report by name.")
            @QueryParam("search") String search,
            @Parameter(name = "node", in = QUERY,
                    description = "Get node reports.")
            @QueryParam("node") Boolean node)
    {

        var tenant = tenantService.getTenantByName(tenantName);
        var reports = reportService.fetchReportsByStatus(tenant.id, search, Boolean.TRUE, node);

        return Response.ok(reports).build();
    }
}
