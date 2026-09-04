package org.grnet.status.dtos.incident;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class IncidentDescriptionUpdateRequestDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Description associated with the new incident status",
            example = "The issue is currently under investigation."
    )
    @JsonProperty("description")
    public String description;
}