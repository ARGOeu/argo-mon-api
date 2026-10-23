package org.grnet.status.enums.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;

public enum IncidentResource implements ApiResource {

    INCIDENT;
    @Override
    public String resourceName() {
        return "Incident";
    }
}
