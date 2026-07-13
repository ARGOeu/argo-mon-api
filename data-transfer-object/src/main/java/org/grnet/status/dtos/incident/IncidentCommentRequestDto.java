package org.grnet.status.dtos.incident;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class IncidentCommentRequestDto {

    @Schema(
            type = SchemaType.STRING,
            description = "Incident comment",
            example = "The service owner has been contacted."
    )
    @JsonProperty("comment")
    @NotBlank(message = "comment cannot be blank")
    public String comment;
}
