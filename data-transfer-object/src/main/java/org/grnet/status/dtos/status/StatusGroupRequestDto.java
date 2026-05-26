package org.grnet.status.dtos.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
public class StatusGroupRequestDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "API identifier or URL",
            example = "https://api.devel.mon.argo.grnet.gr"
    )
    @JsonProperty("api")
    public String api;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Encrypted access token",
            example = "token"
    )
    @JsonProperty("secret")
    public String secret;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the report",
            example = "CORE"
    )
    @JsonProperty("report")
    public String report;
}
