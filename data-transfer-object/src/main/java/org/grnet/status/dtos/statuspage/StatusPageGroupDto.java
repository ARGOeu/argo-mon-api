package org.grnet.status.dtos.statuspage;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.dtos.status.StatusGroupResponseDto;

import java.util.List;

@Schema(name = "StatusPageGroupDto", description = "Represents a named group of monitored endpoints.")
public class StatusPageGroupDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Internal group identifier",
            example = "group-1"
    )
    @JsonProperty("name")
    @NotBlank(message = "group name cannot be blank")
    public String name;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "User-friendly alias of the group",
            example = "Group A"
    )
    @JsonProperty("alias")
    public String alias;

    @Schema(
            type = SchemaType.ARRAY,
            implementation = StatusGroupResponseDto.class,
            description = "List of items in this group"
    )
    @JsonProperty("list")
    @NotEmpty(message = "groups list cannot be empty")
    public List<@Valid StatusGroupResponseDto> list;
}
