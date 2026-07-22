package org.grnet.status.dtos.incident;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.enums.IncidentStatus;

import java.time.Instant;
import java.util.List;

public class IncidentResponseDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Incident identifier",
            example = "d747f74c-fbdd-47ca-8b8f-ce463354df21"
    )
    @JsonProperty("id")
    public String id;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Human-readable incident number",
            example = "INC-2026-000001"
    )
    @JsonProperty("incident_number")
    public String incidentNumber;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Incident title",
            example = "B2ACCESS authentication unavailable"
    )
    @JsonProperty("title")
    public String title;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Description of the incident",
            example = "Users are currently unable to authenticate through the B2ACCESS service."
    )
    @JsonProperty("description")
    public String description;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = ServiceDto.class,
            description = "Affected service"
    )
    @JsonProperty("service")
    public ServiceDto service;

    @Schema(
            implementation = IncidentStatus.class,
            description = "Current incident status",
            example = "NEW"
    )
    @JsonProperty("status")
    public IncidentStatus status;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the user who reported the incident",
            example = "John Doe"
    )
    @JsonProperty("created_by")
    public String createdBy;

    @Schema(
            type = SchemaType.STRING,
            implementation = Instant.class,
            description = "Incident creation timestamp",
            example = "2026-07-13T10:30:00Z"
    )
    @JsonProperty("created_at")
    public Instant createdAt;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "User who added the comment",
            example = "admin"
    )
    @JsonProperty("updated_by")
    public String updatedBy;

    @Schema(
            type = SchemaType.STRING,
            implementation = Instant.class,
            description = "Last update timestamp",
            example = "2026-07-13T10:30:00Z"
    )
    @JsonProperty("updated_at")
    public Instant updatedAt;

    @Schema(
            type = SchemaType.ARRAY,
            implementation = IncidentCommentResponseDto.class,
            description = "Comments added to the incident"
    )
    @JsonProperty("comments")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<IncidentCommentResponseDto> comments;
}