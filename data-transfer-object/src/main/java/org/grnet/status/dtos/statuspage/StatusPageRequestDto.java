package org.grnet.status.dtos.statuspage;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request DTO for creating or updating a status statuspage")
public class  StatusPageRequestDto {

    @Schema(
            type = SchemaType.STRING,
            description = "Page name",
            example = "ARGO Status Page"
    )
    @JsonProperty("name")
    @NotBlank(message = "name cannot be blank")
    public String name;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Slug of the statuspage (used in URL)",
            example = "argo-status"
    )
    @JsonProperty("slug")
    @NotBlank(message = "slug cannot be blank")
    public String slug;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Base URL of the ARGO API",
            example = "https://api.devel.mon.argo.grnet.gr"
    )
    @JsonProperty("api")
    @NotBlank(message = "api cannot be blank")
    public String api;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Encrypted secret key used for ARGO API authentication",
            example = "U2FsdGVkX1+a+pJq..."
    )
    @JsonProperty("secret")
    @NotBlank(message = "secret cannot be blank")
    public String secret;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Report name to display status groups for",
            example = "Critical"
    )
    @JsonProperty("report")
    @NotBlank(message = "report cannot be blank")
    public String report;

    @Schema(
            type = SchemaType.OBJECT,
            description = "JSON configuration object (groups, theming, description, etc.)"
    )
    @JsonProperty("config")
    @NotNull(message = "config must not be null")
    @Valid
    public StatusPageConfigDto config;
}
