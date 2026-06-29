package org.grnet.status.enums.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;
public enum MetricProfileResource implements ApiResource {

    METRIC_PROFILE;
    @Override
    public String resourceName() {
        return "Metric_Profile";
    }
}

