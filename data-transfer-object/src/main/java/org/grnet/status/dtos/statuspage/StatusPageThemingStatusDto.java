package org.grnet.status.dtos.statuspage;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "StatusPageThemingStatusDto", description = "Theming settings for status indicators.")
public class StatusPageThemingStatusDto {

    @Schema(
            description = "Icon type to use",
            example = "led"
    )
    @JsonProperty("icon")
    public String icon;

    @Schema(
            description = "Text display mode",
            example = "none"
    )
    @JsonProperty("text")
    public String text;
}
