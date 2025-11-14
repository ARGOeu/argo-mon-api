package org.grnet.status.dtos.report;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;


@Schema(description = "Request to fetch reports from a target API")
public class ReportRequestDto {

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "API identifier or URL",
            example = "api")
    public String api;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Secret used to authenticate",
            example = "U2s3cr3tKeY")
    public String secret;
}
