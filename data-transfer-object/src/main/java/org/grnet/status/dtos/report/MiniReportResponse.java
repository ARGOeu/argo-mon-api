package org.grnet.status.dtos.report;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Response with name and description")
public class MiniReportResponse {

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Identifier of the report",
            example = "Report Id")
    @JsonProperty("id")
    public String id;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the report",
            example = "report1")
    @JsonProperty("name")
    public String name;

    @Schema(type = SchemaType.BOOLEAN,
            implementation = Boolean.class,
            description = "Indicates whether this report is a node report.",
            example = "true")
    @JsonProperty("node")
    public Boolean node;

}