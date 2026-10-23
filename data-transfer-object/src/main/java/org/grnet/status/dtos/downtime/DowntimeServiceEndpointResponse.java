package org.grnet.status.dtos.downtime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DowntimeServiceEndpointResponse {

    private String id;

    private String hostname;

    private String service;

}
