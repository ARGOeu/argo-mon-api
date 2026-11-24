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

    @Schema(type = SchemaType.OBJECT,
            implementation = FullReportResponseDto.Thresholds.class,
            description = "Threshold values for availability, reliability, uptime, unknown and downtime")
    @JsonProperty("thresholds")
    public FullReportResponseDto.Thresholds thresholds;

    @Schema(type = SchemaType.OBJECT,
            implementation = FullReportResponseDto.TopologySchema.class,
            description = "Topology schema defining the report's group and supergroup structure")
    @JsonProperty("topology_schema")
    public FullReportResponseDto.TopologySchema topologySchema;
}