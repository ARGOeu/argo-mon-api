package org.grnet.status.dtos.tenant.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.URL;

@Schema(name = "AuthMetadataDto", description = "Represents the configuration of a tenant's metadata auth info.")

public class AuthMetadataDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Auth Name",
            example="Auth Name1"
    )
    @JsonProperty("auth_name")
    @Valid
    public String authName;


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Auth Url",
            example="https://auth.url.example.com"
    )
    @JsonProperty("auth_url")
    @Valid
    @URL(message = "Invalid URL format")
    public String authUrl;
}
