package org.grnet.status.dtos.recomputations;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecomputationRequestDto {

    @Schema(
            type = SchemaType.STRING,
            description = "Requester's name",
            example = "John Doe"
    )
    @NotBlank(message = "Requester's name must not be empty")
    private String requesterName;

    @Schema(
            type = SchemaType.STRING,
            description = "Requester's email",
            example = "grnet@gmail.com"
    )
    @Email(message = "Email must be a valid email address")
    @NotBlank(message = "Requester's Email must not be empty")
    private String requesterEmail;

    @NotBlank
    private String reason;
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of recomputation's start",
            example = "2025-10-22T12:44:48.107Z"
    )
    @NotNull
    private Instant startTime;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of recomputation's end",
            example = "2025-10-22T12:44:48.107Z"
    )
    @NotNull
    private Instant endTime;

    @NotBlank
    private String report;

    private List<String> exclude;

    @Valid
    private List<ExcludeMonitoringSourceDto> excludeMonitoringSource;

    @Valid
    private List<ExcludeMetricDto> excludeMetrics;

    @Valid
    private List<AppliedStatusChangeDto> appliedStatusChanges;

}

