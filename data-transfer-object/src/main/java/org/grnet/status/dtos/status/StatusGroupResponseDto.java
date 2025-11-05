package org.grnet.status.dtos.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@ApplicationScoped
public class StatusGroupResponseDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Encrypted access token"
    )
    @JsonProperty("name")
    public String name;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Encrypted access token"
    )
    public String status;
    }

