package org.grnet.status.dtos.incident;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.enums.IncidentStatus;

import java.time.Instant;

public class IncidentActivityResponseDto {

    @Schema(
            type = SchemaType.STRING,
            description = "Activity identifier",
            example = "f84b5c5d-5c16-4b36-8fe4-dc832b95c982")
    @JsonProperty("id")
    public String id;

    @Schema(
            implementation = IncidentStatus.class,
            description = "The previous lifecycle status of the incident.",
            example = "NEW"
    )
    @JsonProperty("previous_status")
    public IncidentStatus previousStatus;

    @Schema(
            implementation = IncidentStatus.class,
            description = "The new lifecycle status of the incident.",
            example = "INVESTIGATING"
    )
    @JsonProperty("new_status")
    public IncidentStatus newStatus;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Description associated with this incident status change",
            example = "The issue is currently under investigation."
    )
    @JsonProperty("status_description")
    public String statusDescription;

    @Schema(
            type = SchemaType.STRING,
            description = "The user who performed the status change.",
            example = "admin"
    )
    @JsonProperty("changed_by")
    public String changedBy;

    @Schema(
            type = SchemaType.STRING,
            implementation = Instant.class,
            description = "The timestamp when the status change occurred.",
            example = "2026-07-20T12:30:00Z"
    )
    @JsonProperty("created_at")
    public Instant createdAt;
}