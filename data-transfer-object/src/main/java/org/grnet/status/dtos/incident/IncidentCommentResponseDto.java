package org.grnet.status.dtos.incident;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

public class IncidentCommentResponseDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Comment identifier",
            example = "10e16683-b39e-4a8e-a691-5013f6618197"
    )
    @JsonProperty("id")
    public String id;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Incident comment",
            example = "The service owner has been contacted."
    )
    @JsonProperty("comment")
    public String comment;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "User who added the comment",
            example = "admin"
    )
    @JsonProperty("created_by")
    public String createdBy;

    @Schema(
            type = SchemaType.STRING,
            implementation = Instant.class,
            description = "Comment creation timestamp",
            example = "2026-07-14T09:45:00Z"
    )
    @JsonProperty("created_at")
    public Instant createdAt;
}