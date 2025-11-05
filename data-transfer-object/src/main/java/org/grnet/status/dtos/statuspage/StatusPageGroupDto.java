package org.grnet.status.dtos.statuspage;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.dtos.status.StatusGroupResponseDto;

import java.util.List;

@Schema(name = "StatusPageGroupDto", description = "Represents a named group of monitored endpoints.")
public class StatusPageGroupDto {

    @Schema(
            description = "Internal group identifier",
            example = "group-1"
    )
    @JsonProperty("name")
    public String name;

    @Schema(
            description = "User-friendly alias of the group",
            example = "Group A"
    )
    @JsonProperty("alias")
    public String alias;

    @Schema(
            description = "List of items in this group"
    )
    @JsonProperty("list")
    public List<StatusGroupResponseDto> list;
}
