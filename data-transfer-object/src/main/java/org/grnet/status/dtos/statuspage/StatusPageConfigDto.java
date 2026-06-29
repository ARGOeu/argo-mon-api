package org.grnet.status.dtos.statuspage;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(name = "StatusPageConfigDto", description = "Represents the configuration of a status page.")
public class StatusPageConfigDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Title of the status page",
            example = "ARGO Status Page"
    )
    @JsonProperty("title")
    @NotBlank(message = "title cannot be blank")
    public String title;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Description text for the page",
            example = "Live status of selected groups."
    )
    @JsonProperty("description")
    public String description;

    @Schema(
            type = SchemaType.ARRAY,
            implementation = StatusPageGroupDto.class,
            description = "List of group sections to display"
    )
    @Valid
    @JsonProperty("groups")
    @NotEmpty(message = "groups cannot be empty")
    public List<@Valid StatusPageGroupDto> groups;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = StatusPageThemingDto.class,
            description = "Theming settings (logo, color, display options)"
    )
    @JsonProperty("theming")
    @Valid
    public StatusPageThemingDto theming;
}
