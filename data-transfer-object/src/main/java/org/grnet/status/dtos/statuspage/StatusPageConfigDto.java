package org.grnet.status.dtos.statuspage;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(name = "StatusPageConfigDto", description = "Represents the configuration of a status page.")
public class StatusPageConfigDto {

    @Schema(
            type = SchemaType.STRING,
            description = "Title of the status page",
            example = "ARGO Status Page"
    )
    @JsonProperty("title")
    public String title;

    @Schema(
            type = SchemaType.STRING,
            description = "Description text for the page",
            example = "Live status of selected groups."
    )
    @JsonProperty("description")
    public String description;

    @Schema(
            type = SchemaType.ARRAY,
            description = "List of group sections to display"
    )
    @JsonProperty("groups")
    public List<StatusPageGroupDto> groups;

    @Schema(
            type = SchemaType.OBJECT,
            description = "Theming settings (logo, color, display options)"
    )
    @JsonProperty("theming")
    public StatusPageThemingDto theming;
}
