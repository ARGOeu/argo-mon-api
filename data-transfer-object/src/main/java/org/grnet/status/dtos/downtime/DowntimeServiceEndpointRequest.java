package org.grnet.status.dtos.downtime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DowntimeServiceEndpointRequest {

    private String hostname;

    private String service;
}
