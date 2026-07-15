package org.grnet.status.dtos.downtime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.constraints.ValidDowntimeClassification;
import org.grnet.status.constraints.ValidDowntimeSeverity;

@Getter
@Setter
public class DowntimeRequest {


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the downtime",
            example = "Downtime due to expired certs"
    )
    private String name;


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Severity of the impact of the downtime. Outage or warning",
            example = "Warning"
    )
    @ValidDowntimeSeverity
    private String severity;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Message to describe the downtime",
            example = "This is an example downtime"
    )
    private String message;


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Scheduled start time of the downtime in UTC timezone",
            example = "2026-07-07T00:00:00Z"
    )

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
    )
    @JsonProperty("scheduled_at")
    public Instant scheduledAt;


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Scheduled completed time of the downtime in UTC timezone",
            example = "2026-07-07T00:00:00Z"
    )

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC"
    )
    @JsonProperty("completed_at")
    public Instant completedAt;

    private List<DowntimeServiceEndpointRequest> services;

}