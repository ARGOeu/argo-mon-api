package org.grnet.status.dtos.argo.web.api;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.grnet.status.dtos.recomputations.RecomputationHistoryDto;
import org.grnet.status.dtos.recomputations.RecomputationRequestDto;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RecomputationResponse extends RecomputationRequestDto {

    private String id;

    private String status;

    private Instant timestamp;

    private List<RecomputationHistoryDto> history;
}
