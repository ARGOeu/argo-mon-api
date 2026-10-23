package org.grnet.status.dtos.incident;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.enums.IncidentStatus;

public class IncidentStatusUpdateRequestDto {

    @Schema(
            implementation = IncidentStatus.class,
            description = "New lifecycle status of the incident",
            example = "IN_PROGRESS"
    )
    @NotNull
    @JsonProperty("status")
    public IncidentStatus status;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Description associated with the new incident status",
            example = "The issue is currently under investigation."
    )
    @JsonProperty("status_description")
    public String statusDescription;
}