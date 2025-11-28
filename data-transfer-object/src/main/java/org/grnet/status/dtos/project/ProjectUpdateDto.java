package org.grnet.status.dtos.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.sql.Timestamp;

public class ProjectUpdateDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the project",
            example = "EOSC-Future"
    )
    @JsonProperty("name")
    @NotBlank(message = "name cannot be blank")
    public String name;

    @Schema(
            type = SchemaType.STRING,
            implementation = Timestamp.class,
            description = "Project start date (timestamp)",
            example = "2025-01-01T00:00:00Z"
    )
    @JsonProperty("start_date")
    public Timestamp startDate;

    @Schema(
            type = SchemaType.STRING,
            implementation = Timestamp.class,
            description = "Project end date (timestamp)",
            example = "2025-12-31T23:59:59Z"
    )
    @JsonProperty("end_date")
    public Timestamp endDate;

    @Schema(
            type = SchemaType.STRING,
            implementation = Timestamp.class,
            description = "Sustainability end date (timestamp)",
            example = "2027-12-31T23:59:59Z"
    )
    @JsonProperty("sustainability_end_date")
    public Timestamp sustainabilityEndDate;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Data retention policy text",
            example = "Policy Text"
    )
    @JsonProperty("data_retention_policy")
    public String dataRetentionPolicy;
}
