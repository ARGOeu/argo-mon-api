package org.grnet.status.dtos.incident;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class IncidentRequestDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Incident title",
            example = "ESHOP unavailable"
    )
    @JsonProperty("title")
    @NotBlank(message = "title cannot be blank")
    public String title;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Description of the incident",
            example = "Users cannot access the ESHOP service."
    )
    @JsonProperty("description")
    @NotBlank(message = "description cannot be blank")
    public String description;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = ServiceDto.class,
            description = "Affected service"
    )
    @JsonProperty("service")
    @NotNull(message = "service cannot be null")
    public ServiceDto service;
}