package org.grnet.status.dtos.statuspage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Request DTO for creating or updating a status statuspage")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusPageRequestDto {

    @Schema(
            type = SchemaType.STRING,
            description = "Page name",
            example = "ARGO Status Page"
    )
    @JsonProperty("name")
    @NotEmpty(message = "name cannot be empty")
    public String name;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Slug of the statuspage (used in URL)",
            example = "argo-status"
    )
    @JsonProperty("slug")
    @NotEmpty(message = "slug cannot be empty")
    public String slug;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Base URL of the ARGO API",
            example = "https://api.devel.mon.argo.grnet.gr"
    )
    @JsonProperty("api")
    @NotEmpty(message = "api cannot be empty")
    public String api;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Encrypted secret key used for ARGO API authentication",
            example = "U2FsdGVkX1+a+pJq..."
    )
    @JsonProperty("secret")
    @NotEmpty(message = "secret cannot be empty")
    public String secret;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Report name to display status groups for",
            example = "Critical"
    )
    @JsonProperty("report")
    @NotEmpty(message = "report cannot be empty")
    public String report;

    @Schema(
            type = SchemaType.OBJECT,
            description = "JSON configuration object (groups, theming, description, etc.)"
    )
    @JsonProperty("config")
    public StatusPageConfigDto config;
}
