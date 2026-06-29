package org.grnet.status.dtos.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Generic response indicating whether a given resource exists.")
public class ExistResponseDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The name or identifier that was checked.",
            example = "status-page"
    )
    @JsonProperty("name")
    public String name;

    @Schema(
            type = SchemaType.BOOLEAN,
            implementation = Boolean.class,
            description = "True if the resource exists, false otherwise.",
            example = "true"
    )
    @JsonProperty("exist")
    public Boolean exist;
}
