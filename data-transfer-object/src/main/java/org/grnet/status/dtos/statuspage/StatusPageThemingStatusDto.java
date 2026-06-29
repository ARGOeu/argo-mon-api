package org.grnet.status.dtos.statuspage;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "StatusPageThemingStatusDto", description = "Theming settings for status indicators.")
public class StatusPageThemingStatusDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Icon type to use",
            example = "led"
    )
    @JsonProperty("icon")
    @NotBlank(message = "theming status icon cannot be blank")
    public String icon;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Text display mode",
            example = "none"
    )
    @JsonProperty("text")
    @NotBlank(message = "theming status text cannot be blank")
    public String text;
}
