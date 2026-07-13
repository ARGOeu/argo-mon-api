package org.grnet.status.dtos.incident;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ServiceDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Service identifier",
            example = "6a6e8037-1e23-4b65-a75a-37d9e8d5bc44"
    )
    @JsonProperty("id")
    @NotBlank(message = "service.id cannot be blank")
    public String id;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Service name",
            example = "ESHOP"
    )
    @JsonProperty("name")
    @NotBlank(message = "service.name cannot be blank")
    public String name;
}
