package org.grnet.status.dtos.setting;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Map;

@Schema(name = "SettingUpdate", description = "Used to update an existing application setting.")
public class SettingUpdateDto {

    @Schema(
            type = SchemaType.OBJECT,
            description = "Updated data of the setting.",
            example = "{\n" +
                    "  \"config\": {\n" +
                    "    \"base.url\": \"https://example.performance.com\"\n" +
                    "  }\n" +
                    "}"
    )
    @JsonProperty("data")
    public Map<String, Object> data;

    @Schema(
            type = SchemaType.BOOLEAN,
            description = "Enable or disable the setting.",
            example = "true"
    )
    @JsonProperty("enabled")
    public boolean enabled;
}

