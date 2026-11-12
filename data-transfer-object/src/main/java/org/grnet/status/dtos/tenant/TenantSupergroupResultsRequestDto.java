package org.grnet.status.dtos.tenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantSupergroupResultsRequestDto {

    @NotNull(message = "display_supergroup_results is required")
    @JsonProperty("display_supergroup_results")
    public Boolean displaySupergroupResults;
}