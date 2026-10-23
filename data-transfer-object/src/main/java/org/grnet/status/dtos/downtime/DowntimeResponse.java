package org.grnet.status.dtos.downtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DowntimeResponse {

    private String id;

    private String name;

    private String severity;

    private String message;
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of creation",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("scheduled_at")
    public Instant scheduledAt;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of downtime completion , UTC timezone",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("completed_at")
    public Instant completedAt;


    private String classification;

    private List<DowntimeServiceEndpointResponse> services;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_by")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String updatedBy;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of creation",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("created_at")
    public Instant createdAt;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of last update",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("updated_at")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Instant updatedAt;

    @JsonProperty("warning")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String warning;
}