package org.grnet.status.dtos.statuspage;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "StatusPageThemingDto", description = "UI configuration and theming options.")
public class StatusPageThemingDto {

    @Schema(
            description = "Logo URL for the status page",
            example = "https://example.com/logo.png"
    )
    @JsonProperty("logo")
    public String logo;

    @Schema(
            description = "Page background or main color",
            example = "#ffffff"
    )
    @JsonProperty("color")
    public String color;

    @Schema(
            description = "Status display options (icon, text, etc.)"
    )
    @JsonProperty("status")
    public StatusPageThemingStatusDto status;

    @Schema(
            description = "Number of columns for layout",
            example = "one"
    )
    @JsonProperty("columns")
    public String columns;
}
