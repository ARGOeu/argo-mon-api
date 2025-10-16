package org.grnet.status.dtos.report;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Response with name and description")
public class ReportResponseDto {

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the report",
            example = "report1")
    public String name;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Description of the report",
            example = "Report for usage")
    public String description;
}
