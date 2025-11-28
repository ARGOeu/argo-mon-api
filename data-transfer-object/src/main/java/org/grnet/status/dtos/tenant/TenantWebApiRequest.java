package org.grnet.status.dtos.tenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "TenantWebApiRequest", description = "Represents the configuration of a tenant.")
public class TenantWebApiRequest {

    @Schema(
            type = SchemaType.OBJECT,
            implementation = TenantInfoDto.class,
            description = "Tenant Info "
    )
    @JsonProperty("info")
    @Valid
    public TenantWebApiInfo info;

    public static class TenantWebApiInfo {


        @Schema(
                type = SchemaType.STRING,
                description = "Tenant's name",
                example = "GRNET"
        )
        @NotNull(message = "Name must not be null")
        @NotBlank(message = "Name must not be empty")
        @JsonProperty("name")
        public String name;

        @Schema(
                type = SchemaType.STRING,
                description = "Tenant's email",
                example = "grnet@gmail.com"
        )
        @Email(message = "Email must be a valid email address")
        @JsonProperty("email")
        public String email;

        @Schema(
                type = SchemaType.STRING,
                description = "Tenant's description",
                example = "This is the GRNET tenant"
        )
        @JsonProperty("description")
        public String description;

        @Schema(
                type = SchemaType.STRING,
                description = "Tenant's website",
                example = "www.grnet.gr"
        )
        @JsonProperty("website")
        public String website;

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
        public Instant updatedAt;

        @Schema(
                type = SchemaType.STRING,
                description = "Tenant's image",
                example = "/image/tenant.png"
        )
        @JsonProperty("image")
        public String image;
    }
}