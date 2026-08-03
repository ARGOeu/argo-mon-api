package org.grnet.status.dtos.tenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class PublicTenantInformationResponseDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Logo for the status page — can be a Base64 data URI or HTTPS URL",
            example = "data:image/png;base64,iVBORw0KGgoAAA... or https://example.com/logo.png"
    )
    @JsonProperty("logo")
    public String logo;

    @Schema(
            type = SchemaType.BOOLEAN,
            implementation = Boolean.class,
            description = "Indicates whether the tenant is configured to have performance data",
            example = "true"
    )
    @JsonProperty("performance")
    @Valid
    public Boolean performance;

}

