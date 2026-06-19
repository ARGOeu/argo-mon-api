package org.grnet.status.enums.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;
public enum AggregationProfileResource implements ApiResource {

    AGGREGATION_PROFILE;
    @Override
    public String resourceName() {
        return "Aggregation_Profile";
    }
}

