package org.grnet.status.dtos.downtime;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DailyDowntimeResponse {

    private String date;

    private List<DailyDowntimeEndpointResponse> endpoints;
}
